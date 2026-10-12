package com.example.fitnesstracker.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitnesstracker.FitnessTrackerApp
import com.example.fitnesstracker.databinding.FragmentHistoryBinding
import com.example.fitnesstracker.ui.ViewModelFactory

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels {
        val app = requireActivity().application as FitnessTrackerApp
        ViewModelFactory(app.activityRepository, app.goalRepository)
    }

    private lateinit var adapter: ActivityAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        setupFilterButton()
        observeActivities()
    }

    private fun setupRecyclerView() {
        adapter = ActivityAdapter(
            onItemClick = { activity ->
                val bundle = Bundle().apply {
                    putLong("activityId", activity.id)
                }
                findNavController().navigate(
                    com.example.fitnesstracker.R.id.detailFragment,
                    bundle
                )
            },
            onDeleteClick = { activity ->
                com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Brisanje aktivnosti")
                    .setMessage("Da li ste sigurni da želite obrisati ovu aktivnost?")
                    .setPositiveButton("Obriši") { _, _ ->
                        viewModel.deleteActivity(activity)
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Aktivnost je obrisana",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                    .setNegativeButton("Otkaži", null)
                    .show()
            }
        )
        binding.recyclerActivities.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HistoryFragment.adapter
        }
    }

    private fun setupSearch() {
        binding.editSearch.addTextChangedListener { text ->
            val query = text?.toString().orEmpty()
            if (query.isBlank()) {
                viewModel.clearFilter()
            } else {
                viewModel.searchByDescription(query)
            }
        }
    }

    private fun setupFilterButton() {
        binding.buttonFilter.setOnClickListener {
            FilterDialogFragment { type, startDate, endDate, minDurationSeconds ->
                viewModel.applyFilter(
                    type = type,
                    startDate = startDate,
                    endDate = endDate,
                    minDurationSeconds = minDurationSeconds
                )
            }.show(childFragmentManager, "FilterDialog")
        }
    }

    private fun observeActivities() {
        viewModel.activities.observe(viewLifecycleOwner) { activities ->
            adapter.submitList(activities)
            binding.textEmptyState.visibility =
                if (activities.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}