package com.example.fitnesstracker.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.data.local.ActivityEntity
import com.example.fitnesstracker.data.repository.ActivityRepository
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: ActivityRepository) : ViewModel() {

    private val queryState = MutableLiveData<QueryState>(QueryState.NoFilter)

    val activities: LiveData<List<ActivityEntity>> = queryState.switchMap { state ->
        when (state) {
            is QueryState.NoFilter -> repository.getAllActivities().asLiveData()
            is QueryState.Search -> repository.searchByDescription(state.query).asLiveData()
            is QueryState.Filter -> repository.filterActivities(
                type = state.type,
                startDate = state.startDate,
                endDate = state.endDate,
                minDurationSeconds = state.minDurationSeconds
            ).asLiveData()
        }
    }

    fun searchByDescription(query: String) {
        queryState.value = QueryState.Search(query)
    }

    fun applyFilter(
        type: String? = null,
        startDate: Long? = null,
        endDate: Long? = null,
        minDurationSeconds: Long? = null
    ) {
        queryState.value = QueryState.Filter(type, startDate, endDate, minDurationSeconds)
    }

    fun clearFilter() {
        queryState.value = QueryState.NoFilter
    }

    fun deleteActivity(activity: ActivityEntity) {
        viewModelScope.launch {
            repository.delete(activity)
        }
    }
}

/**
 * Predstavlja TRENUTNI način prikaza liste - ili sve, ili pretraga, ili filter.
 * Sealed class garantuje da su ovo JEDINE tri moguće opcije (compiler provjerava
 * da smo pokrili sve slučajeve u "when" bloku iznad).
 */
sealed class QueryState {
    object NoFilter : QueryState()
    data class Search(val query: String) : QueryState()
    data class Filter(
        val type: String?,
        val startDate: Long?,
        val endDate: Long?,
        val minDurationSeconds: Long?
    ) : QueryState()
}