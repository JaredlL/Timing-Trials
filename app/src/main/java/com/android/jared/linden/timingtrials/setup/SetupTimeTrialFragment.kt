package com.android.jared.linden.timingtrials.setup


import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.TimePicker
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment

import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.databinding.FragmentSetupTimeTrialBinding
import kotlinx.android.synthetic.main.fragment_setup_time_trial.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

import java.util.*


class SetupTimeTrialFragment : Fragment() {

    private val timeTrialViewModel: SetupTimeTrialViewModel by sharedViewModel()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {


        val mAdapter = ArrayAdapter<String>(activity,R.layout.support_simple_spinner_dropdown_item, listOf("15", "30", "60", "90", "120"))
        val binding = DataBindingUtil.inflate<FragmentSetupTimeTrialBinding>(inflater, R.layout.fragment_setup_time_trial, container, false).apply {
            viewModel = timeTrialViewModel

            lifecycleOwner = (this@SetupTimeTrialFragment)
            autocomplete.threshold = 1
            autocomplete.setAdapter(mAdapter)
            coursebutton.setOnClickListener {
                showCourseFrag()
            }
            startTimeButton2.setOnClickListener {
                TimePickerFragment().show(childFragmentManager, "timePicker")
            }


        }

        timeTrialViewModel.timeTrialName.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
           if(it == "" && timeTrialViewModel.courseName.value == null){
               showCourseFrag()
           }
        })

        return binding.root
    }

    private fun showCourseFrag(){
        val courseFrag: SelectCourseFragment = requireActivity().supportFragmentManager
                .findFragmentByTag("dialog") as? SelectCourseFragment ?: SelectCourseFragment.newInstance()

        courseFrag.show(requireActivity().supportFragmentManager, "dialog")
    }



    companion object {

        @JvmStatic
        fun newInstance() = SetupTimeTrialFragment()
    }
}

class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    private val timeTrialViewModel: SetupTimeTrialViewModel by sharedViewModel()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        if(timeTrialViewModel.startTime.value != null){
            c.time = timeTrialViewModel.startTime.value
        }
        else{
            c.add(Calendar.MINUTE, 15)
            c.set(Calendar.SECOND, 0)
            c.set(Calendar.MILLISECOND, 0)
        }
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute, true)
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        // Do something with the time chosen by the user
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, hourOfDay)
        c.set(Calendar.MINUTE, minute)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        timeTrialViewModel.setStartTime(c.time)
    }
}
