package cl.diego.audiotv

import android.view.KeyEvent as AKey
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Speaker
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.SurroundSound
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt


/* =========================================================================
 * NOMBRE DE LA APP  (cámbialo aquí)
 * ========================================================================= */

private const val APP_NAME = "Audix"


/* =========================================================================
 * TEMAS
 * ========================================================================= */

private class Palette(
    val name: String,
    val isDark: Boolean,
    val bg1: Color,
    val bg2: Color,
    val card: Color,
    val ink: Color,
    val cyan: Color,
    val pink: Color,
    val violet: Color,
    val amber: Color,
    val green: Color
)

private val Palettes = listOf(

    Palette(
        "Oscuro", true,
        Color(0xFF0B0F1E), Color(0xFF1B1038), Color(0xFF161C36), Color.White,
        Color(0xFF00E5FF), Color(0xFFFF3D9A), Color(0xFF9C6BFF),
        Color(0xFFFFB300), Color(0xFF00E676)
    ),

    Palette(
        "Claro", false,
        Color(0xFFF4F6FB), Color(0xFFE6EAF5), Color(0xFFFFFFFF), Color(0xFF1A1D2E),
        Color(0xFF0097B2), Color(0xFFD81B73), Color(0xFF6B3FD4),
        Color(0xFFC77700), Color(0xFF00A152)
    ),

    Palette(
        "Negro", true,
        Color(0xFF000000), Color(0xFF000000), Color(0xFF0E0E10), Color.White,
        Color(0xFF00E5FF), Color(0xFFFF3D9A), Color(0xFF9C6BFF),
        Color(0xFFFFB300), Color(0xFF00E676)
    ),

    Palette(
        "Océano", true,
        Color(0xFF06161F), Color(0xFF0B2F3F), Color(0xFF0F2A38), Color(0xFFE6F7FF),
        Color(0xFF4DD0E1), Color(0xFFFF6E9C), Color(0xFF8C9EFF),
        Color(0xFFFFCA28), Color(0xFF3DDC97)
    ),

    Palette(
        "Atardecer", true,
        Color(0xFF1F0F14), Color(0xFF3A1426), Color(0xFF2A1420), Color(0xFFFFF1E8),
        Color(0xFF7FE3E8), Color(0xFFFF5C8A), Color(0xFFC084FC),
        Color(0xFFFFB454), Color(0xFF7CE38B)
    )
)

private fun paletteOf(name: String): Palette =
    Palettes.firstOrNull { it.name == name } ?: Palettes.first()

private val LocalPalette = staticCompositionLocalOf { Palettes.first() }

// Colores que se leen del tema activo (se usan igual que antes: Cyan, Pink, etc.)
private val Cyan: Color
    @Composable @ReadOnlyComposable get() = LocalPalette.current.cyan

private val Pink: Color
    @Composable @ReadOnlyComposable get() = LocalPalette.current.pink

private val Violet: Color
    @Composable @ReadOnlyComposable get() = LocalPalette.current.violet

private val Amber: Color
    @Composable @ReadOnlyComposable get() = LocalPalette.current.amber

private val Green: Color
    @Composable @ReadOnlyComposable get() = LocalPalette.current.green

private val CardBg: Color
    @Composable @ReadOnlyComposable get() = LocalPalette.current.card

// Color de texto y de resaltado (blanco en temas oscuros, oscuro en el claro)
private val Ink: Color
    @Composable @ReadOnlyComposable get() = LocalPalette.current.ink


/* =========================================================================
 * APP
 *
 * Ancho < 600 dp (celular): diseño compacto con barra inferior.
 * Ancho >= 600 dp (TV / tablet): diseño completo de siempre.
 * ========================================================================= */

