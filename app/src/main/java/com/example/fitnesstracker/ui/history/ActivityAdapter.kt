package com.example.fitnesstracker.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnesstracker.data.local.ActivityEntity
import com.example.fitnesstracker.data.local.ActivityType
import com.example.fitnesstracker.databinding.ItemActivityBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.fitnesstracker.R


class ActivityAdapter(
    private val onItemClick: (ActivityEntity) -> Unit,
    private val onDeleteClick: (ActivityEntity) -> Unit
) : ListAdapter<ActivityEntity, ActivityAdapter.ActivityViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val binding = ItemActivityBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ActivityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ActivityViewHolder(
        private val binding: ItemActivityBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd.MM.yyyy. HH:mm", Locale.getDefault())

        fun bind(activity: ActivityEntity) {
            val type = ActivityType.fromString(activity.type)

            binding.textActivityType.text = typeDisplayName(type)
            binding.textActivityDate.text = dateFormat.format(Date(activity.startTimeMillis))
            binding.textActivityDistance.text = com.example.fitnesstracker.ui.settings.UnitsHelper
                .formatDistance(binding.root.context, activity.distanceMeters)
            binding.textActivityDuration.text = formatDuration(activity.durationSeconds)

            binding.root.setOnClickListener { onItemClick(activity) }

            binding.buttonDelete.setOnClickListener {
                onDeleteClick(activity)
            }
        }

        private fun typeDisplayName(type: ActivityType): String {
            val context = binding.root.context
            return when (type) {
                ActivityType.RUNNING -> context.getString(R.string.activity_running)
                ActivityType.WALKING -> context.getString(R.string.activity_walking)
                ActivityType.CYCLING -> context.getString(R.string.activity_cycling)
                ActivityType.OTHER -> context.getString(R.string.activity_other)
            }
        }

        private fun formatDuration(totalSeconds: Long): String {
            val h = totalSeconds / 3600
            val m = (totalSeconds % 3600) / 60
            val s = totalSeconds % 60
            return String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
        }
    }

    /**
     * DiffUtil poredi stare i nove stavke da zna TAČNO šta se promijenilo
     * (dodato/obrisano/izmijenjeno), umjesto da pretpostavi da se promijenilo sve.
     */
    class DiffCallback : DiffUtil.ItemCallback<ActivityEntity>() {
        override fun areItemsTheSame(oldItem: ActivityEntity, newItem: ActivityEntity): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ActivityEntity, newItem: ActivityEntity): Boolean =
            oldItem == newItem
    }
}