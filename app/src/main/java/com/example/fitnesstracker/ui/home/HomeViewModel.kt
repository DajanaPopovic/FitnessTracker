package com.example.fitnesstracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.data.local.ActivityEntity
import com.example.fitnesstracker.data.repository.ActivityRepository
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: ActivityRepository) : ViewModel() {

    fun saveActivity(activity: ActivityEntity) {
        viewModelScope.launch {
            repository.insert(activity)
        }
    }
}