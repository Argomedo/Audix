package cl.diego.audiotv

import android.content.Context
import android.media.audiofx.BassBoost
import android.media.audiofx.DynamicsProcessing
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.log10
import kotlin.math.roundToInt

data class PresetConfig(
    val gains: FloatArray,
    val bass: Int = 0,
    val loudness: Int = 0,
    val bassStabilizer: Int = 0,
    val vocalClarity: Int = 0,
    val smartVolume: Int = 0,
    val stereoWide: Int = 0,
    val limiter: Int = 0
)

object Presets {

    val configs: Map<String, PresetConfig> = linkedMapOf(
        "Flat" to PresetConfig(gains = floatArrayOf(0f, 0f, 0f, 0f, 0f)),
        "Rock" to PresetConfig(
            gains = floatArrayOf(5f, 3f, -2f, 3f, 5f),
            bass = 55, loudness = 15, bassStabilizer = 25,
            vocalClarity = 10, smartVolume = 15, stereoWide = 30, limiter = 35
        ),
        "Jazz" to PresetConfig(
            gains = floatArrayOf(4f, 2f, -2f, 2f, 5f),
            bass = 35, loudness = 10, bassStabilizer = 20,
            vocalClarity = 30, smartVolume = 15, stereoWide = 20, limiter = 25
        ),
        "Cinema" to PresetConfig(
            gains = floatArrayOf(6f, 3f, 0f, 2f, 4f),
            bass = 55, loudness = 25, bassStabilizer = 45,
            vocalClarity = 45, smartVolume = 55, stereoWide = 40, limiter = 65
        ),
        // Ajustado para Sony HT-S100F (2.0, sin subwoofer, S-Force propio)
        "Cine Barra" to PresetConfig(
            gains = floatArrayOf(3f, 1f, -1f, 3f, 2f),
            bass = 40, loudness = 20, bassStabilizer = 50,
            vocalClarity = 55, smartVolume = 60, stereoWide = 15, limiter = 70
        ),
        "Voz" to PresetConfig(
            gains = floatArrayOf(-4f, -1f, 4f, 4f, -1f),
            bass = 15, loudness = 20, bassStabilizer = 25,
            vocalClarity = 80, smartVolume = 60, stereoWide = 10, limiter = 45
        ),
        "Bass+" to PresetConfig(
            gains = floatArrayOf(8f, 5f, 0f, 0f, 0f),
            bass = 90, loudness = 10, bassStabilizer = 70,
            vocalClarity = 0, smartVolume = 20, stereoWide = 10, limiter = 60
        ),
        "Pop" to PresetConfig(
            gains = floatArrayOf(-1f, 3f, 5f, 3f, -1f),
            bass = 45, loudness = 15, bassStabilizer = 20,
            vocalClarity = 35, smartVolume = 25, stereoWide = 35, limiter = 40
        ),
        "Agudos+" to PresetConfig(
            gains = floatArrayOf(0f, 0f, 0f, 4f, 8f),
            bass = 20, loudness = 10, bassStabilizer = 15,
            vocalClarity = 45, smartVolume = 15, stereoWide = 30, limiter = 30
        )
    )

    val names: List<String> = configs.keys.toList()

    // Compatibilidad con código antiguo que usaba Presets.gains
    val gains: Map<String, FloatArray>
        get() = configs.mapValues { it.value.gains }
}


data class Profile(
    val name: String,
    val bass: Int,
    val loud: Int,
    val surr: Int,
    val rumble: Boolean,
    val freqs: List<Int>,
    val gains: List<Float>,
    val bassStabilizer: Int = 0,
    val vocalClarity: Int = 0,
    val smartVolume: Int = 0,
    val limiter: Int = 0,
    val volumeBoost: Int = 100          // 100 = sin refuerzo
)


class AudioEngine private constructor(ctx: Context) {

    private val prefs = ctx.getSharedPreferences("audiotv", Context.MODE_PRIVATE)

    private var bassFx: BassBoost? = null
    private var loudFx: LoudnessEnhancer? = null
    private var virtFx: Virtualizer? = null
    private var eqFx: Equalizer? = null
    private var dp: DynamicsProcessing? = null

    private var eqMin = -1200
    private var eqMax = 1200

    var power by mutableStateOf(prefs.getBoolean("power", true))
    var bass by mutableIntStateOf(prefs.getInt("bass", 0))
    var loudness by mutableIntStateOf(prefs.getInt("loud", 0))

