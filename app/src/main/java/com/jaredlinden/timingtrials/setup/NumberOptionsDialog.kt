package com.jaredlinden.timingtrials.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.data.NumbersDirection
import com.jaredlinden.timingtrials.databinding.FragmentNumberOptionsBinding
import com.jaredlinden.timingtrials.util.PREF_NUMBERING_MODE
import com.jaredlinden.timingtrials.util.getViewModel
import com.jaredlinden.timingtrials.util.injector
import kotlinx.android.synthetic.main.fragment_number_options.*

class NumberOptionsDialog: DialogFragment(){



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val mViewModel = requireActivity().getViewModel { requireActivity().injector.timeTrialSetupViewModel() }.numberOptionsViewModel

        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_AppCompat_Dialog_Alert)


        val binding = DataBindingUtil.inflate<FragmentNumberOptionsBinding>(inflater, R.layout.fragment_number_options, container, false).apply {
            viewModel = mViewModel
            lifecycleOwner = this@NumberOptionsDialog
            numberOptionsSpinner.adapter = ArrayAdapter.createFromResource(requireContext(), R.array.numberOptions, R.layout.support_simple_spinner_dropdown_item)
            radioGroup.setOnCheckedChangeListener { group, checkedId ->
                when(checkedId){
                    ascendingRadioButton.id -> mViewModel.numberDirection.value = NumbersDirection.ASCEND
                    else -> mViewModel.numberDirection.value = NumbersDirection.DESCEND
                }
            }
            button.setOnClickListener {
                this@NumberOptionsDialog.dismiss()
            }
        }

        mViewModel.numberRulesMediator.observe(viewLifecycleOwner, Observer {

        })

        mViewModel.mode.observe(viewLifecycleOwner, Observer {mode->
            mode?.let {
                PreferenceManager.getDefaultSharedPreferences(requireContext()).edit().putString(PREF_NUMBERING_MODE, mode.name).apply()
            }

        })




//        mViewModel.selectedNumberOptionType.observe(viewLifecycleOwner, Observer {
////            if(it == 1){
////                binding.autoNumberOptionsLayout.visibility = View.GONE
////            }else{
////                binding.autoNumberOptionsLayout.visibility = View.VISIBLE
////            }
//        })
        mViewModel.numberDirection.observe(viewLifecycleOwner, Observer {
            it?.let {
                when(it){
                    NumbersDirection.ASCEND -> {
                        binding.radioGroup.check(ascendingRadioButton.id)
                    }
                    else ->{
                        binding.radioGroup.check(descendingRadioButton.id)

                    }

                }


            }
        })

        return binding.root
    }

}