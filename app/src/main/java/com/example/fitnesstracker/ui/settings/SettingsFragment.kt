package com.example.fitnesstracker.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.fitnesstracker.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = SettingsKeys.PREFS_NAME
        preferenceManager.sharedPreferencesMode = android.content.Context.MODE_PRIVATE

        setPreferencesFromResource(R.xml.preferences, rootKey)

        setupLanguagePreference()
    }

    private fun setupLanguagePreference() {
        val languagePref = findPreference<androidx.preference.ListPreference>(SettingsKeys.KEY_LANGUAGE)
        languagePref?.setOnPreferenceChangeListener { _, newValue ->
            val languageCode = newValue as String
            LocaleHelper.setLocale(requireContext(), languageCode)
            requireActivity().recreate()
            true
        }
    }
}