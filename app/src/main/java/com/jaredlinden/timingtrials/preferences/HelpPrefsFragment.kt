package com.jaredlinden.timingtrials.preferences

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
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


        findPreference(R.string.contact_developer)
                .setOnPreferenceClickListener {
                    val uri = Uri.fromParts(
                            "mailto",
                            "linden.jared@gmail.com",
                            null
                    )
                    val t = getDebugInfo()
                    val intent = Intent(Intent.ACTION_SENDTO, uri)
                            .putExtra(Intent.EXTRA_SUBJECT, "Timing Trials Feedback")
                            .putExtra(Intent.EXTRA_TEXT, t)
                    startActivity(intent)
                    false
                }

        findPreference(R.string.p_helpref_demo)
                .setOnPreferenceClickListener {
                    (requireActivity() as MainActivity).showOnboading()
                    false
                }

        findPreference(R.string.p_helpref_version).summary = BuildConfig.VERSION_NAME
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