    // "surround" ahora representa Stereo Wide (se conserva el nombre por compatibilidad)
    var surround by mutableIntStateOf(
        prefs.getInt("stereo_wide", prefs.getInt("surr", 0))
    )

    // Variable antigua, se mantiene para no romper perfiles anteriores
    var rumble by mutableStateOf(prefs.getBoolean("rumble", false))

    var bassStabilizer by mutableIntStateOf(
        prefs.getInt(
            "bass_stabilizer",
            if (!prefs.contains("bass_stabilizer") && prefs.getBoolean("rumble", false)) 35 else 0
        )
    )

    var vocalClarity by mutableIntStateOf(prefs.getInt("vocal_clarity", 0))
    var smartVolume by mutableIntStateOf(prefs.getInt("smart_volume", 0))
    var limiter by mutableIntStateOf(prefs.getInt("limiter", 0))

    /*
     * Refuerzo de volumen en porcentaje.
     * 100 = normal, 200 = el doble de amplitud (+6 dB), 300 = +9.5 dB.
     */
    var volumeBoost by mutableIntStateOf(
        prefs.getInt("volume_boost_pct", 100).coerceIn(100, 300)
    )

    /** Ganancia equivalente en dB del refuerzo actual. */
    val boostDb: Float
        get() = if (volumeBoost <= 100) 0f
        else (20.0 * log10(volumeBoost / 100.0)).toFloat()

    var preset by mutableStateOf(prefs.getString("preset", "Flat") ?: "Flat")

    // Tema elegido (lo usa la pantalla de Temas)
    var theme by mutableStateOf(prefs.getString("theme", "Oscuro") ?: "Oscuro")

    fun updateTheme(name: String) {
        theme = name
        prefs.edit().putString("theme", name).apply()
    }

    var hzEditable by mutableStateOf(false)
    var summary by mutableStateOf("")

    val freqs = mutableStateListOf<Int>()
    val gains = mutableStateListOf<Float>()
    val profiles = mutableStateListOf<Profile>()

    var activeProfile by mutableStateOf(prefs.getString("activeProfile", null))

    val advancedDspAvailable: Boolean
        get() = dp != null

    init {
        bassFx = runCatching { BassBoost(0, 0) }.getOrNull()
        loudFx = runCatching { LoudnessEnhancer(0) }.getOrNull()
        virtFx = runCatching { Virtualizer(0, 0) }.getOrNull()

        initEq()
        loadProfiles()

        summary = listOf(
            "Bass" to bassFx,
            "Loudness" to loudFx,
            "DSP" to (dp ?: eqFx),
            "Volumen+" to (dp ?: loudFx),
            "Stereo Wide" to virtFx
        ).joinToString("    ") { (name, fx) ->
            "$name ${if (fx != null) "✓" else "✗"}"
        }

        applyAll()
    }


    private fun initEq() {

        val savedFreqs = prefs.getString("freqs", null)
            ?.split(",")?.mapNotNull { it.toIntOrNull() }

        val savedGains = prefs.getString("gains", null)
            ?.split(",")?.mapNotNull { it.toFloatOrNull() }

        if (Build.VERSION.SDK_INT >= 28) {
            dp = runCatching {
                val cfg = DynamicsProcessing.Config.Builder(
                    DynamicsProcessing.VARIANT_FAVOR_FREQUENCY_RESOLUTION,
                    2,
                    true, 6,    // Pre-EQ
                    true, 4,    // MBC
                    true, 3,    // Post-EQ
                    true        // Limiter
                ).build()
                DynamicsProcessing(0, 0, cfg)
            }.getOrNull()
        }

        if (dp != null) {

            hzEditable = true

            freqs.addAll(
                if (savedFreqs != null && savedFreqs.size == 5) savedFreqs
                else listOf(60, 230, 910, 3600, 14000)
            )

            gains.addAll(
                if (savedGains != null && savedGains.size == 5) savedGains
                else List(5) { 0f }
            )

        } else {

            eqFx = runCatching { Equalizer(0, 0) }.getOrNull()

            val e = eqFx ?: return

            val range = e.bandLevelRange
            eqMin = range[0].toInt()
            eqMax = range[1].toInt()

            val numberOfBands = e.numberOfBands.toInt()

            for (i in 0 until numberOfBands) {
                freqs.add(e.getCenterFreq(i.toShort()) / 1000)
            }

            gains.addAll(
                if (savedGains != null && savedGains.size == numberOfBands) savedGains
                else List(numberOfBands) { 0f }
            )
        }
    }


