package com.jamal2367.deepl

import android.content.Context
import android.content.res.TypedArray
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.MenuItem
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.preference.*

class SettingsActivity : AppCompatActivity() {

    private val sTypedValue = TypedValue()

    @ColorInt
    fun getColor(context: Context, @AttrRes resource: Int): Int {
        val a: TypedArray = context.obtainStyledAttributes(sTypedValue.data, intArrayOf(resource))
        val color = a.getColor(0, 0)
        a.recycle()
        return color
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        val toolbar: Toolbar = findViewById(R.id.settingsToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // set the navigation bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            window.navigationBarColor = getColor(this, R.attr.trackColor)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {
        private val darkModeKey get() = getString(R.string.key_dark_mode)
        private val switchLangButtonKey get() = getString(R.string.key_switch_lang_button)

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            preferenceManager.sharedPreferencesName = "config"
            val preferences = preferenceManager.sharedPreferences
            val darkMode = findPreference<ListPreference>(darkModeKey)
            darkMode?.value = preferences.getString(
                darkModeKey,
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM.toString()
            )
            val switchLangSettingButton = findPreference<SwitchPreference>(switchLangButtonKey)
            switchLangSettingButton?.isChecked =
                preferences.getBoolean(switchLangButtonKey, true)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val darkModePreference = findPreference<ListPreference>(darkModeKey)
            darkModePreference?.onPreferenceChangeListener = this
        }


        override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
            if (preference.key == darkModeKey) {
                Handler(Looper.getMainLooper()).postDelayed({
                    val data = requireActivity().application.getSharedPreferences(
                        "config",
                        MODE_PRIVATE
                    )
                    var darkThemeMode = data.getString(
                        darkModeKey,
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM.toString()
                    )!!
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && darkThemeMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM.toString()) {
                        darkThemeMode = AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY.toString()
                    }
                    AppCompatDelegate.setDefaultNightMode(darkThemeMode.toInt())
                }, 100)
            }
            return true
        }
    }
}