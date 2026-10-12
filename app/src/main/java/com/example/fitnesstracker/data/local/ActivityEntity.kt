package com.example.fitnesstracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class ActivityEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val durationSeconds: Long,
    val distanceMeters: Double,
    val startTimeMillis: Long,
    val description: String? = null,
    val avgSpeedMps: Double = 0.0,
    val routePointsJson: String? = null
){
    fun getActivityType(): ActivityType = ActivityType.fromString(type)
}