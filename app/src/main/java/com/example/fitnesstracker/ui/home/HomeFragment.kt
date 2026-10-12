package com.example.fitnesstracker.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.fitnesstracker.FitnessTrackerApp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.local.ActivityEntity
import com.example.fitnesstracker.data.local.ActivityType
import com.example.fitnesstracker.databinding.FragmentHomeBinding
import com.example.fitnesstracker.tracking.ActivityTrackingService
import com.example.fitnesstracker.tracking.TrackingState
import com.example.fitnesstracker.ui.ViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // ViewModel se pravi preko ViewModelFactory, uzimajući repository
    // iz Application klase (FitnessTrackerApp)
    private val viewModel: HomeViewModel by viewModels {
        val app = requireActivity().application as FitnessTrackerApp
        ViewModelFactory(app.activityRepository, app.goalRepository)
    }

    // Trenutno izabran tip aktivnosti (default: trčanje)
    private var selectedType: ActivityType = ActivityType.RUNNING

    // Launcher za traženje permisije lokacije - moderan Android pristup
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (granted) {
            actuallyStartTracking()
        } else {
            Toast.makeText(requireContext(), getString(R.string.permission_denied_message), Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupChipListener()
        setupStartStopButton()
        observeTrackingState()
    }

    private fun setupChipListener() {
        binding.chipGroupActivityType.setOnCheckedStateChangeListener { _, checkedIds ->
            selectedType = when (checkedIds.firstOrNull()) {
                R.id.chip_walking -> ActivityType.WALKING
                R.id.chip_cycling -> ActivityType.CYCLING
                R.id.chip_other -> ActivityType.OTHER
                else -> ActivityType.RUNNING
            }
        }
    }

    private fun setupStartStopButton() {
        binding.buttonStartStop.setOnClickListener {
            if (TrackingState.isTracking.value) {
                stopTrackingAndPromptSave()
            } else {
                requestPermissionAndStart()
            }
        }
    }

    private fun requestPermissionAndStart() {
        val hasPermission = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            actuallyStartTracking()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    private fun actuallyStartTracking() {
        val intent = Intent(requireContext(), ActivityTrackingService::class.java).apply {
            action = ActivityTrackingService.ACTION_START
        }
        ContextCompat.startForegroundService(requireContext(), intent)
        binding.chipGroupActivityType.isEnabled = false
    }

    private fun stopTrackingAndPromptSave() {
        val intent = Intent(requireContext(), ActivityTrackingService::class.java).apply {
            action = ActivityTrackingService.ACTION_STOP
        }
        requireContext().startService(intent)

        showSaveDialog()
    }

    /**
     * Nakon zaustavljanja, nudimo korisniku da unese opcioni opis prije upisa u bazu.
     * Podatke (trajanje, distancu, rutu) uzimamo direktno iz TrackingState-a jer
     * su tu već akumulirani tokom praćenja.
     */
    private fun showSaveDialog() {
        val elapsedSeconds = TrackingState.elapsedSeconds.value
        val distanceMeters = TrackingState.distanceMeters.value
        val routePoints = TrackingState.routePoints.value

        // Ako je aktivnost trajala manje od 5 sekundi, vjerovatno je greška klika - ne upisujemo
        if (elapsedSeconds < 5) {
            resetUiToIdle()
            return
        }

        val editText = EditText(requireContext()).apply {
            hint = getString(R.string.dialog_description_hint)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_save_activity_title)
            .setView(editText)
            .setPositiveButton(R.string.action_save) { _, _ ->
                val app = requireActivity().application as FitnessTrackerApp
                val routeJson = app.activityRepository.encodeRoute(routePoints)
                val avgSpeed = if (elapsedSeconds > 0) distanceMeters / elapsedSeconds else 0.0

                val activity = ActivityEntity(
                    type = selectedType.name,
                    durationSeconds = elapsedSeconds,
                    distanceMeters = distanceMeters,
                    startTimeMillis = System.currentTimeMillis() - (elapsedSeconds * 1000),
                    description = editText.text.toString().ifBlank { null },
                    avgSpeedMps = avgSpeed,
                    routePointsJson = routeJson
                )
                viewModel.saveActivity(activity)
                resetUiToIdle()
                Toast.makeText(requireContext(), "Aktivnost sačuvana", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.action_discard) { _, _ ->
                resetUiToIdle()
            }
            .setCancelable(false)
            .show()
    }

    private fun resetUiToIdle() {
        TrackingState.reset()
        binding.chipGroupActivityType.isEnabled = true
        binding.textElapsedTime.text = "00:00:00"
        binding.textDistance.text = "0.00 km"
        binding.textSpeed.text = "0.0 km/h"
    }

    /**
     * "Posmatramo" TrackingState (StateFlow-ove iz servisa) i ažuriramo UI uživo.
     * repeatOnLifecycle(STARTED) automatski pauzira posmatranje kad Fragment nije
     * vidljiv (npr. korisnik ode na drugi tab) i nastavlja kad se vrati - sprječava
     * nepotrebno trošenje resursa i moguće greške.
     */
    private fun observeTrackingState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    TrackingState.isTracking.collect { isTracking ->
                        binding.buttonStartStop.text = if (isTracking) {
                            getString(R.string.action_stop_activity)
                        } else {
                            getString(R.string.action_start_activity)
                        }
                    }
                }

                launch {
                    TrackingState.elapsedSeconds.collect { seconds ->
                        val h = seconds / 3600
                        val m = (seconds % 3600) / 60
                        val s = seconds % 60
                        binding.textElapsedTime.text = String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
                    }
                }

                launch {
                    TrackingState.distanceMeters.collect { meters ->
                        binding.textDistance.text = com.example.fitnesstracker.ui.settings.UnitsHelper
                            .formatDistance(requireContext(), meters)
                    }
                }

                launch {
                    TrackingState.currentSpeedMps.collect { speedMps ->
                        binding.textSpeed.text = com.example.fitnesstracker.ui.settings.UnitsHelper
                            .formatSpeed(requireContext(), speedMps)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}