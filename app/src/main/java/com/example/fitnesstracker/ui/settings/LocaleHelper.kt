package com.example.fitnesstracker.ui.settings

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Upravlja postavljanjem jezika aplikacije. Kombinuje dvije stvari:
 * 1. Čuvanje IZABRANOG jezika u SharedPreferences (da se pamti između pokretanja)
 * 2. Primjenu tog jezika na dati Context (createConfigurationContext)
 */
object LocaleHelper {

    fun setLocale(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(SettingsKeys.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(SettingsKeys.KEY_LANGUAGE, languageCode).apply()
    }

    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(SettingsKeys.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(SettingsKeys.KEY_LANGUAGE, SettingsKeys.LANGUAGE_SERBIAN)
            ?: SettingsKeys.LANGUAGE_SERBIAN
    }

    /**
     * Primjenjuje sačuvani jezik na dati Context i vraća NOVI, "obojeni" Context.
     * Ovo se poziva iz attachBaseContext() u SVAKOJ Activity
     *  Android zahtijeva da se lokalizacija primijeni PRIJE nego što se Activity zaista kreira,
     * što je razlog zašto ne možemo ovo uraditi samo u onCreate().
     */
    fun applyLocale(context: Context): Context {
        val languageCode = getSavedLanguage(context)
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }
}