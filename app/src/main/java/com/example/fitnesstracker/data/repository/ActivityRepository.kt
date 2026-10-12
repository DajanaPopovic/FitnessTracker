package com.example.fitnesstracker.data.repository

import com.example.fitnesstracker.data.local.ActivityDao
import com.example.fitnesstracker.data.local.ActivityEntity
import com.example.fitnesstracker.data.local.ActivityTypeCount
import com.example.fitnesstracker.data.local.Converters
import com.example.fitnesstracker.data.local.LatLngPoint
import kotlinx.coroutines.flow.Flow
class ActivityRepository(private val activityDao: ActivityDao) {

    private val converters = Converters()
    fun getAllActivities(): Flow<List<ActivityEntity>> = activityDao.getAllActivities()
    fun getActivityById(id: Long): Flow<ActivityEntity?> = activityDao.getActivityById(id)

    fun filterActivities(
        type: String?,
        startDate: Long?,
        endDate: Long?,
        minDurationSeconds: Long?
    ): Flow<List<ActivityEntity>> =
        activityDao.filterActivities(type, startDate, endDate, minDurationSeconds)

    fun searchByDescription(query: String): Flow<List<ActivityEntity>> =
        activityDao.searchByDescription(query)

    fun getActivitiesSince(sinceMillis: Long): Flow<List<ActivityEntity>> =
        activityDao.getActivitiesSince(sinceMillis)

    fun getActivityCountByType(): Flow<List<ActivityTypeCount>> =
        activityDao.getActivityCountByType()

    suspend fun insert(activity: ActivityEntity): Long = activityDao.insert(activity)

    suspend fun update(activity: ActivityEntity) = activityDao.update(activity)

    suspend fun delete(activity: ActivityEntity) = activityDao.delete(activity)
    fun encodeRoute(points: List<LatLngPoint>): String =
        converters.fromLatLngPointList(points) ?: "[]"

    fun decodeRoute(json: String?): List<LatLngPoint> =
        converters.toLatLngPointList(json) ?: emptyList()
}