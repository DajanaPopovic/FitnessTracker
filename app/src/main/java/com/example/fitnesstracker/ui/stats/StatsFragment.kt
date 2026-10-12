package com.example.fitnesstracker.ui.stats

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.fitnesstracker.FitnessTrackerApp
import com.example.fitnesstracker.data.local.ActivityEntity
import com.example.fitnesstracker.data.local.ActivityType
import com.example.fitnesstracker.data.local.ActivityTypeCount
import com.example.fitnesstracker.databinding.FragmentStatsBinding
import com.example.fitnesstracker.ui.ViewModelFactory
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.util.Calendar
import com.example.fitnesstracker.R

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StatsViewModel by viewModels {
        val app = requireActivity().application as FitnessTrackerApp
        ViewModelFactory(app.activityRepository, app.goalRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupChartAppearance()
        observeData()
    }

    private fun setupChartAppearance() {
        val textColor = com.google.android.material.color.MaterialColors.getColor(
            binding.root, com.google.android.material.R.attr.colorOnSurface
        )

        binding.chartDistanceByDay.apply {
            description.isEnabled = false
            legend.isEnabled = false
            axisRight.isEnabled = false
            axisLeft.axisMinimum = 0f
            axisLeft.textColor = textColor
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.textColor = textColor
            setFitBars(true)
            setNoDataText(getString(com.example.fitnesstracker.R.string.stats_empty_message))
            setNoDataTextColor(textColor)
        }

        binding.chartCountByType.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            setDrawEntryLabels(true)
            setEntryLabelColor(textColor)
            setNoDataText(getString(com.example.fitnesstracker.R.string.stats_empty_message))
            setNoDataTextColor(textColor)
            setCenterTextColor(textColor)
            legend.textColor = textColor
        }
    }

    private fun observeData() {
        viewModel.activitiesLastWeek.observe(viewLifecycleOwner) { activities ->
            updateDistanceChart(activities)
            checkEmptyState(activities)
        }

        viewModel.activityCountByType.observe(viewLifecycleOwner) { counts ->
            updatePieChart(counts)
        }
    }

    private fun checkEmptyState(activities: List<ActivityEntity>) {
        binding.textStatsEmpty.visibility = if (activities.isEmpty()) View.VISIBLE else View.GONE
    }

    /**
     * Grupiše aktivnosti po danu (poslednjih 7 dana) i sabira distancu za svaki dan,
     * zatim popunjava bar chart. Dani bez aktivnosti prikazuju se sa vrijednošću 0.
     */
    private fun updateDistanceChart(activities: List<ActivityEntity>) {
        val dayLabels = mutableListOf<String>()
        val distanceByDayKm = mutableListOf<Float>()

        val calendar = Calendar.getInstance()

        val dayNames = resources.getStringArray(R.array.day_abbreviations)

// Pravimo 7 "kanti" - jedna po danu, počevši od 6 dana unazad do danas.
// Ključ je datum (godina+dan u godini) da bismo bili sigurni da se dani
// iz različitih sedmica ne pomiješaju ako aktivnost ima isti "EEE" naziv.
        val dayBuckets = LinkedHashMap<Int, Pair<String, Float>>()
        for (i in 6 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)

            val dayOfYearKey = calendar.get(Calendar.YEAR) * 1000 + calendar.get(Calendar.DAY_OF_YEAR)
            val label = dayNameFor(calendar, dayNames)
            dayBuckets[dayOfYearKey] = label to 0f
        }

        activities.forEach { activity ->
            val activityDay = Calendar.getInstance().apply { timeInMillis = activity.startTimeMillis }
            val dayOfYearKey = activityDay.get(Calendar.YEAR) * 1000 + activityDay.get(Calendar.DAY_OF_YEAR)

            val existing = dayBuckets[dayOfYearKey]
            if (existing != null) {
                val km = (activity.distanceMeters / 1000.0).toFloat()
                dayBuckets[dayOfYearKey] = existing.first to (existing.second + km)
            }
        }

        dayBuckets.values.forEach { (label, distance) ->
            dayLabels.add(label)
            distanceByDayKm.add(distance)
        }

        val entries = distanceByDayKm.mapIndexed { index, value -> BarEntry(index.toFloat(), value) }

        val textColor = com.google.android.material.color.MaterialColors.getColor(
            binding.root, com.google.android.material.R.attr.colorOnSurface
        )

        val dataSet = BarDataSet(entries, "").apply {
            color = requireContext().getColor(com.google.android.material.R.color.material_dynamic_primary40)
            valueTextSize = 10f
            valueTextColor = textColor
        }

        binding.chartDistanceByDay.apply {
            data = BarData(dataSet)
            xAxis.valueFormatter = IndexAxisValueFormatter(dayLabels)
            xAxis.granularity = 1f
            xAxis.labelCount = dayLabels.size
            animateY(600)
            invalidate()
        }
    }

    private fun updatePieChart(counts: List<ActivityTypeCount>) {
        if (counts.isEmpty()) return

        val entries = counts.map { count ->
            val type = ActivityType.fromString(count.type)
            PieEntry(count.count.toFloat(), typeDisplayName(type))
        }

        val colors = listOf(
            Color.parseColor("#4285F4"), // plava
            Color.parseColor("#34A853"), // zelena
            Color.parseColor("#FBBC04"), // žuta
            Color.parseColor("#9AA0A6")  // siva - za "Ostalo"
        )

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            valueTextSize = 12f
            valueTextColor = Color.WHITE
        }

        binding.chartCountByType.apply {
            data = PieData(dataSet)
            animateY(600)
            invalidate()
        }
    }

    private fun typeDisplayName(type: ActivityType): String = when (type) {
        ActivityType.RUNNING -> getString(R.string.activity_running)
        ActivityType.WALKING -> getString(R.string.activity_walking)
        ActivityType.CYCLING -> getString(R.string.activity_cycling)
        ActivityType.OTHER -> getString(R.string.activity_other)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Vraća naziv dana iz našeg string-array-a (latinica, prati jezik iz podešavanja),
     * na osnovu Calendar.DAY_OF_WEEK vrijednosti. Android Calendar broji nedjelju
     * kao dan 1, pa prilagođavamo indeks tako da niz počinje od ponedjeljka (index 0).
     */
    private fun dayNameFor(calendar: Calendar, dayNames: Array<String>): String {
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1=nedjelja, 2=ponedjeljak, ... 7=subota
        val index = (dayOfWeek ) % 7 // pretvara u 0=ponedjeljak, 1=utorak, ..., 6=nedjelja
        return dayNames[index]
    }
}