package com.tvstorage.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.tvstorage.app.MainActivity
import com.tvstorage.app.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TimerService : Service() {

    companion object {
        const val CHANNEL_ID = "tv_timer_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "com.tvstorage.app.STOP_TIMER"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("TV Storage")
            .setContentText("Приложение работает в фоновом режиме")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "TV Timer",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Уведомления таймера TV Storage"
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}