package com.example.fitnesstracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "goals")
data class GoalEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val activityType: String? = null,

    val metric: String,

    val period: String,

    val targetValue: Double,

    val isActive: Boolean = true
) {
    fun getMetric(): GoalMetric = GoalMetric.valueOf(metric)
    fun getPeriod(): GoalPeriod = GoalPeriod.valueOf(period)
    fun getActivityTypeEnum(): ActivityType? =
        activityType?.let { ActivityType.fromString(it) }
}