package com.example.fitnesstracker.ui.goals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitnesstracker.FitnessTrackerApp
import com.example.fitnesstracker.databinding.FragmentGoalsBinding
import com.example.fitnesstracker.ui.ViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.fitnesstracker.R

class GoalsFragment : Fragment() {

    private var _binding: FragmentGoalsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GoalsViewModel by viewModels {
        val app = requireActivity().application as FitnessTrackerApp
        ViewModelFactory(app.activityRepository, app.goalRepository)
    }

    private lateinit var adapter: GoalsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGoalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFab()
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = GoalsAdapter(
            onDeleteClick = { goal -> confirmDelete(goal) }
        )
        binding.recyclerGoals.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@GoalsFragment.adapter
        }
    }

    private fun confirmDelete(goal: com.example.fitnesstracker.data.local.GoalEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.action_delete_goal)
            .setMessage(R.string.confirm_delete_goal_message)
            .setPositiveButton(R.string.action_delete_goal) { _, _ ->
                viewModel.deleteGoal(goal)
            }
            .setNegativeButton(R.string.action_discard, null)
            .show()
    }

    private fun setupFab() {
        binding.fabAddGoal.setOnClickListener {
            AddGoalDialogFragment { goal ->
                viewModel.saveGoal(goal)
            }.show(childFragmentManager, "AddGoalDialog")
        }
    }

    private fun observeData() {
        viewModel.activeGoals.observe(viewLifecycleOwner) { goals ->
            adapter.submitList(goals)
            binding.textGoalsEmpty.visibility = if (goals.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.allActivities.observe(viewLifecycleOwner) { activities ->
            adapter.submitActivities(activities)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}