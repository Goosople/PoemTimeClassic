package io.goosople.poemtime

import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuInflater
import androidx.navigation.fragment.findNavController
import androidx.preference.*
import io.goosople.poemtime.PoemTimeUtils.Companion.setEditTextRange

@Suppress("DEPRECATION")
class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.setting, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        findPreference<Preference>("about")?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_nav_settings_to_about_libs)
            true
        }

        val numberPreference: EditTextPreference = findPreference("delay")!!
        val azureKeyPreference: EditTextPreference = findPreference("key")!!
        val azurePreference: SwitchPreference = findPreference("aztts")!!
        if (!PreferenceManager.getDefaultSharedPreferences(activity!!)
                .getBoolean("online_service", false)
        ) numberPreference.isVisible = true
        azurePreference.isVisible = numberPreference.isVisible
        azureKeyPreference.isVisible =
            (azurePreference.isVisible && PreferenceManager.getDefaultSharedPreferences(activity!!)
                .getBoolean("aztts", false))
        numberPreference.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER
            setEditTextRange(editText, 1, 20)
        }
        findPreference<SwitchPreference>("online_service")!!.setOnPreferenceChangeListener { _, newValue ->
            numberPreference.isVisible = newValue != true
            azurePreference.isVisible = numberPreference.isVisible
            azureKeyPreference.isVisible =
                (azurePreference.isVisible && PreferenceManager.getDefaultSharedPreferences(activity!!)
                    .getBoolean("aztts", false))
            return@setOnPreferenceChangeListener true
        }
        azurePreference.setOnPreferenceChangeListener { _, newValue ->
            azureKeyPreference.isVisible =
                (azurePreference.isVisible && newValue == true)
            return@setOnPreferenceChangeListener true
        }
    }
}