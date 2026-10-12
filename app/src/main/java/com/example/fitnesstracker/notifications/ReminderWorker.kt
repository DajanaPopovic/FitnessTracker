package com.example.fitnesstracker.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.fitnesstracker.FitnessTrackerApp
import com.example.fitnesstracker.MainActivity
import com.example.fitnesstracker.R
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        val INACTIVITY_THRESHOLD_MILLIS = TimeUnit.HOURS.toMillis(24)
        const val NOTIFICATION_ID_REMINDER = 2001
    }

    override suspend fun doWork(): Result {
        val app = applicationContext as FitnessTrackerApp

        val prefs = applicationContext.getSharedPreferences(
            com.example.fitnesstracker.ui.settings.SettingsKeys.PREFS_NAME, Context.MODE_PRIVATE
        )
        val notificationsEnabled = prefs.getBoolean(
            com.example.fitnesstracker.ui.settings.SettingsKeys.KEY_NOTIFICATIONS_ENABLED, true
        )
        if (!notificationsEnabled) return Result.success()

        val activities = app.activityRepository.getAllActivities().first()

        val lastActivityTime = activities.maxOfOrNull { it.startTimeMillis }
        val now = System.currentTimeMillis()

        val isInactive = lastActivityTime == null || (now - lastActivityTime) >= INACTIVITY_THRESHOLD_MILLIS

        if (isInactive) {
            sendReminderNotification()
        }

        return Result.success()
    }

    private fun sendReminderNotification() {

        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                applicationContext, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        if (!hasPermission) return

        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0,
            android.content.Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, NotificationHelper.CHANNEL_REMINDER)
            .setContentTitle(applicationContext.getString(R.string.reminder_notification_title))
            .setContentText(applicationContext.getString(R.string.reminder_notification_text))
            .setSmallIcon(R.drawable.ic_nav_home)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(NOTIFICATION_ID_REMINDER, notification)
    }
}