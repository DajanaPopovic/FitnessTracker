package com.example.fitnesstracker.ui.settings

/**
 * Centralizovana imena SharedPreferences ključeva - koriste se i u UI (Settings ekran)
 * i u pozadinskoj logici (ReminderWorker, HomeFragment za jedinice) da izbjegnemo
 * greške u kucanju stringova na više mjesta.
 */
object SettingsKeys {
    const val PREFS_NAME = "app_settings"

    const val KEY_LANGUAGE = "app_language"           // "sr" ili "en"
    const val KEY_UNITS = "distance_units"             // "km" ili "mi"
    const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"

    const val LANGUAGE_SERBIAN = "sr"
    const val LANGUAGE_ENGLISH = "en"

    const val UNITS_KM = "km"
    const val UNITS_MILES = "mi"
}