    private fun applyAll() {

        val enabled = power

        runCatching {
            bassFx?.apply {
                this.enabled = enabled
                if (enabled) setStrength((bass * 10).toShort())
            }
        }

        runCatching {
            loudFx?.apply {
                this.enabled = enabled
                if (enabled) {
                    // Si no hay DynamicsProcessing, el refuerzo se suma aquí (respaldo)
                    val fallbackBoost =
                        if (dp == null) (boostDb * 100).roundToInt() else 0
                    setTargetGain(loudness * 10 + fallbackBoost)
                }
            }
        }

        runCatching {
            virtFx?.apply {
                this.enabled = enabled
                if (enabled) setStrength((surround * 10).toShort())
            }
        }

        applyEq()
    }


    private fun applyEq() {

        val enabled = power
        val dynamics = dp

        if (dynamics != null) {

            runCatching {

                dynamics.enabled = enabled

                /* ---------- GANANCIA DE ENTRADA (Volumen+) ---------- */
                dynamics.setInputGainAllChannelsTo(
                    if (enabled) boostDb else 0f
                )

                /* ---------- PRE EQ ---------- */
                dynamics.setPreEqBandAllChannelsTo(
                    0, DynamicsProcessing.EqBand(true, 35f, 0f)
                )

                for (i in freqs.indices) {
                    dynamics.setPreEqBandAllChannelsTo(
                        i + 1,
                        DynamicsProcessing.EqBand(true, freqs[i].toFloat(), gains[i])
                    )
                }

                /* ---------- MBC ---------- */
                val stabilizer = bassStabilizer / 100f
                val smart = smartVolume / 100f
                val useMbc = stabilizer > 0f || smart > 0f

                val smartRatio = 1f + (2f * smart)
                val smartThreshold = -30f + (15f * smart)
                val bassRatio = 1f + (2f * stabilizer)
                val bassThreshold = -30f + (15f * stabilizer)

                val mbc = DynamicsProcessing.Mbc(true, enabled && useMbc, 4)

                // Graves
                mbc.setBand(
                    0,
                    DynamicsProcessing.MbcBand(
                        true, 120f, 10f, 140f,
                        maxOf(bassRatio, smartRatio),
                        maxOf(bassThreshold, smartThreshold),
                        6f, -80f, 1f, 0f, 0f
                    )
                )
                // Medios-graves
                mbc.setBand(
                    1,
                    DynamicsProcessing.MbcBand(
                        true, 500f, 15f, 140f,
                        smartRatio, smartThreshold, 6f, -80f, 1f, 0f, 0f
                    )
                )
                // Medios
                mbc.setBand(
                    2,
                    DynamicsProcessing.MbcBand(
                        true, 3000f, 15f, 130f,
                        smartRatio, smartThreshold, 6f, -80f, 1f, 0f, 0f
                    )
                )
                // Agudos
                mbc.setBand(
                    3,
                    DynamicsProcessing.MbcBand(
                        true, 12000f, 15f, 120f,
                        smartRatio, smartThreshold, 6f, -80f, 1f, 0f, 0f
                    )
                )

                dynamics.setMbcAllChannelsTo(mbc)

                /* ---------- POST EQ ---------- */
                val clarity = vocalClarity / 100f

                dynamics.setPostEqBandAllChannelsTo(
                    0, DynamicsProcessing.EqBand(true, 250f, -2f * clarity)
                )
                dynamics.setPostEqBandAllChannelsTo(
                    1, DynamicsProcessing.EqBand(true, 2500f, 4f * clarity)
                )
                dynamics.setPostEqBandAllChannelsTo(
                    2, DynamicsProcessing.EqBand(true, 8000f, 1.5f * clarity)
                )

                /* ---------- LIMITADOR ---------- */
                val limit = limiter / 100f
                val baseThreshold = -1f - (5f * limit)

                // Con refuerzo activo el techo baja a -2 dB como mínimo
                val threshold =
                    if (volumeBoost > 100) minOf(baseThreshold, -2f)
                    else baseThreshold

                // El limitador queda activo siempre que haya refuerzo
                val limiterEnabled =
                    enabled && (limiter > 0 || volumeBoost > 100)

                dynamics.setLimiterAllChannelsTo(
                    DynamicsProcessing.Limiter(
                        true, limiterEnabled, 0,
                        2f, 100f, 20f, threshold, 0f
                    )
                )
            }

            return
        }

        /* ---------- FALLBACK: Equalizer clásico ---------- */

        val equalizer = eqFx ?: return

        runCatching {
            equalizer.enabled = enabled
            for (i in freqs.indices) {
                equalizer.setBandLevel(
                    i.toShort(),
                    (gains[i] * 100).toInt().coerceIn(eqMin, eqMax).toShort()
                )
            }
        }
    }


