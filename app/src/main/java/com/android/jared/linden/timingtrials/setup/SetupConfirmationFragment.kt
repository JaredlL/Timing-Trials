package com.android.jared.linden.timingtrials.setup


import android.os.Bundle
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


class SetupConfirmationFragment : DialogFragment() {

    private lateinit var confirmationViewModel: ISetupConformationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        confirmationViewModel = requireActivity().getViewModel { injector.timeTrialSetupViewModel() }.setupConformationViewModel

        confirmationViewModel.title.observe(viewLifecycleOwner, Observer { dialog?.setTitle(it) })

        val binding = DataBindingUtil.inflate<FragmentSetupConfirmationBinding>(inflater, R.layout.fragment_setup_confirmation, container, false).apply{
            lifecycleOwner = (this@SetupConfirmationFragment)
            viewModel = confirmationViewModel
            cancelButton.setOnClickListener {
                this@SetupConfirmationFragment.dismiss()
            }

            okButton.setOnClickListener {
                if(confirmationViewModel.confirmationFunction()){

                }else{
                    Toast.makeText(requireActivity(), "TT must start in the future, select start time", Toast.LENGTH_LONG).show()
                    this@SetupConfirmationFragment.dismiss()
                }
            }

        }


        return binding.root
    }
}

class UseOldConfirmationFragment : DialogFragment() {

    private lateinit var confirmationViewModel: ISetupConformationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        confirmationViewModel = requireActivity().getViewModel { injector.timeTrialSetupViewModel() }.resumeOldConfirmationViewModel

        confirmationViewModel.title.observe(viewLifecycleOwner, Observer { dialog?.setTitle(it) })

        val binding = DataBindingUtil.inflate<FragmentSetupConfirmationBinding>(inflater, R.layout.fragment_setup_confirmation, container, false).apply{
            lifecycleOwner = (this@UseOldConfirmationFragment)
            viewModel = confirmationViewModel
            cancelButton.setOnClickListener {
                this@UseOldConfirmationFragment.dismiss()
            }

            okButton.setOnClickListener{
                confirmationViewModel.confirmationFunction()
                this@UseOldConfirmationFragment.dismiss()
            }


        }

        return binding.root
    }
}
