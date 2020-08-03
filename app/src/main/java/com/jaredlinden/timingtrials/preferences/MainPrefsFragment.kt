package com.jaredlinden.timingtrials.preferences


import android.media.MediaPlayer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.navigation.NavArgs
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.jaredlinden.timingtrials.IFabCallbacks
import com.jaredlinden.timingtrials.R

/**
 * A simple [Fragment] subclass.
 */
class MainPrefsFragment : PreferenceFragmentCompat() {

    val args: MainPrefsFragmentArgs by navArgs()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_main, rootKey)

        (requireActivity() as? IFabCallbacks)?.setVisibility(View.GONE)



        findPreference(R.string.p_mainpref_advanced).apply {
            isVisible = !args.hideLinks
            setOnPreferenceClickListener {
            val action = MainPrefsFragmentDirections.actionSettingsFragmentToAdvancedFragment()
            findNavController().navigate(action)
            true
        }
        }

        findPreference(R.string.p_helpref_about).apply {
            isVisible = !args.hideLinks
            setOnPreferenceClickListener {
                val action = MainPrefsFragmentDirections.actionSettingsFragmentToHelpPrefsFragment()
                findNavController().navigate(action)
                true
            }
        }

        var startPlayer  = MediaPlayer.create(requireContext(), R.raw.start)
        findPreference(R.string.p_mainpref_sound).setOnPreferenceClickListener {
            if((it as SwitchPreference).isChecked){
                if (startPlayer.isPlaying) {
                    startPlayer.seekTo(0)
                    startPlayer.start()
                }else{
                    startPlayer = MediaPlayer.create(requireContext(), R.raw.start)
                    startPlayer.start()
                }
            }
            else{
                startPlayer.pause()
                startPlayer.seekTo(0)
            }
            true
        }

    }
}
