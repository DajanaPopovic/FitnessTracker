package com.example.fitnesstracker.tracking

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.fitnesstracker.MainActivity
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.local.LatLngPoint
import com.example.fitnesstracker.notifications.NotificationHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel

class ActivityTrackingService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private val serviceScope = CoroutineScope(SupervisorJob())
    private var timerJob: Job? = null

    private var lastLocation: Location? = null
    private var startTimeMillis: Long = 0L

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTracking()
            ACTION_STOP -> stopTracking()
        }
        return START_STICKY
    }

    private fun startTracking() {
        TrackingState.start()
        startTimeMillis = System.currentTimeMillis()

        startForeground(NotificationHelper.NOTIFICATION_ID_TRACKING, buildNotification())

        startLocationUpdates()
        startTimer()
    }

    private fun startLocationUpdates() {

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            3000L // interval - novi podatak svake 3 sekunde
        ).setMinUpdateIntervalMillis(2000L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                handleNewLocation(location)
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback as LocationCallback,
                mainLooper
            )
        } catch (e: SecurityException) {
            stopTracking()
        }
    }

    private fun handleNewLocation(location: Location) {
        // FILTER 1: Ignoriši tačke sa izuzetno lošom preciznošću (većom od 25 metara)
        if (location.hasAccuracy() && location.accuracy > 25f) {
            return
        }

        val previous = lastLocation

        if (previous != null) {
            // Izračunaj vremensku razliku u sekundama između dvije lokacije
            val timeDeltaSeconds = (location.time - previous.time) / 1000.0
            val addedDistance = previous.distanceTo(location).toDouble()

            // FILTER 2:
            // Ako je trenutna brzina između 2 tačke veća od 50 m/s (~180 km/h), ignorišemo skok
            if (timeDeltaSeconds > 0) {
                val calculatedSpeedMps = addedDistance / timeDeltaSeconds
                if (calculatedSpeedMps > 50.0) {
                    // Ažuriramo lastLocation da ne bismo stalno poredili sa starom tačkom,
                    // ali NE dodajemo nerealnu distancu u radni zapis!
                    lastLocation = location
                    return
                }
            }

            val point = LatLngPoint(
                latitude = location.latitude,
                longitude = location.longitude,
                timestampMillis = System.currentTimeMillis()
            )

            TrackingState.addPoint(point, addedDistance)
        } else {
            val point = LatLngPoint(location.latitude, location.longitude, System.currentTimeMillis())
            TrackingState.addPoint(point, 0.0)
        }

        TrackingState.updateSpeed(if (location.hasSpeed()) location.speed.toDouble() else 0.0)

        lastLocation = location
    }

    private fun startTimer() {
        timerJob = serviceScope.launch {
            while (true) {
                delay(1000L)
                val elapsed = (System.currentTimeMillis() - startTimeMillis) / 1000
                TrackingState.updateElapsed(elapsed)
                updateNotification(elapsed)
            }
        }
    }

    private fun stopTracking() {
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        timerJob?.cancel()
        TrackingState.stop()
        lastLocation = null

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NotificationHelper.CHANNEL_TRACKING)
            .setContentTitle("Aktivnost se prati")
            .setContentText("00:00 - praćenje u toku")
            .setSmallIcon(R.drawable.ic_nav_stats)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(elapsedSeconds: Long) {
        val minutes = elapsedSeconds / 60
        val seconds = elapsedSeconds % 60
        val timeText = String.format("%02d:%02d", minutes, seconds)

        val notification = NotificationCompat.Builder(this, NotificationHelper.CHANNEL_TRACKING)
            .setContentTitle("Aktivnost se prati")
            .setContentText("$timeText - praćenje u toku")
            .setSmallIcon(R.drawable.ic_nav_stats)
            .setOngoing(true)
            .build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(NotificationHelper.NOTIFICATION_ID_TRACKING, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}