package com.jaredlinden.timingtrials.preferences


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.preference.PreferenceFragmentCompat
import com.jaredlinden.timingtrials.IFabCallbacks
import com.jaredlinden.timingtrials.R

/**
 * A simple [Fragment] subclass.
 */
class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.main_preferences, rootKey)

        (requireActivity() as? IFabCallbacks)?.setVisibility(View.GONE)

    }
}