@Composable
fun AudioTvApp(e: AudioEngine) {

    val palette = paletteOf(e.theme)

    var showThemes by remember { mutableStateOf(false) }

    BackHandler(enabled = showThemes) { showThemes = false }

    CompositionLocalProvider(LocalPalette provides palette) {

        val scheme =
            if (palette.isDark) {
                darkColorScheme(
                    primary = palette.cyan,
                    surface = palette.card,
                    onSurface = palette.ink,
                    onSurfaceVariant = palette.ink.copy(alpha = 0.7f)
                )
            } else {
                lightColorScheme(
                    primary = palette.cyan,
                    surface = palette.card,
                    onSurface = palette.ink,
                    onSurfaceVariant = palette.ink.copy(alpha = 0.7f)
                )
            }

        MaterialTheme(colorScheme = scheme) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(listOf(palette.bg1, palette.bg2))
                    )
            ) {

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

                    if (maxWidth < 600.dp) {

                        CompactLayout(e, showThemes) { showThemes = !showThemes }

                    } else {

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(28.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {

                            Header(e) { showThemes = !showThemes }

                            if (showThemes) ThemesPanel(e)

                            PhoneLayout(e)

                            Text(
                                text = e.summary,
                                color = Ink.copy(alpha = 0.5f),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


/* =========================================================================
 * DISEÑO COMPACTO (CELULAR)
 * ========================================================================= */

@Composable
private fun CompactLayout(
    e: AudioEngine,
    showThemes: Boolean,
    onThemes: () -> Unit
) {

    var tab by remember { mutableStateOf(0) }

    val scroll = rememberScrollState()

    LaunchedEffect(tab) { scroll.scrollTo(0) }

    val tabs = listOf(
        "Inicio" to Icons.Rounded.VolumeUp,
        "Efectos" to Icons.Rounded.Tune,
        "EQ" to Icons.Rounded.Equalizer,
        "Perfiles" to Icons.Rounded.GraphicEq
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scroll)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Header(e, onThemes)

            if (showThemes) ThemesPanel(e)

            when (tab) {

                0 -> {
                    VolumeBoostPanel(e)
                    Presets(e)
                    Text(
                        text = e.summary,
                        color = Ink.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }

                1 -> EffectsPanel(e)

                2 -> Eq(e)

                else -> Profiles(e)
            }
        }

        NavigationBar(containerColor = CardBg) {

            tabs.forEachIndexed { index, (label, icon) ->

                NavigationBarItem(
                    selected = tab == index,
                    onClick = { tab = index },
                    icon = { Icon(imageVector = icon, contentDescription = label) },
                    label = { Text(label, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Cyan,
                        selectedTextColor = Cyan,
                        indicatorColor = Cyan.copy(alpha = 0.16f),
                        unselectedIconColor = Ink.copy(alpha = 0.6f),
                        unselectedTextColor = Ink.copy(alpha = 0.6f)
                    )
                )
            }
        }
    }
}


/* =========================================================================
 * VISTA PRINCIPAL (TV)
 * ========================================================================= */

@Composable
private fun PhoneLayout(e: AudioEngine) {

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

        VolumeBoostPanel(e)

        Presets(e)

        EffectsPanel(e)

        Eq(e)

        Profiles(e)
    }
}


/* =========================================================================
 * HEADER
 * ========================================================================= */

@Composable
private fun Header(e: AudioEngine, onThemes: () -> Unit) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = Icons.Rounded.GraphicEq,
            contentDescription = null,
            tint = Cyan,
            modifier = Modifier.size(44.dp)
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text = APP_NAME,
            style = MaterialTheme.typography.headlineLarge.copy(
                brush = Brush.horizontalGradient(listOf(Cyan, Pink)),
                fontWeight = FontWeight.ExtraBold
            ),
            modifier = Modifier.weight(1f)
        )

        // Ícono de Temas
        IconButton(onClick = onThemes) {
            Icon(
                imageVector = Icons.Rounded.Palette,
                contentDescription = "Temas",
                tint = Ink
            )
        }

        Spacer(Modifier.width(4.dp))

        Icon(
            imageVector = Icons.Rounded.PowerSettingsNew,
            contentDescription = null,
            tint = if (e.power) Green else Color.Gray
        )

        Spacer(Modifier.width(8.dp))

        Switch(
            checked = e.power,
            onCheckedChange = { value -> e.update { power = value } },
            colors = SwitchDefaults.colors(
                checkedTrackColor = Green,
                checkedThumbColor = Color.White
            )
        )
    }
}


/* =========================================================================
 * TEMAS (panel)
 * ========================================================================= */

@Composable
private fun ThemesPanel(e: AudioEngine) {

    Panel(Violet) {

        SectionHeader(
            icon = Icons.Rounded.Palette,
            title = "Temas",
            subtitle = "Elige cómo se ve la app",
            accent = Violet,
            trailing = {
                Text(
                    text = e.theme,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Violet
                )
            }
        )

        Spacer(Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(Palettes) { p ->
                ThemeCard(p, e.theme == p.name) { e.updateTheme(p.name) }
            }
        }
    }
}


@Composable
private fun ThemeCard(p: Palette, selected: Boolean, onClick: () -> Unit) {

    var focused by remember { mutableStateOf(false) }

    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier
            .width(112.dp)
            .clip(shape)
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick)
            .background(Brush.verticalGradient(listOf(p.bg1, p.bg2)))
            .border(
                width = if (focused || selected) 2.5.dp else 1.dp,
                color = when {
                    focused -> Ink
                    selected -> Cyan
                    else -> Ink.copy(alpha = 0.25f)
                },
                shape = shape
            )
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(p.cyan, p.pink, p.violet, p.green).forEach { c ->
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(c)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(p.card)
        )

        Text(
            text = if (selected) "✓ ${p.name}" else p.name,
            color = p.ink,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}


/* =========================================================================
 * PANEL BASE
 *
 * FOCUS:   fondo visible + borde del color de texto.
 * EDITING: color del efecto + borde del efecto.
 * ========================================================================= */

@Composable
private fun Panel(
    accent: Color,
    modifier: Modifier = Modifier,
    focused: Boolean = false,
    editing: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {

    val background = when {
        editing -> accent.copy(alpha = 0.16f)
        focused -> Ink.copy(alpha = 0.16f)
        else -> CardBg
    }

    val borderColor = when {
        editing -> accent
        focused -> Ink
        else -> accent.copy(alpha = 0.35f)
    }

    val borderWidth = if (editing || focused) 2.5.dp else 1.dp

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        border = BorderStroke(borderWidth, borderColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp), content = content)
    }
}


