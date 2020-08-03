package com.jaredlinden.timingtrials.preferences

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceFragmentCompat
import com.jaredlinden.timingtrials.BuildConfig
import com.jaredlinden.timingtrials.IFabCallbacks
import com.jaredlinden.timingtrials.MainActivity
import com.jaredlinden.timingtrials.R
import timber.log.Timber
import java.util.*

class HelpPrefsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_help, rootKey)

        (requireActivity() as? IFabCallbacks)?.setVisibility(View.GONE)

        findPreference(R.string.p_helpref_documentation).setOnPreferenceClickListener {
            val url = getString(R.string.help_url)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
            true
        }

        findPreference(R.string.contact_developer).setOnPreferenceClickListener {

            try{
                val t = getDebugInfo()
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    type = "*/*"
                    data = Uri.parse("mailto:")
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.email)))
                    putExtra(Intent.EXTRA_SUBJECT, "Timing Trials Feedback")
                    putExtra(Intent.EXTRA_TEXT, t)
                }

            }catch (e:Exception){
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
            }

            false
        }

        findPreference(R.string.p_helpref_demo).setOnPreferenceClickListener {
            (requireActivity() as MainActivity).showDemoDataDialog()
            false
        }

        findPreference(R.string.p_helpref_version).summary = BuildConfig.VERSION_NAME

        findPreference(R.string.p_helpref_about).setOnPreferenceClickListener {
            val action = HelpPrefsFragmentDirections.actionHelpPrefsFragmentToAboutFragment()
            findNavController().navigate(action)
            true
        }
    }

    fun getDebugInfo(): String? {
        try {

            return Arrays.asList("",
                    "----------",
                    ("Timing Trials: "
                            + BuildConfig.VERSION_NAME
                            ) + " ("
                            .toString() + " build "
                            + BuildConfig.VERSION_CODE
                            .toString() + ")",
                    "Android: " + Build.VERSION.RELEASE + " (" + Build.DISPLAY + ")",
                    "Model: " + Build.MANUFACTURER + " " + Build.MODEL,
                    "Product: " + Build.PRODUCT + " (" + Build.DEVICE + ")",
                    "Kernel: "
                            + System.getProperty("os.version")
                            + " ("
                            + Build.VERSION.INCREMENTAL
                            + ")",
                    "----------",
                    "").joinToString("\n")
        } catch (e: Exception) {
            Timber.e(e)
        }
        return ""
    }
}