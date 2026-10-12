package com.example.fitnesstracker.ui.stats

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.fitnesstracker.data.local.ActivityEntity
import com.example.fitnesstracker.data.local.ActivityTypeCount
import com.example.fitnesstracker.data.repository.ActivityRepository
import java.util.Calendar

class StatsViewModel(private val repository: ActivityRepository) : ViewModel() {

    // Aktivnosti u poslednjih 7 dana - za "udaljenost po danima" graf
    val activitiesLastWeek: LiveData<List<ActivityEntity>> = repository
        .getActivitiesSince(sevenDaysAgoMillis())
        .asLiveData()

    // Broj aktivnosti grupisan po tipu - za "broj aktivnosti po tipu" graf
    val activityCountByType: LiveData<List<ActivityTypeCount>> = repository
        .getActivityCountByType()
        .asLiveData()

    private fun sevenDaysAgoMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        return calendar.timeInMillis
    }
}