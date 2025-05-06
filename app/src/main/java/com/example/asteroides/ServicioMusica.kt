package com.example.asteroides

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat

class ServicioMusica : Service() {

    private lateinit var reproductor: MediaPlayer
    private lateinit var notificationManager: NotificationManager
    private val CANAL_ID = "mi_canal"
    private val NOTIFICACION_ID = 1

    override fun onCreate() {
        Toast.makeText(this, "Servicio creado", Toast.LENGTH_SHORT).show()
        reproductor = MediaPlayer.create(this, R.raw.audio)

        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CANAL_ID,
                "Mis Notificaciones",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.description = "Descripcion del canal"
            notificationManager.createNotificationChannel(notificationChannel)

            val intent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notificacion = NotificationCompat.Builder(this, CANAL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Titulo")
                .setContentText("Texto de la notificación.")
                .setContentIntent(pendingIntent)
            notificationManager.notify(NOTIFICACION_ID, notificacion.build())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(this, "Servicio arrancado $startId", Toast.LENGTH_SHORT).show()
        reproductor.start()
        return START_STICKY
    }

    override fun onDestroy() {
        Toast.makeText(this, "Servicio detenido", Toast.LENGTH_SHORT).show()
        reproductor.stop()
        notificationManager.cancel(NOTIFICACION_ID)

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