@Composable
private fun Badge(icon: ImageVector, color: Color) {

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color)
    }
}


@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    accent: Color,
    trailing: (@Composable () -> Unit)? = null
) {

    Row(verticalAlignment = Alignment.CenterVertically) {

        Badge(icon = icon, color = accent)

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {

            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ink
            )

            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Ink.copy(alpha = 0.55f)
                )
            }
        }

        if (trailing != null) trailing()
    }
}


/* =========================================================================
 * MANEJO DEL CONTROL REMOTO (compartido)
 *
 * NORMAL:  ← → ↑ ↓ = navegación,  OK = entrar en edición
 * EDICIÓN: ← = -1 paso,  → = +1 paso,  ↑ ↓ bloqueados,  BACK = salir
 * ========================================================================= */

private fun Modifier.dpadAdjust(
    enabled: Boolean,
    editing: Boolean,
    onEditing: (Boolean) -> Unit,
    onStep: (Int) -> Unit
): Modifier = onPreviewKeyEvent { event ->

    if (event.type != KeyEventType.KeyDown) {
        false
    } else {
        when (event.nativeKeyEvent.keyCode) {

            AKey.KEYCODE_DPAD_CENTER,
            AKey.KEYCODE_ENTER -> {
                if (enabled && !editing) {
                    onEditing(true)
                    true
                } else {
                    editing
                }
            }

            AKey.KEYCODE_DPAD_LEFT,
            AKey.KEYCODE_DPAD_RIGHT -> {
                if (!editing) {
                    false
                } else {
                    onStep(
                        if (event.nativeKeyEvent.keyCode == AKey.KEYCODE_DPAD_LEFT) -1 else 1
                    )
                    true
                }
            }

            AKey.KEYCODE_DPAD_UP,
            AKey.KEYCODE_DPAD_DOWN -> editing

            else -> false
        }
    }
}


