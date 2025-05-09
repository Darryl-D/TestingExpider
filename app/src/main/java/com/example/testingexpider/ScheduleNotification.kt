package com.example.testingexpider

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

const val titleExtra = "titleExtra"
const val messageExtra = "messageExtra"

@SuppressLint("ScheduleExactAlarm")
fun scheduleNotification(context: Context, title: String, message: String, timeInMillis: Long) {
    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra(titleExtra, title)
        putExtra(messageExtra, message)
    }

    // Create a unique request code using a hash of the title and time
    val requestCode = (title + timeInMillis.toString()).hashCode()

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
    alarmManager?.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        timeInMillis,
        pendingIntent
    )
}

