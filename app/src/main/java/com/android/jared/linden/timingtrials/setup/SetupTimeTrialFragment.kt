package com.android.jared.linden.timingtrials.setup


import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TimePicker
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController

import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.databinding.FragmentSetupTimeTrialBinding
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector
import org.threeten.bp.*


class SetupTimeTrialFragment : Fragment() {

    private lateinit var propsViewModel: ITimeTrialPropertiesViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        propsViewModel = requireActivity().getViewModel { requireActivity().injector.timeTrialSetupViewModel() }.timeTrialPropertiesViewModel

        //Order is important
        propsViewModel.setupMediator.observe(viewLifecycleOwner, Observer {  })

        val mAdapter = ArrayAdapter<String>(requireContext(),R.layout.support_simple_spinner_dropdown_item, listOf("15", "30", "60", "90", "120"))
        val binding = DataBindingUtil.inflate<FragmentSetupTimeTrialBinding>(inflater, R.layout.fragment_setup_time_trial, container, false).apply {
            viewModel = propsViewModel
            lifecycleOwner = (this@SetupTimeTrialFragment)
            autocomplete.threshold = 1
            autocomplete.setAdapter(mAdapter)
            coursebutton.setOnClickListener {
                showCourseFrag()
            }
            startTimeButton2.setOnClickListener {
                TimePickerFragment().show(childFragmentManager, "timePicker")
            }

            startTtButton.setOnClickListener{
                propsViewModel.timeTrial.value?.let {
                    if(it.riderList.isEmpty()){
                        Toast.makeText(requireActivity(), "TT Needs at least 1 rider", Toast.LENGTH_LONG).show()
                        //container.currentItem = 1
                        return@let
                    }
                    if(it.timeTrialHeader.startTime.isBefore(OffsetDateTime.now())){
                        Toast.makeText(requireActivity(), "TT must start in the future, select start time", Toast.LENGTH_LONG).show()
                        TimePickerFragment().show(requireActivity().supportFragmentManager, "timePicker")
                        return@let
                    }
                    val confDialog: SetupConfirmationFragment = requireActivity().supportFragmentManager
                            .findFragmentByTag("confdialog") as? SetupConfirmationFragment ?: SetupConfirmationFragment()

                    if(confDialog.dialog?.isShowing != true){
                        confDialog.show(requireActivity().supportFragmentManager, "confdialog")
                    }

                }
            }



        }

        propsViewModel.timeTrial.observe(viewLifecycleOwner, Observer {tt->
          tt?.let {
              if (it.timeTrialHeader.ttName == "" && it.course == null) {
                  showCourseFrag()
              }
          }
        })





        return binding.root
    }

    private fun showCourseFrag(){

        val action = SetupViewPagerFragmentDirections.actionSetupViewPagerFragmentToSelectCourseFragment()
        findNavController().navigate(action)
    }

//    private fun showCourseFrag(){
//        val courseFrag: SelectCourseFragment = requireActivity().supportFragmentManager
//                .findFragmentByTag("dialog") as? SelectCourseFragment ?: SelectCourseFragment.newInstance()
//
//        if(courseFrag.dialog?.isShowing != true) {
//
//            courseFrag.show(requireActivity().supportFragmentManager, "dialog")
//        }
//    }



    companion object {

        @JvmStatic
        fun newInstance() = SetupTimeTrialFragment()
    }
}

class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    private lateinit var timeTrialViewModel: ITimeTrialPropertiesViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker

        timeTrialViewModel = requireActivity().getViewModel { requireActivity().injector.timeTrialSetupViewModel() }.timeTrialPropertiesViewModel

        var now = Instant.now()
        if(timeTrialViewModel.startTime.value != null){
            now = timeTrialViewModel.startTime.value?.toInstant()
        }
        else{
            now.plusSeconds(15*60)
        }
        val ldt = LocalDateTime.ofInstant(now, ZoneId.systemDefault())
        val hour = ldt.hour
        val minute = ldt.minute

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        // Do something with the time chosen by the user


        val ldt = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault())
        val ldt2 = LocalDateTime.of(ldt.year, ldt.month, ldt.dayOfMonth, hourOfDay, minute)
        timeTrialViewModel.startTime.value = ZonedDateTime.of(ldt2, ZoneId.systemDefault()).toOffsetDateTime()
    }
}
