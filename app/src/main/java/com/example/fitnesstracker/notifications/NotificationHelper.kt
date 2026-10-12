package com.example.fitnesstracker.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationHelper {

    const val CHANNEL_TRACKING = "tracking_channel"
    const val CHANNEL_REMINDER = "reminder_channel"

    const val NOTIFICATION_ID_TRACKING = 1001

    fun createChannels(context: Context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)

            val trackingChannel = NotificationChannel(
                CHANNEL_TRACKING,
                "Praćenje aktivnosti",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Prikazuje se dok je aktivnost u toku"
            }

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDER,
                "Podsjetnici za aktivnost",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Podsjeća te da budeš fizički aktivan/na"
            }

            manager.createNotificationChannel(trackingChannel)
            manager.createNotificationChannel(reminderChannel)
        }
    }
}