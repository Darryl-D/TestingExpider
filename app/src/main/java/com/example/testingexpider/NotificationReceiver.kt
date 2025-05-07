package com.example.testingexpider

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

const val channelID = "channel1"
const val notificationID = 121
const val channelName = "Expiry Notifications"
const val channelDescription = "Notifies about upcoming food/drink expiry dates"

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(titleExtra) ?: "Expiry Alert"
        val message = intent.getStringExtra(messageExtra) ?: "An item is about to expire."

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the notification channel if needed (for Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelID, channelName, importance).apply {
                description = channelDescription
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Build and show the notification
        val notification = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.expiderlogo) //
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationID, notification)
    }
}
