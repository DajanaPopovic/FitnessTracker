package com.example.fitnesstracker.ui.settings

import android.content.Context
import java.util.Locale

object UnitsHelper {

    private const val METERS_TO_MILES = 0.000621371

    fun getSavedUnits(context: Context): String {
        val prefs = context.getSharedPreferences(SettingsKeys.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(SettingsKeys.KEY_UNITS, SettingsKeys.UNITS_KM) ?: SettingsKeys.UNITS_KM
    }

    /**
     * Formatira distancu (uvijek čuvanu interno u metrima) u čitljiv tekst,
     * poštujući korisnikov izbor jedinica.
     */
    fun formatDistance(context: Context, meters: Double): String {
        val units = getSavedUnits(context)
        return if (units == SettingsKeys.UNITS_MILES) {
            String.format(Locale.US, "%.2f mi", meters * METERS_TO_MILES)
        } else {
            String.format(Locale.US, "%.2f km", meters / 1000.0)
        }
    }

    /** Isto, ali za brzinu (m/s -> km/h ili mph). */
    fun formatSpeed(context: Context, speedMps: Double): String {
        val units = getSavedUnits(context)
        return if (units == SettingsKeys.UNITS_MILES) {
            String.format(Locale.US, "%.1f mph", speedMps * 2.23694)
        } else {
            String.format(Locale.US, "%.1f km/h", speedMps * 3.6)
        }
    }
}