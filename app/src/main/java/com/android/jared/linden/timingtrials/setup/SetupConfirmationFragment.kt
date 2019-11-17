package com.android.jared.linden.timingtrials.setup


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import com.android.jared.linden.timingtrials.MainActivity

import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.databinding.FragmentSetupConfirmationBinding
import com.android.jared.linden.timingtrials.timing.TimingActivity
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector
import com.android.jared.linden.timingtrials.data.ITEM_ID_EXTRA


class SetupConfirmationFragment : DialogFragment() {

    private lateinit var confirmationViewModel: ISetupConformationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        confirmationViewModel = requireActivity().getViewModel { requireActivity().injector.timeTrialSetupViewModel() }.setupConformationViewModel

        confirmationViewModel.title.observe(viewLifecycleOwner, Observer { dialog?.setTitle(it) })

        val binding = DataBindingUtil.inflate<FragmentSetupConfirmationBinding>(inflater, R.layout.fragment_setup_confirmation, container, false).apply{
            lifecycleOwner = (this@SetupConfirmationFragment)
            viewModel = confirmationViewModel
            cancelButton.setOnClickListener {
                confirmationViewModel.negativeFunction()
                this@SetupConfirmationFragment.dismiss()
            }

            okButton.setOnClickListener {
                if(confirmationViewModel.positiveFunction()){
                    val intent = Intent(requireActivity(), TimingActivity::class.java)
                    startActivity(intent)
                    this@SetupConfirmationFragment.dismiss()
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

    //private lateinit var confirmationViewModel: ISetupConformationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

       val confirmationViewModel = requireActivity().getViewModel { injector.mainViewModel() }.resumeOldViewModel

        confirmationViewModel.title.observe(viewLifecycleOwner, Observer { dialog?.setTitle(it) })

        val binding = DataBindingUtil.inflate<FragmentSetupConfirmationBinding>(inflater, R.layout.fragment_setup_confirmation, container, false).apply{
            lifecycleOwner = (this@UseOldConfirmationFragment)
            viewModel = confirmationViewModel
            cancelButton.setOnClickListener {
                confirmationViewModel.negativeFunction()
                val intent = Intent(requireActivity(), MainActivity::class.java)
                startActivity(intent)
                this@UseOldConfirmationFragment.dismiss()
            }

            okButton.setOnClickListener{
                val intent = Intent(requireActivity(), MainActivity::class.java)
                intent.putExtra(ITEM_ID_EXTRA, confirmationViewModel.timeTrialDefinition.value?.id)
                startActivity(intent)
                this@UseOldConfirmationFragment.dismiss()
            }


        }

        return binding.root
    }
}
