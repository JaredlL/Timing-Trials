package com.jaredlinden.timingtrials.onboarding

import android.app.Dialog
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.util.EventObserver
import com.jaredlinden.timingtrials.util.getViewModel
import com.jaredlinden.timingtrials.util.injector
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.net.URLConnection


class OnboardingFragment : DialogFragment() {


//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
//                              savedInstanceState: Bundle?): View? {
//        // Inflate the layout for this fragment
//
//
//
//        return inflater.inflate(R.layout.fragment_onboarding, container, false)
//    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            val v = it.layoutInflater.inflate(R.layout.fragment_onboarding, null)

            builder.setView(v)
                    // Add action buttons
                    .setPositiveButton(R.string.yes) { dialog, id ->
                        val url = URL("https://bb.githack.com/lindenj/timingtrialsdata/raw/master/Timing Trials Export 20-06-20.tt")
                        val vm = requireActivity().getViewModel { requireActivity().injector.importViewModel()}
                        vm.readUrlInput(url)

                        vm.importMessage.observe(requireActivity(), EventObserver{
                            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                        })
                    }
                    .setNegativeButton(R.string.no) { dialog, id ->
                        getDialog()?.cancel()
                    }
            builder.create()

        }?: throw IllegalStateException("Activity cannot be null")
    }

}