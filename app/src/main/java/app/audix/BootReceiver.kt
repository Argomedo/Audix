package app.audix

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        runCatching {
            context.startForegroundService(Intent(context, AudioEffectService::class.java))
        }
    }
}
