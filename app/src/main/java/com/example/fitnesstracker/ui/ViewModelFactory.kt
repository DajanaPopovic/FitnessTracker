package com.example.fitnesstracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstracker.data.repository.ActivityRepository
import com.example.fitnesstracker.data.repository.GoalRepository
import com.example.fitnesstracker.ui.goals.GoalsViewModel
import com.example.fitnesstracker.ui.history.HistoryViewModel
import com.example.fitnesstracker.ui.home.HomeViewModel
import com.example.fitnesstracker.ui.stats.StatsViewModel

/**
 * Jedna zajednička Factory klasa za SVE ViewModele u aplikaciji.
 * Kad god fragment/activity zatraži ViewModel preko ove fabrike,
 * ona "zna" koji repository (ili repozitorije) taj ViewModel treba,
 * i ručno ga prosljeđuje kroz konstruktor.
 */
class ViewModelFactory(
    private val activityRepository: ActivityRepository,
    private val goalRepository: GoalRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(activityRepository) as T

            modelClass.isAssignableFrom(HistoryViewModel::class.java) ->
                HistoryViewModel(activityRepository) as T

            modelClass.isAssignableFrom(StatsViewModel::class.java) ->
                StatsViewModel(activityRepository) as T

            modelClass.isAssignableFrom(GoalsViewModel::class.java) ->
                GoalsViewModel(goalRepository, activityRepository) as T

            else -> throw IllegalArgumentException("Nepoznata ViewModel klasa: ${modelClass.name}")
        }
    }
}