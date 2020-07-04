package com.jaredlinden.timingtrials.preferences


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceFragmentCompat
import com.jaredlinden.timingtrials.IFabCallbacks
import com.jaredlinden.timingtrials.R

/**
 * A simple [Fragment] subclass.
 */
class MainPrefsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_main, rootKey)

        (requireActivity() as? IFabCallbacks)?.setVisibility(View.GONE)

        findPreference(R.string.p_mainpref_advanced).setOnPreferenceClickListener {
            val action = MainPrefsFragmentDirections.actionSettingsFragmentToAdvancedFragment()
            findNavController().navigate(action)
            true
        }

        findPreference(R.string.p_helpref_about).setOnPreferenceClickListener {
            val action = MainPrefsFragmentDirections.actionSettingsFragmentToHelpPrefsFragment()
            findNavController().navigate(action)
            true
        }

    }
}
