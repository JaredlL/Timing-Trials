package com.jaredlinden.timingtrials.preferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import com.jaredlinden.timingtrials.IFabCallbacks
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.util.EventObserver
import com.jaredlinden.timingtrials.util.getViewModel
import com.jaredlinden.timingtrials.util.injector

class AdvancedPrefsFragment : PreferenceFragmentCompat() {

    lateinit var viewModel: PrefsViewModel

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_advanced, rootKey)

        viewModel = requireActivity().getViewModel { injector.prefsViewModel() }



        (requireActivity() as? IFabCallbacks)?.setVisibility(View.GONE)

        findPreference(R.string.p_advanced_clear).setOnPreferenceClickListener {
            val builder = AlertDialog.Builder(requireContext()).setTitle(resources.getString(R.string.clear_database))
                    .setMessage(resources.getString(R.string.confirm_delete_database_message))
                    .setPositiveButton(resources.getString(R.string.delete)) { _, _ ->
                        viewModel.deleteAll()
                    }
                    .setNegativeButton(getString(R.string.dismiss)){ _, _->

                    }
                    .create().show()
            true
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v =super.onCreateView(inflater, container, savedInstanceState)

        viewModel.allDeleted.observe(viewLifecycleOwner, EventObserver{
            if(it){
                Snackbar.make(requireView(), getString(R.string.database_cleared), Snackbar.LENGTH_SHORT).show()
            }
        })
        return v
    }
}