    private fun saveCurrent() {
        prefs.edit()
            .putBoolean("power", power)
            .putInt("bass", bass)
            .putInt("loud", loudness)
            .putInt("surr", surround)           // compatibilidad antigua
            .putInt("stereo_wide", surround)    // nuevo nombre
            .putBoolean("rumble", rumble)
            .putInt("bass_stabilizer", bassStabilizer)
            .putInt("vocal_clarity", vocalClarity)
            .putInt("smart_volume", smartVolume)
            .putInt("limiter", limiter)
            .putInt("volume_boost_pct", volumeBoost)
            .putString("preset", preset)
            .putString("freqs", freqs.joinToString(","))
            .putString("gains", gains.joinToString(","))
            .apply()
    }


    fun saveCustomPreset() {
        prefs.edit()
            .putInt("custom_bass", bass)
            .putInt("custom_loud", loudness)
            .putInt("custom_surr", surround)
            .putInt("custom_stereo_wide", surround)
            .putBoolean("custom_rumble", rumble)
            .putInt("custom_bass_stabilizer", bassStabilizer)
            .putInt("custom_vocal_clarity", vocalClarity)
            .putInt("custom_smart_volume", smartVolume)
            .putInt("custom_limiter", limiter)
            .putString("custom_freqs", freqs.joinToString(","))
            .putString("custom_gains", gains.joinToString(","))
            .apply()

        preset = "Personalizado"
        saveCurrent()
    }


    fun hasCustomPreset(): Boolean = prefs.contains("custom_bass")


    fun loadCustomPreset() {

        if (!hasCustomPreset()) return

        update {

            bass = prefs.getInt("custom_bass", 0)
            loudness = prefs.getInt("custom_loud", 0)
            surround = prefs.getInt(
                "custom_stereo_wide", prefs.getInt("custom_surr", 0)
            )
            rumble = prefs.getBoolean("custom_rumble", false)
            bassStabilizer = prefs.getInt(
                "custom_bass_stabilizer", if (rumble) 35 else 0
            )
            vocalClarity = prefs.getInt("custom_vocal_clarity", 0)
            smartVolume = prefs.getInt("custom_smart_volume", 0)
            limiter = prefs.getInt("custom_limiter", 0)

            val customFreqs = prefs.getString("custom_freqs", null)
                ?.split(",")?.mapNotNull { it.toIntOrNull() }

            val customGains = prefs.getString("custom_gains", null)
                ?.split(",")?.mapNotNull { it.toFloatOrNull() }

            if (customFreqs != null && customFreqs.size == freqs.size) {
                freqs.clear()
                freqs.addAll(customFreqs)
            }

            if (customGains != null && customGains.size == gains.size) {
                gains.clear()
                gains.addAll(customGains)
            }

            preset = "Personalizado"
        }
    }


    private fun loadProfiles() {

        val arr = runCatching {
            JSONArray(prefs.getString("profiles", "[]"))
        }.getOrDefault(JSONArray())

        for (i in 0 until arr.length()) {

            runCatching {

                val o = arr.getJSONObject(i)

                profiles.add(
                    Profile(
                        name = o.getString("name"),
                        bass = o.getInt("bass"),
                        loud = o.getInt("loud"),
                        surr = o.optInt("surr", 0),
                        rumble = o.optBoolean("rumble", false),
                        freqs = o.getString("freqs").split(",").map { it.toInt() },
                        gains = o.getString("gains").split(",").map { it.toFloat() },
                        bassStabilizer = o.optInt(
                            "bassStabilizer",
                            if (o.optBoolean("rumble", false)) 35 else 0
                        ),
                        vocalClarity = o.optInt("vocalClarity", 0),
                        smartVolume = o.optInt("smartVolume", 0),
                        limiter = o.optInt("limiter", 0),
                        volumeBoost = o.optInt("volumeBoost", 100)
                    )
                )
            }
        }
    }


    private fun saveProfiles() {

        val arr = JSONArray()

        profiles.forEach { p ->
            arr.put(
                JSONObject()
                    .put("name", p.name)
                    .put("bass", p.bass)
                    .put("loud", p.loud)
                    .put("surr", p.surr)
                    .put("rumble", p.rumble)
                    .put("bassStabilizer", p.bassStabilizer)
                    .put("vocalClarity", p.vocalClarity)
                    .put("smartVolume", p.smartVolume)
                    .put("limiter", p.limiter)
                    .put("volumeBoost", p.volumeBoost)
                    .put("freqs", p.freqs.joinToString(","))
                    .put("gains", p.gains.joinToString(","))
            )
        }

        prefs.edit()
            .putString("profiles", arr.toString())
            .putString("activeProfile", activeProfile)
            .apply()
    }