/* =========================================================================
 * VOLUMEN+
 *
 * 100% = volumen normal.  200% = +6 dB.  300% = +9.5 dB.
 * Barra de 24 segmentos: verde (limpio) -> ámbar (alto) -> rosado (máximo).
 * ========================================================================= */

@Composable
private fun VolumeBoostPanel(e: AudioEngine) {

    var focused by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf(false) }

    BackHandler(enabled = editing) { editing = false }

    val pct = e.volumeBoost

    val accent = when {
        pct < 167 -> Green
        pct < 234 -> Amber
        else -> Pink
    }

    val zone = when {
        pct == 100 -> "Sin refuerzo"
        pct < 167 -> "Limpio"
        pct < 234 -> "Alto"
        else -> "Máximo • el limitador trabaja más"
    }

    val segments = 24
    val filled = (((pct - 100) / 200f) * segments).roundToInt()

    Panel(
        accent = accent,
        focused = focused,
        editing = editing,
        modifier = Modifier
            .onFocusChanged { state ->
                focused = state.isFocused
                if (!state.isFocused) editing = false
            }
            .focusable(e.power)
            .dpadAdjust(
                enabled = e.power,
                editing = editing,
                onEditing = { editing = it },
                onStep = { d ->
                    val next = (pct + d * 5).coerceIn(100, 300)
                    if (next != pct) e.update { volumeBoost = next }
                }
            )
    ) {

        SectionHeader(
            icon = Icons.Rounded.VolumeUp,
            title = "Volumen+",
            subtitle = "Sube el volumen por encima del normal",
            accent = accent,
            trailing = {
                Text(
                    text = "$pct%",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accent
                )
            }
        )

        Spacer(Modifier.height(14.dp))

        // Barra segmentada
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {

            repeat(segments) { i ->

                val segColor = when {
                    i < 8 -> Green
                    i < 16 -> Amber
                    else -> Pink
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (i < filled && e.power) segColor
                            else Ink.copy(alpha = 0.08f)
                        )
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = zone,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = if (pct == 100) "0 dB" else "+%.1f dB".format(e.boostDb),
                fontSize = 13.sp,
                color = Ink.copy(alpha = 0.7f)
            )
        }

        Slider(
            value = pct.toFloat(),
            onValueChange = { v ->
                val next = ((v / 5f).roundToInt() * 5).coerceIn(100, 300)
                if (next != pct) e.update { volumeBoost = next }
            },
            valueRange = 100f..300f,
            steps = 39,
            enabled = e.power,
            modifier = Modifier
                .fillMaxWidth()
                .focusProperties { canFocus = false },
            colors = SliderDefaults.colors(
                thumbColor = accent,
                activeTrackColor = accent,
                inactiveTrackColor = accent.copy(alpha = 0.2f)
            )
        )

        Text(
            text = when {
                editing -> "AJUSTANDO  •  ← / → cambian 5%  •  BACK para salir"
                focused -> "SELECCIONADO  •  OK para ajustar"
                !e.advancedDspAvailable ->
                    "Este equipo no soporta limitador: sobre 200% puede distorsionar"
                else ->
                    "Limitador automático activo para evitar distorsión"
            },
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (editing) accent else Ink.copy(alpha = 0.6f)
        )
    }
}


/* =========================================================================
 * SLIDER DE EFECTO
 * ========================================================================= */

