package com.example.fitnesstracker.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.fitnesstracker.data.local.ActivityEntity
import com.example.fitnesstracker.data.repository.ActivityRepository
import androidx.lifecycle.ViewModelProvider

class DetailViewModel(
    private val repository: ActivityRepository,
    private val activityId: Long
) : ViewModel() {

    val activity: LiveData<ActivityEntity?> = repository
        .getActivityById(activityId)
        .asLiveData()
}


class DetailViewModelFactory(
    private val repository: ActivityRepository,
    private val activityId: Long
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return DetailViewModel(repository, activityId) as T
    }
}