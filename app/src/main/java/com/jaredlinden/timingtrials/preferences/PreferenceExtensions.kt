package com.jaredlinden.timingtrials.preferences

import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

fun PreferenceFragmentCompat.findPreference(prefStringId: Int): Preference {
    return findPreference(getString(prefStringId))!!
}