@Composable
private fun FxSlider(
    icon: ImageVector,
    label: String,
    value: Float,
    color: Color,
    enabled: Boolean,
    onChange: (Int) -> Unit
) {

    var focused by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf(false) }

    BackHandler(enabled = editing) { editing = false }

    Panel(
        accent = color,
        focused = focused,
        editing = editing,
        modifier = Modifier
            // onFocusChanged ANTES de focusable
            .onFocusChanged { state ->
                focused = state.isFocused
                if (!state.isFocused) editing = false
            }
            .focusable(enabled)
            .dpadAdjust(
                enabled = enabled,
                editing = editing,
                onEditing = { editing = it },
                onStep = { d ->
                    val current = value.roundToInt()
                    val next = (current + d).coerceIn(0, 100)
                    if (next != current) onChange(next)
                }
            )
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {

            Badge(icon, color)

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink
                )

                if (editing) {
                    Text(
                        text = "AJUSTANDO  •  ← / →  •  BACK para salir",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                } else if (focused) {
                    Text(
                        text = "SELECCIONADO  •  OK para ajustar",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                }
            }

            Text(
                text = "${value.roundToInt()}%",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        // Sigue funcionando con táctil, pero no recibe foco del D-pad
        Slider(
            value = value,
            onValueChange = { newValue -> onChange(newValue.roundToInt()) },
            valueRange = 0f..100f,
            steps = 99,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .focusProperties { canFocus = false },
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = color.copy(alpha = 0.2f)
            )
        )
    }
}


/* =========================================================================
 * EFECTOS
 *
 * Ancho >= 500 dp: 2 por fila (TV).  Menos: 1 por fila (celular).
 * ========================================================================= */

private class Fx(
    val icon: ImageVector,
    val label: String,
    val value: Int,
    val color: Color,
    val onSet: AudioEngine.(Int) -> Unit
)

