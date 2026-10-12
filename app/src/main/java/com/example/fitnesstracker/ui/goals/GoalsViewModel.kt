package com.example.fitnesstracker.ui.goals

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.data.local.ActivityEntity
import com.example.fitnesstracker.data.local.GoalEntity
import com.example.fitnesstracker.data.repository.ActivityRepository
import com.example.fitnesstracker.data.repository.GoalRepository
import kotlinx.coroutines.launch

/**
 * ViewModel za Ciljeve - upravlja definisanjem ciljeva i računanjem napretka.
 * Prima DVA repozitorija: GoalRepository (za same ciljeve) i ActivityRepository
 * (jer napredak računamo dinamički na osnovu upisanih aktivnosti).
 */
class GoalsViewModel(
    private val goalRepository: GoalRepository,
    private val activityRepository: ActivityRepository
) : ViewModel() {

    val activeGoals: LiveData<List<GoalEntity>> = goalRepository
        .getActiveGoals()
        .asLiveData()

    // Sve aktivnosti - koristićemo ih da izračunamo napredak po cilju u Fragmentu/Adapteru
    val allActivities: LiveData<List<ActivityEntity>> = activityRepository
        .getAllActivities()
        .asLiveData()

    fun saveGoal(goal: GoalEntity) {
        viewModelScope.launch {
            goalRepository.insert(goal)
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            goalRepository.delete(goal)
        }
    }
}