    fun saveProfile(name: String) {

        val n = name.trim()
        if (n.isEmpty()) return

        val p = Profile(
            name = n,
            bass = bass,
            loud = loudness,
            surr = surround,
            rumble = rumble,
            freqs = freqs.toList(),
            gains = gains.toList(),
            bassStabilizer = bassStabilizer,
            vocalClarity = vocalClarity,
            smartVolume = smartVolume,
            limiter = limiter,
            volumeBoost = volumeBoost
        )

        val i = profiles.indexOfFirst { it.name == n }

        if (i >= 0) profiles[i] = p else profiles.add(p)

        activeProfile = n
        saveProfiles()
    }


    fun loadProfile(p: Profile) {

        update {

            bass = p.bass
            loudness = p.loud
            surround = p.surr
            rumble = p.rumble
            bassStabilizer = p.bassStabilizer
            vocalClarity = p.vocalClarity
            smartVolume = p.smartVolume
            limiter = p.limiter
            volumeBoost = p.volumeBoost.coerceIn(100, 300)

            if (p.freqs.size == freqs.size && p.gains.size == gains.size) {
                for (i in freqs.indices) {
                    freqs[i] = p.freqs[i]
                    gains[i] = p.gains[i]
                }
            }

            preset = "Personalizado"
        }

        activeProfile = p.name
        saveProfiles()
    }


    fun deleteProfile(name: String) {

        profiles.removeAll { it.name == name }

        if (activeProfile == name) activeProfile = null

        saveProfiles()
    }


    fun renameProfile(old: String, new: String) {

        val n = new.trim()
        val i = profiles.indexOfFirst { it.name == old }

        if (i < 0 || n.isEmpty() || profiles.any { it.name == n }) return

        profiles[i] = profiles[i].copy(name = n)

        if (activeProfile == old) activeProfile = n

        saveProfiles()
    }


    fun duplicateProfile(name: String) {

        val i = profiles.indexOfFirst { it.name == name }
        if (i < 0) return

        var n = "$name copia"
        var k = 2

        while (profiles.any { it.name == n }) {
            n = "$name copia $k"
            k++
        }

        profiles.add(i + 1, profiles[i].copy(name = n))
        saveProfiles()
    }


    fun moveProfile(name: String, delta: Int) {

        val i = profiles.indexOfFirst { it.name == name }
        val j = i + delta

        if (i < 0 || j !in profiles.indices) return

        val p = profiles.removeAt(i)
        profiles.add(j, p)

        saveProfiles()
    }


    fun update(block: AudioEngine.() -> Unit) {
        this.block()
        applyAll()
        saveCurrent()
    }


    fun usePreset(name: String) {

        if (name == "Personalizado") {
            loadCustomPreset()
            return
        }

        val config = Presets.configs[name] ?: return

        update {

            preset = name

            // EQ
            for (i in gains.indices) {
                val index = (i * 4f / (gains.size - 1).coerceAtLeast(1))
                    .roundToInt()
                    .coerceIn(0, config.gains.lastIndex)

                gains[i] = config.gains[index]
            }

            // Efectos (el refuerzo de volumen NO se toca)
            bass = config.bass
            loudness = config.loudness
            bassStabilizer = config.bassStabilizer
            vocalClarity = config.vocalClarity
            smartVolume = config.smartVolume
            surround = config.stereoWide
            limiter = config.limiter
            rumble = false
        }
    }


    fun setGain(index: Int, value: Float) {
        update {
            gains[index] = value
            preset = "Personalizado"
        }
    }


    fun nudgeFreq(index: Int, up: Boolean) {

        update {

            val frequency = (freqs[index] * if (up) 1.1f else 0.9f).roundToInt()

            val low = if (index == 0) 50 else freqs[index - 1] + 10

            val high =
                if (index == freqs.lastIndex) 20000 else freqs[index + 1] - 10

            if (low < high) {
                freqs[index] = frequency.coerceIn(low, high)
            }

            preset = "Personalizado"
        }
    }


    companion object {

        @Volatile
        private var instance: AudioEngine? = null

        fun get(context: Context): AudioEngine {
            return instance ?: synchronized(this) {
                instance ?: AudioEngine(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}