package com.example.fitnesstracker.ui.goals

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.local.ActivityType
import com.example.fitnesstracker.data.local.GoalEntity
import com.example.fitnesstracker.data.local.GoalMetric
import com.example.fitnesstracker.data.local.GoalPeriod
import com.example.fitnesstracker.databinding.DialogAddGoalBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AddGoalDialogFragment(
    private val onSave: (GoalEntity) -> Unit
) : DialogFragment() {

    private lateinit var binding: DialogAddGoalBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAddGoalBinding.inflate(layoutInflater)

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_add_goal_title)
            .setView(binding.root)
            .setPositiveButton(R.string.action_save) { _, _ -> trySave() }
            .setNegativeButton(R.string.action_discard, null)
            .create()
    }

    private fun trySave() {
        val targetInput = binding.editTargetValue.text.toString().toDoubleOrNull()
        if (targetInput == null || targetInput <= 0) return

        val type = when (binding.chipGroupGoalType.checkedChipId) {
            binding.chipGoalRunning.id -> ActivityType.RUNNING
            binding.chipGoalWalking.id -> ActivityType.WALKING
            binding.chipGoalCycling.id -> ActivityType.CYCLING
            else -> null // "Svi tipovi"
        }

        val metric = if (binding.chipGroupMetric.checkedChipId == binding.chipMetricDistance.id) {
            GoalMetric.DISTANCE
        } else {
            GoalMetric.DURATION
        }

        val period = if (binding.chipGroupPeriod.checkedChipId == binding.chipPeriodDaily.id) {
            GoalPeriod.DAILY
        } else {
            GoalPeriod.WEEKLY
        }

        // Korisnik unosi km ili minute (čitljivije), mi konvertujemo u
        // internu jedinicu (metri ili sekunde) prije čuvanja u bazu
        val targetValue = when (metric) {
            GoalMetric.DISTANCE -> targetInput * 1000.0 // km -> m
            GoalMetric.DURATION -> targetInput * 60.0   // min -> s
            GoalMetric.STEPS -> targetInput
        }

        val goal = GoalEntity(
            activityType = type?.name,
            metric = metric.name,
            period = period.name,
            targetValue = targetValue
        )

        onSave(goal)
    }
}