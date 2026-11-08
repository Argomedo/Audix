package cl.diego.audiotv

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder

/** Mantiene vivo el proceso para que los efectos sigan aplicados. */
class AudioEffectService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_STICKY

    override fun onCreate() {
        super.onCreate()
        AudioEngine.get(this) // crea y aplica los efectos guardados
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel("audio", "Audio", NotificationManager.IMPORTANCE_LOW))
        val n = Notification.Builder(this, "audio")
            .setContentTitle("Audix activo")
            .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
            .build()
        runCatching {
            if (Build.VERSION.SDK_INT >= 34) {
                startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(1, n)
            }
        }
    }
}