package io.goosople.poemtime

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.widget.EditText
import androidx.navigation.fragment.findNavController
import androidx.preference.*

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

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
        if (!PreferenceManager.getDefaultSharedPreferences(activity)
                .getBoolean("online_service", false)
        ) numberPreference.isVisible = true
        azurePreference.isVisible = numberPreference.isVisible
        azureKeyPreference.isVisible =
            (azurePreference.isVisible && PreferenceManager.getDefaultSharedPreferences(activity)
                .getBoolean("aztts", false))
        numberPreference.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER
            setEditTextRange(editText, 2, 20)
        }
        findPreference<SwitchPreference>("online_service")!!.setOnPreferenceChangeListener { _, newValue ->
            numberPreference.isVisible = newValue != true
            azurePreference.isVisible = numberPreference.isVisible
            azureKeyPreference.isVisible =
                (azurePreference.isVisible && PreferenceManager.getDefaultSharedPreferences(activity)
                    .getBoolean("aztts", false))
            return@setOnPreferenceChangeListener true
        }
        azurePreference.setOnPreferenceChangeListener { _, newValue ->
            azureKeyPreference.isVisible =
                (azurePreference.isVisible && newValue == true)
            return@setOnPreferenceChangeListener true
        }
    }

    private fun setEditTextRange(editText: EditText, min: Int, max: Int) {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                Log.d("beforeTextChanged", "s=$s; start=$start; count=$count; after=$after")
            }

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Log.d("onTextChanged", "s=$s; start=$start; before=$before; count=$count")
                // 拿到数字
                if (s.toString().isNotEmpty()) {
                    var value = Integer.valueOf(s.toString())
                    if (s.toString().substring(0, 1).contains("0") && value != 0) {
                        // 第一个字为0
                        editText.setText(value.toString())
                        return
                    }
                    // 判断大小
                    if (value < min) {
                        editText.setText(min.toString())
                    } else if (value > max) {
                        // 移除最老的第一个数试试看
                        var ads = s.toString().substring(1)
                        if (ads.isEmpty()) ads = "0"
                        val ad = ads.toInt()
                        if (ad in min..max) {
                            editText.setText(ad.toString())
                        } else {
                            // 排除输入的最后一个是0
                            var ssr = s.toString().substring(0, s.toString().length - 1)
                            if (ssr.contains("0")) {
                                // 移除最老的0
                                ssr = (s.toString().substring(0, s.toString().indexOf("0"))
                                        + s.toString().substring(s.toString().indexOf("0") + 1))
                                value = Integer.valueOf(ssr)
                                if (value in min..max) {
                                    editText.setText(value.toString())
                                    return
                                }
                            }
                            editText.setText(max.toString())
                        }
                    }
                    return
                }
            }

            @SuppressLint("SetTextI18n")
            override fun afterTextChanged(s: Editable) {
                Log.d("afterTextChanged", "s=$s")
            }
        }
        editText.addTextChangedListener(watcher)
    }
}