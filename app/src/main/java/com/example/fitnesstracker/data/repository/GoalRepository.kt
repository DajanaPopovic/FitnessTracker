package com.example.fitnesstracker.data.repository

import com.example.fitnesstracker.data.local.GoalDao
import com.example.fitnesstracker.data.local.GoalEntity
import kotlinx.coroutines.flow.Flow

class GoalRepository(private val goalDao: GoalDao) {

    fun getActiveGoals(): Flow<List<GoalEntity>> = goalDao.getActiveGoals()

    fun getGoalById(id: Long): Flow<GoalEntity?> = goalDao.getGoalById(id)
    suspend fun insert(goal: GoalEntity): Long = goalDao.insert(goal)

    suspend fun update(goal: GoalEntity) = goalDao.update(goal)
    suspend fun delete(goal: GoalEntity) = goalDao.delete(goal)
}