@Composable
private fun EffectsPanel(e: AudioEngine) {

    val fxs = listOf(
        Fx(Icons.Rounded.Speaker, "Bass Boost", e.bass, Pink) { bass = it },
        Fx(Icons.Rounded.VolumeUp, "Loudness", e.loudness, Amber) { loudness = it },
        Fx(Icons.Rounded.Vibration, "Bass Stabilizer", e.bassStabilizer, Cyan) { bassStabilizer = it },
        Fx(Icons.Rounded.RecordVoiceOver, "Vocal Clarity", e.vocalClarity, Violet) { vocalClarity = it },
        Fx(Icons.Rounded.Speed, "Smart Volume", e.smartVolume, Green) { smartVolume = it },
        Fx(Icons.Rounded.SurroundSound, "Stereo Wide", e.surround, Violet) { surround = it },
        Fx(Icons.Rounded.Security, "Limiter", e.limiter, Pink) { limiter = it }
    )

    Panel(Cyan) {

        SectionHeader(
            icon = Icons.Rounded.Speaker,
            title = "Efectos",
            subtitle = "Procesamiento de audio",
            accent = Cyan
        )

        Spacer(Modifier.height(12.dp))

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {

            val cols = if (maxWidth >= 500.dp) 2 else 1

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                fxs.chunked(cols).forEach { row ->

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {

                        row.forEach { fx ->

                            Box(modifier = Modifier.weight(1f)) {

                                FxSlider(
                                    fx.icon,
                                    fx.label,
                                    fx.value.toFloat(),
                                    fx.color,
                                    e.power
                                ) { value ->
                                    e.update {
                                        fx.onSet(this, value)
                                        preset = "Personalizado"
                                    }
                                }
                            }
                        }

                        if (row.size < cols) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}


/* =========================================================================
 * PRESETS
 * ========================================================================= */

@Composable
private fun Presets(e: AudioEngine) {

    Panel(Violet) {

        SectionHeader(
            icon = Icons.Rounded.Tune,
            title = "Presets",
            subtitle = "Configuraciones rápidas",
            accent = Violet,
            trailing = {
                Text(
                    text = e.preset,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Violet
                )
            }
        )

        Spacer(Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

            items(Presets.names) { name ->

                FilterChip(
                    selected = e.preset == name,
                    onClick = { e.usePreset(name) },
                    enabled = e.power,
                    label = { Text(name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Violet.copy(alpha = 0.25f),
                        selectedLabelColor = Violet
                    )
                )
            }

            item {

                FilterChip(
                    selected = e.preset == "Personalizado",
                    onClick = {
                        if (e.hasCustomPreset()) e.loadCustomPreset()
                    },
                    enabled = e.power,
                    label = { Text("Personalizado") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Cyan.copy(alpha = 0.25f),
                        selectedLabelColor = Cyan
                    )
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "Guardar configuración actual",
                fontSize = 12.sp,
                color = Ink.copy(alpha = 0.5f),
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = { e.saveCustomPreset() },
                enabled = e.power,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Violet.copy(alpha = 0.22f),
                    contentColor = Ink
                )
            ) {
                Text("Guardar")
            }
        }
    }
}


/* =========================================================================
 * ECUALIZADOR
 * ========================================================================= */

@Composable
private fun Eq(e: AudioEngine) {

    Panel(Green) {

        SectionHeader(
            icon = Icons.Rounded.Equalizer,
            title = "Ecualizador",
            subtitle = "Frecuencias y ganancia",
            accent = Green,
            trailing = {
                Text(
                    text = e.preset,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Green
                )
            }
        )

        Spacer(Modifier.height(10.dp))

        e.freqs.forEachIndexed { index, frequency ->
            EqBand(e = e, index = index, frequency = frequency)
        }

        if (e.hzEditable) {
            Text(
                text = "− / + cambian la frecuencia • OK sobre el slider ajusta la ganancia",
                fontSize = 12.sp,
                color = Ink.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}


/* =========================================================================
 * BANDA EQ
 *
 * El contenedor completo es focusable.
 * EDICIÓN: OK entra, ← / → = -/+ 1 dB, BACK sale.
 * Los botones -/+ y el Slider no roban el foco.
 * ========================================================================= */

@Composable
private fun EqBand(e: AudioEngine, index: Int, frequency: Int) {

    var focused by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf(false) }

    BackHandler(enabled = editing) { editing = false }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            // onFocusChanged DEBE estar antes de focusable
            .onFocusChanged { state ->
                focused = state.isFocused
                if (!state.isFocused) editing = false
            }
            .focusable(e.power)
            .dpadAdjust(
                enabled = e.power,
                editing = editing,
                onEditing = { editing = it },
                onStep = { d ->
                    val current = e.gains[index].roundToInt()
                    val next = (current + d).coerceIn(-12, 12)
                    if (next != current) e.setGain(index, next.toFloat())
                }
            )
            .then(
                if (focused || editing) {
                    Modifier
                        .background(
                            color = if (editing) Green.copy(alpha = 0.16f)
                            else Ink.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .border(
                            width = 2.5.dp,
                            color = if (editing) Green else Ink,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                } else {
                    Modifier
                }
            )
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (e.hzEditable) {
                IconButton(
                    onClick = { e.nudgeFreq(index, false) },
                    enabled = e.power,
                    modifier = Modifier.focusProperties { canFocus = false }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Remove,
                        contentDescription = "Reducir frecuencia",
                        tint = Ink
                    )
                }
            }

            Text(
                text = if (frequency >= 1000) "%.1f kHz".format(frequency / 1000f)
                else "$frequency Hz",
                color = Ink,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(68.dp)
            )

            if (e.hzEditable) {
                IconButton(
                    onClick = { e.nudgeFreq(index, true) },
                    enabled = e.power,
                    modifier = Modifier.focusProperties { canFocus = false }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Aumentar frecuencia",
                        tint = Ink
                    )
                }
            }

            Slider(
                value = e.gains[index],
                onValueChange = { value ->
                    e.setGain(index, value.roundToInt().toFloat())
                },
                valueRange = -12f..12f,
                steps = 23,
                enabled = e.power,
                modifier = Modifier
                    .weight(1f)
                    .focusProperties { canFocus = false },
                colors = SliderDefaults.colors(
                    thumbColor = Green,
                    activeTrackColor = Green,
                    inactiveTrackColor = Green.copy(alpha = 0.2f)
                )
            )

            Text(
                text = "${e.gains[index].roundToInt()} dB",
                color = Green,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(52.dp),
                textAlign = TextAlign.End
            )
        }

        if (editing) {
            Text(
                text = "AJUSTANDO GANANCIA  •  ← / →  •  BACK para salir",
                color = Green,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp),
                textAlign = TextAlign.Center
            )
        } else if (focused) {
            Text(
                text = "SELECCIONADO  •  OK para ajustar ganancia",
                color = Ink,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}


/* =========================================================================
 * PERFILES
 * ========================================================================= */

@Composable
private fun Profiles(e: AudioEngine) {

    var newName by remember { mutableStateOf("") }

    Panel(Pink) {

        SectionHeader(
            icon = Icons.Rounded.GraphicEq,
            title = "Perfiles",
            subtitle = "Configuraciones completas guardadas",
            accent = Pink,
            trailing = {
                Text(
                    text = "${e.profiles.size}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Pink
                )
            }
        )

        Spacer(Modifier.height(12.dp))

        if (e.profiles.isEmpty()) {

            Text(
                text = "No hay perfiles guardados todavía.",
                color = Ink.copy(alpha = 0.55f),
                fontSize = 13.sp
            )

        } else {

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(e.profiles) { profile ->
                    ProfileItem(e = e, profile = profile)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Nombre del perfil") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = {
                    if (newName.isNotBlank()) {
                        e.saveProfile(newName.trim())
                        newName = ""
                    }
                },
                enabled = e.power,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Pink.copy(alpha = 0.22f),
                    contentColor = Ink
                )
            ) {
                Text("Guardar")
            }
        }
    }
}


@Composable
private fun ProfileItem(e: AudioEngine, profile: Profile) {

    val active = e.activeProfile == profile.name

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (active) Pink.copy(alpha = 0.10f)
            else Ink.copy(alpha = 0.025f)
        ),
        border = BorderStroke(
            1.dp,
            if (active) Pink.copy(alpha = 0.55f)
            else Ink.copy(alpha = 0.08f)
        )
    ) {

        Column(
            modifier = Modifier
                .width(300.dp)
                .padding(12.dp)
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Column(modifier = Modifier.weight(1f)) {

                    Text(
                        text = profile.name,
                        color = Ink,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (active) {
                        Text(text = "Activo", color = Pink, fontSize = 11.sp)
                    }
                }

                Button(
                    onClick = { e.loadProfile(profile) },
                    enabled = e.power,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Cyan.copy(alpha = 0.18f),
                        contentColor = Cyan
                    )
                ) {
                    Text("Cargar")
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                ProfileValue("Bass", profile.bass, Pink)
                ProfileValue("Loud", profile.loud, Amber)
                ProfileValue("Wide", profile.surr, Violet)
                ProfileValue("Vol", profile.volumeBoost, Green)
            }

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {

                ProfileButton("Duplicar") { e.duplicateProfile(profile.name) }
                ProfileButton("↑") { e.moveProfile(profile.name, -1) }
                ProfileButton("↓") { e.moveProfile(profile.name, 1) }
                ProfileButton("Eliminar") { e.deleteProfile(profile.name) }
            }
        }
    }
}


@Composable
private fun ProfileValue(label: String, value: Int, color: Color) {

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(7.dp))
            .background(color.copy(alpha = 0.10f))
            .padding(horizontal = 7.dp, vertical = 4.dp)
    ) {
        Text(text = "$label $value%", color = color, fontSize = 10.sp)
    }
}


@Composable
private fun ProfileButton(text: String, onClick: () -> Unit) {

    OutlinedButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
        modifier = Modifier.height(30.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Ink.copy(alpha = 0.18f))
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            color = Ink.copy(alpha = 0.75f)
        )
    }
}