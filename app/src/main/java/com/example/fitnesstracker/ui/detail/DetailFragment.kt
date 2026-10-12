package com.example.fitnesstracker.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.fitnesstracker.FitnessTrackerApp
import com.example.fitnesstracker.data.local.ActivityEntity
import com.example.fitnesstracker.data.local.ActivityType
import com.example.fitnesstracker.data.local.LatLngPoint
import com.example.fitnesstracker.databinding.FragmentDetailBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.fitnesstracker.R

class DetailFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private val activityId: Long by lazy {
        arguments?.getLong("activityId") ?: -1L
    }

    private val viewModel: DetailViewModel by viewModels {
        val app = requireActivity().application as FitnessTrackerApp
        DetailViewModelFactory(app.activityRepository, activityId)
    }

    private var googleMap: GoogleMap? = null
    private var pendingRoutePoints: List<LatLngPoint>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(com.example.fitnesstracker.R.id.map_fragment)
                as SupportMapFragment
        mapFragment.getMapAsync(this)

        observeActivity()
    }

    private fun observeActivity() {
        viewModel.activity.observe(viewLifecycleOwner) { activity ->
            if (activity != null) {
                bindActivityData(activity)
            }
        }
    }

    private fun bindActivityData(activity: ActivityEntity) {
        val type = ActivityType.fromString(activity.type)
        binding.textDetailType.text = typeDisplayName(type)

        val dateFormat = SimpleDateFormat("dd.MM.yyyy. 'u' HH:mm", Locale.getDefault())
        binding.textDetailDate.text = dateFormat.format(Date(activity.startTimeMillis))

        // Popunjavamo statistike preko "included" layout-a
        binding.statDistance.statValue.text = com.example.fitnesstracker.ui.settings.UnitsHelper
            .formatDistance(requireContext(), activity.distanceMeters)
        binding.statDistance.statLabel.text = getString(com.example.fitnesstracker.R.string.label_distance)

        binding.statDuration.statValue.text = formatDuration(activity.durationSeconds)
        binding.statDuration.statLabel.text = getString(com.example.fitnesstracker.R.string.label_duration)

        binding.statAvgSpeed.statValue.text = com.example.fitnesstracker.ui.settings.UnitsHelper
            .formatSpeed(requireContext(), activity.avgSpeedMps)
        binding.statAvgSpeed.statLabel.text = getString(com.example.fitnesstracker.R.string.label_avg_speed)

        // Repository dekodira JSON rutu nazad u listu tačaka
        val app = requireActivity().application as FitnessTrackerApp
        val routePoints = app.activityRepository.decodeRoute(activity.routePointsJson)

        val maxSpeedMps = calculateMaxSpeedMps(routePoints)
        binding.statMaxSpeed.statValue.text = com.example.fitnesstracker.ui.settings.UnitsHelper
            .formatSpeed(requireContext(), maxSpeedMps)
        binding.statMaxSpeed.statLabel.text = getString(com.example.fitnesstracker.R.string.label_max_speed)

        if (!activity.description.isNullOrBlank()) {
            binding.textDetailDescription.visibility = View.VISIBLE
            binding.textDetailDescription.text = activity.description
        }

        drawRouteOnMap(routePoints)
    }

    /**
     * Računamo maksimalnu brzinu iz razmaka između uzastopnih GPS tačaka
     * (distanca / vrijeme između dvije tačke).
     */
    private fun calculateMaxSpeedMps(points: List<LatLngPoint>): Double {
        if (points.size < 2) return 0.0

        var maxSpeedMps = 0.0
        for (i in 1 until points.size) {
            val prev = points[i - 1]
            val curr = points[i]

            val results = FloatArray(1)
            android.location.Location.distanceBetween(
                prev.latitude, prev.longitude, curr.latitude, curr.longitude, results
            )
            val distanceMeters = results[0]
            val timeSeconds = (curr.timestampMillis - prev.timestampMillis) / 1000.0

            if (timeSeconds > 0) {
                val speedMps = distanceMeters / timeSeconds
                if (speedMps > maxSpeedMps) maxSpeedMps = speedMps
            }
        }
        return maxSpeedMps
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        // Ako su podaci o ruti stigli PRIJE nego što je mapa spremna, nacrtaj ih sad
        pendingRoutePoints?.let { drawRouteOnMap(it) }
    }

    private fun drawRouteOnMap(points: List<LatLngPoint>) {
        val map = googleMap
        if (map == null) {
            // Mapa još nije spremna - sačuvaj podatke za kasnije (onMapReady)
            pendingRoutePoints = points
            return
        }

        if (points.size < 2) {
            binding.textNoRoute.visibility = View.VISIBLE
            return
        }

        val latLngPoints = points.map { LatLng(it.latitude, it.longitude) }

        map.addPolyline(
            PolylineOptions()
                .addAll(latLngPoints)
                .width(10f)
                .color(android.graphics.Color.parseColor("#4285F4"))
        )

        // Automatski centriramo i zumiramo kameru tako da CIJELA ruta stane na ekran
        val boundsBuilder = LatLngBounds.Builder()
        latLngPoints.forEach { boundsBuilder.include(it) }
        val bounds = boundsBuilder.build()

        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
    }

    private fun typeDisplayName(type: ActivityType): String = when (type) {
        ActivityType.RUNNING -> getString(R.string.activity_running)
        ActivityType.WALKING -> getString(R.string.activity_walking)
        ActivityType.CYCLING -> getString(R.string.activity_cycling)
        ActivityType.OTHER -> getString(R.string.activity_other)
    }

    private fun formatDuration(totalSeconds: Long): String {
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}