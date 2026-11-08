package app.audix

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runCatching { startForegroundService(Intent(this, AudioEffectService::class.java)) }
        val engine = AudioEngine.get(this)
        setContent { AudixApp(engine) }
    }
}