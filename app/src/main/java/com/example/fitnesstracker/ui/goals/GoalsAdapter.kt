package com.example.fitnesstracker.ui.goals

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnesstracker.data.local.ActivityEntity
import com.example.fitnesstracker.data.local.ActivityType
import com.example.fitnesstracker.data.local.GoalEntity
import com.example.fitnesstracker.data.local.GoalMetric
import com.example.fitnesstracker.data.local.GoalPeriod
import com.example.fitnesstracker.databinding.ItemGoalBinding
import java.util.Calendar
import java.util.Locale
import com.example.fitnesstracker.R

class GoalsAdapter(
    private val onDeleteClick: (GoalEntity) -> Unit
) : ListAdapter<GoalEntity, GoalsAdapter.GoalViewHolder>(DiffCallback()) {

    // Sve aktivnosti - ažuriraju se odvojeno od liste ciljeva (preko submitActivities),
    // jer trebaju OBA izvora podataka da se izračuna napredak
    private var allActivities: List<ActivityEntity> = emptyList()

    fun submitActivities(activities: List<ActivityEntity>) {
        allActivities = activities
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val binding = ItemGoalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GoalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GoalViewHolder(private val binding: ItemGoalBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(goal: GoalEntity) {
            val metric = goal.getMetric()
            val period = goal.getPeriod()
            val type = goal.getActivityTypeEnum()

            binding.textGoalTitle.text = buildTitle(type, period)

            val relevantActivities = filterRelevantActivities(goal, period, type)
            val progressValue = calculateProgress(relevantActivities, metric)

            binding.textGoalProgress.text = formatProgress(progressValue, goal.targetValue, metric)

            val percent = if (goal.targetValue > 0) {
                ((progressValue / goal.targetValue) * 100).toInt().coerceIn(0, 100)
            } else 0
            binding.progressGoal.setProgressCompat(percent, true)

            binding.buttonDeleteGoal.setOnClickListener { onDeleteClick(goal) }
        }

        /** Filtrira aktivnosti koje "pripadaju" ovom cilju - po tipu i vremenskom periodu. */
        private fun filterRelevantActivities(
            goal: GoalEntity, period: GoalPeriod, type: ActivityType?
        ): List<ActivityEntity> {
            val periodStartMillis = getPeriodStartMillis(period)
            return allActivities.filter { activity ->
                val matchesType = type == null || activity.getActivityType() == type
                val matchesPeriod = activity.startTimeMillis >= periodStartMillis
                matchesType && matchesPeriod
            }
        }

        private fun getPeriodStartMillis(period: GoalPeriod): Long {
            val calendar = Calendar.getInstance()
            when (period) {
                GoalPeriod.DAILY -> {
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                }
                GoalPeriod.WEEKLY -> {
                    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                }
            }
            return calendar.timeInMillis
        }

        /** Sabira relevantnu metriku (distancu/trajanje/korake) iz filtriranih aktivnosti. */
        private fun calculateProgress(activities: List<ActivityEntity>, metric: GoalMetric): Double {
            return when (metric) {
                GoalMetric.DISTANCE -> activities.sumOf { it.distanceMeters }
                GoalMetric.DURATION -> activities.sumOf { it.durationSeconds }.toDouble()
                // Aproksimacija koraka: ~0.75m po koraku (standardna prosječna vrijednost)
                GoalMetric.STEPS -> activities.sumOf { it.distanceMeters } / 0.75
            }
        }

        private fun formatProgress(current: Double, target: Double, metric: GoalMetric): String {
            return when (metric) {
                GoalMetric.DISTANCE -> String.format(
                    Locale.US, "%.1f / %.1f km", current / 1000.0, target / 1000.0
                )
                GoalMetric.DURATION -> {
                    val currentMin = (current / 60).toInt()
                    val targetMin = (target / 60).toInt()
                    "$currentMin / $targetMin min"
                }
                GoalMetric.STEPS -> "${current.toInt()} / ${target.toInt()} koraka"
            }
        }

        private fun buildTitle(type: ActivityType?, period: GoalPeriod): String {
            val context = binding.root.context
            val typeName = when (type) {
                ActivityType.RUNNING -> context.getString(R.string.activity_running)
                ActivityType.WALKING -> context.getString(R.string.activity_walking)
                ActivityType.CYCLING -> context.getString(R.string.activity_cycling)
                ActivityType.OTHER -> context.getString(R.string.activity_other)
                null -> context.getString(R.string.goal_all_activities)
            }
            val periodName = when (period) {
                GoalPeriod.DAILY -> context.getString(R.string.period_daily_lowercase)
                GoalPeriod.WEEKLY -> context.getString(R.string.period_weekly_lowercase)
            }
            return "$typeName - $periodName"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<GoalEntity>() {
        override fun areItemsTheSame(oldItem: GoalEntity, newItem: GoalEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: GoalEntity, newItem: GoalEntity) = oldItem == newItem
    }
}