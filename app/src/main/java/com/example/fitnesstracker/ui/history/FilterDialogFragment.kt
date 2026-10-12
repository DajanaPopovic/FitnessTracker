package com.example.fitnesstracker.ui.history

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.local.ActivityType
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Dialog za filtriranje po više kriterijuma:
 * - Tip aktivnosti
 * - Minimalno trajanje (u minutama)
 * - Datum OD i Datum DO
 */
class FilterDialogFragment(
    private val onApply: (
        type: String?,
        startDate: Long?,
        endDate: Long?,
        minDurationSeconds: Long?
    ) -> Unit
) : DialogFragment() {

    private var startDateMillis: Long? = null
    private var endDateMillis: Long? = null
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        val calendar = Calendar.getInstance()

        // Dinamičko kreiranje interfejsa unutar dijaloga
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 20)
        }

        // 1. Tip aktivnosti (Spinner)
        val labelType = TextView(context).apply {
            text = getString(R.string.filter_activity_type_label)
            textSize = 14f
        }
        val types = arrayOf(
            getString(R.string.filter_all_types),
            getString(R.string.activity_running),
            getString(R.string.activity_walking),
            getString(R.string.activity_cycling),
            getString(R.string.activity_other)
        )
        val spinnerType = Spinner(context).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, types)
        }

        // 2. Min. trajanje u minutama
        val editMinDuration = EditText(context).apply {
            hint = getString(R.string.filter_min_duration_hint)
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        // 3. Početni datum (OD)
        val editStartDate = EditText(context).apply {
            hint = getString(R.string.filter_start_date_hint)
            isFocusable = false
            isClickable = true
            setOnClickListener {
                DatePickerDialog(context, { _, year, month, day ->
                    calendar.set(year, month, day, 0, 0, 0)
                    startDateMillis = calendar.timeInMillis
                    setText(dateFormat.format(Date(startDateMillis!!)))
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
            }
        }

        // 4. Krajnji datum (DO)
        val editEndDate = EditText(context).apply {
            hint = getString(R.string.filter_end_date_hint)
            isFocusable = false
            isClickable = true
            setOnClickListener {
                DatePickerDialog(context, { _, year, month, day ->
                    calendar.set(year, month, day, 23, 59, 59)
                    endDateMillis = calendar.timeInMillis
                    setText(dateFormat.format(Date(endDateMillis!!)))
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
            }
        }

        // Dodavanje elemenata u kontejner
        container.addView(labelType)
        container.addView(spinnerType)
        container.addView(editMinDuration)
        container.addView(editStartDate)
        container.addView(editEndDate)

        return MaterialAlertDialogBuilder(context)
            .setTitle(R.string.dialog_filter_title)
            .setView(container)
            .setPositiveButton(R.string.action_apply) { _, _ ->
                val selectedType = when (spinnerType.selectedItemPosition) {
                    1 -> ActivityType.RUNNING.name
                    2 -> ActivityType.WALKING.name
                    3 -> ActivityType.CYCLING.name
                    4 -> ActivityType.OTHER.name
                    else -> null
                }
                val minMinutes = editMinDuration.text.toString().toLongOrNull()
                val minDurationSeconds = minMinutes?.times(60)

                onApply(selectedType, startDateMillis, endDateMillis, minDurationSeconds)
            }
            .setNegativeButton(R.string.action_clear) { _, _ ->
                onApply(null, null, null, null)
            }
            .create()
    }
}