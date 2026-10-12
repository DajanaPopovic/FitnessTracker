package com.example.fitnesstracker.tracking

import com.example.fitnesstracker.data.local.LatLngPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


object TrackingState {

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    private val _distanceMeters = MutableStateFlow(0.0)
    val distanceMeters: StateFlow<Double> = _distanceMeters.asStateFlow()

    private val _currentSpeedMps = MutableStateFlow(0.0)
    val currentSpeedMps: StateFlow<Double> = _currentSpeedMps.asStateFlow()

    private val _routePoints = MutableStateFlow<List<LatLngPoint>>(emptyList())
    val routePoints: StateFlow<List<LatLngPoint>> = _routePoints.asStateFlow()

    fun start() {
        _isTracking.value = true
        _elapsedSeconds.value = 0L
        _distanceMeters.value = 0.0
        _currentSpeedMps.value = 0.0
        _routePoints.value = emptyList()
    }

    fun updateElapsed(seconds: Long) {
        _elapsedSeconds.value = seconds
    }

    fun updateSpeed(speedMps: Double) {
        _currentSpeedMps.value = speedMps
    }

    fun addPoint(point: LatLngPoint, addedDistanceMeters: Double) {
        _routePoints.value = _routePoints.value + point
        _distanceMeters.value = _distanceMeters.value + addedDistanceMeters
    }

    fun stop() {
        _isTracking.value = false
    }

    fun reset() {
        _isTracking.value = false
        _elapsedSeconds.value = 0L
        _distanceMeters.value = 0.0
        _currentSpeedMps.value = 0.0
        _routePoints.value = emptyList()
    }
}