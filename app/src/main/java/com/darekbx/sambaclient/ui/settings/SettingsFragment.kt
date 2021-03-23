package com.darekbx.sambaclient.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.darekbx.sambaclient.BuildConfig
import com.darekbx.sambaclient.R

/**
 * TODO:
 *  - view type:
 *    - list
 *    - grid + columns count
 *
 *
 */
class SettingsFragment: PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener  {

    var onPreferenceChangedListener: (() -> Unit) = { }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setDefaultValues()
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    private fun setDefaultValues() {
        val preferences = preferenceManager.sharedPreferences
        with(preferences.edit()) {
            if (!preferences.contains("show_as_list")) {
                putBoolean("show_as_list", BuildConfig.SHOW_AS_LIST)
            }
            if (!preferences.contains("grid_columns_count")) {
                putInt("grid_columns_count", BuildConfig.GRID_COLUMNS_COUNT)
            }
            apply()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        onPreferenceChangedListener()
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
}
