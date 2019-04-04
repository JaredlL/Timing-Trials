package com.android.jared.linden.timingtrials.setup


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer

import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.databinding.FragmentSetupConfirmationBinding
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector
import kotlinx.android.synthetic.main.fragment_setup_confirmation.*


class SetupConfirmationFragment : DialogFragment() {

    private lateinit var setupViewModel: ISetupConformationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        setupViewModel = requireActivity().getViewModel { injector.timeTrialSetupViewModel() }.setupConformationViewModel

        setupViewModel.title.observe(viewLifecycleOwner, Observer { dialog?.setTitle(it) })

        val binding = DataBindingUtil.inflate<FragmentSetupConfirmationBinding>(inflater, R.layout.fragment_setup_confirmation, container, false).apply{
            lifecycleOwner = (this@SetupConfirmationFragment)
            viewModel = setupViewModel
            cancelButton.setOnClickListener {
                this@SetupConfirmationFragment.dismiss()
            }


            setupViewModel.onStartTT = {doStart ->
                if(doStart)
                {

                }
                else
                {
                    Toast.makeText(requireActivity(), "TT must start in the future, select start time", Toast.LENGTH_LONG).show()
                    this@SetupConfirmationFragment.dismiss()
                }

        }


        }

        return binding.root
    }


}
