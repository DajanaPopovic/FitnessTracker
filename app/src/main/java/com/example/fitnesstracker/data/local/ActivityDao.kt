package com.example.fitnesstracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
@Dao
interface ActivityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activity: ActivityEntity): Long
    @Update
    suspend fun update(activity: ActivityEntity)
    @Delete
    suspend fun delete(activity: ActivityEntity)
    @Query("SELECT * FROM activities ORDER BY startTimeMillis DESC")
    fun getAllActivities(): Flow<List<ActivityEntity>>
    @Query("SELECT * FROM activities WHERE id = :activityId")
    fun getActivityById(activityId: Long): Flow<ActivityEntity?>

    @Query("""
        SELECT * FROM activities 
        WHERE (:type IS NULL OR type = :type)
        AND (:startDate IS NULL OR startTimeMillis >= :startDate)
        AND (:endDate IS NULL OR startTimeMillis <= :endDate)
        AND (:minDurationSeconds IS NULL OR durationSeconds >= :minDurationSeconds)
        ORDER BY startTimeMillis DESC
    """)
    fun filterActivities(
        type: String?,
        startDate: Long?,
        endDate: Long?,
        minDurationSeconds: Long?
    ): Flow<List<ActivityEntity>>
    @Query("SELECT * FROM activities WHERE description LIKE '%' || :query || '%' ORDER BY startTimeMillis DESC")
    fun searchByDescription(query: String): Flow<List<ActivityEntity>>
    @Query("""
        SELECT * FROM activities 
        WHERE startTimeMillis >= :sinceMillis 
        ORDER BY startTimeMillis ASC
    """)
    fun getActivitiesSince(sinceMillis: Long): Flow<List<ActivityEntity>>
    @Query("SELECT type, COUNT(*) as count FROM activities GROUP BY type")
    fun getActivityCountByType(): Flow<List<ActivityTypeCount>>
}

data class ActivityTypeCount(
    val type: String,
    val count: Int
)