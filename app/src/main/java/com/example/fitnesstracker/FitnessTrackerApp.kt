package com.example.fitnesstracker

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.fitnesstracker.data.local.AppDatabase
import com.example.fitnesstracker.data.repository.ActivityRepository
import com.example.fitnesstracker.data.repository.GoalRepository
import com.example.fitnesstracker.notifications.NotificationHelper
import com.example.fitnesstracker.notifications.ReminderWorker
import java.util.concurrent.TimeUnit

class FitnessTrackerApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    val activityRepository: ActivityRepository by lazy {
        ActivityRepository(database.activityDao())
    }

    val goalRepository: GoalRepository by lazy {
        GoalRepository(database.goalDao())
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
        scheduleReminderWork()
    }


    private fun scheduleReminderWork() {
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "fitness_tracker_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}