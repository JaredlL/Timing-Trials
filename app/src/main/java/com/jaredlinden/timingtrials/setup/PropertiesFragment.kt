package com.jaredlinden.timingtrials.setup


import android.app.Dialog
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.databinding.FragmentSetupTimeTrialBinding
import com.jaredlinden.timingtrials.util.getViewModel
import com.jaredlinden.timingtrials.util.injector
import org.threeten.bp.*


class SetupTimeTrialFragment : Fragment() {

    private lateinit var propsViewModel: ITimeTrialPropertiesViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        propsViewModel = requireActivity().getViewModel { requireActivity().injector.timeTrialSetupViewModel() }.timeTrialPropertiesViewModel

        //Order is important
        propsViewModel.setupMediator.observe(viewLifecycleOwner, object : Observer<Any> {
            override fun onChanged(t: Any?) {

            }

        })

        val mAdapter = ArrayAdapter<String>(requireContext(), R.layout.support_simple_spinner_dropdown_item, listOf("15", "30", "60", "90", "120"))
        val binding = DataBindingUtil.inflate<FragmentSetupTimeTrialBinding>(inflater, R.layout.fragment_setup_time_trial, container, false).apply {
            viewModel = propsViewModel
            lifecycleOwner = (this@SetupTimeTrialFragment)
            autocomplete.threshold = 1
            autocomplete.setAdapter(mAdapter)
            coursebutton.setOnClickListener {
                showCourseFrag()
            }
            startTimeButton2.setOnClickListener {
                TimePickerFragment2().show(childFragmentManager, "timePicker")
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
                        TimePickerFragment2().show(requireActivity().supportFragmentManager, "timePicker")
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




    companion object {

        @JvmStatic
        fun newInstance() = SetupTimeTrialFragment()
    }
}

class  TimePickerFragment2 : DialogFragment(){


    //var tp :TimePicker? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater

            val v = inflater.inflate(R.layout.fragment_timepicker, null)
            val currentTp: TimePicker = v.findViewById(R.id.timePicker1)
            val title: TextView = v.findViewById(R.id.timePickerTitle)

            val timeTrialViewModel = requireActivity().getViewModel { requireActivity().injector.timeTrialSetupViewModel() }.timeTrialPropertiesViewModel

            val initialLdt = LocalDateTime.ofInstant(Instant.now().plusSeconds(60*10), ZoneId.systemDefault())
            val ldtNow = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault())
            val initialHour = timeTrialViewModel.startTime.value?.hour?:initialLdt.hour
            val initialMinute = timeTrialViewModel.startTime.value?.minute?:initialLdt.minute
            val initialMinDif = (initialHour * 60 + initialMinute) - (ldtNow.hour * 60 + ldtNow.minute)
            val initialCorrected = if(initialMinDif <= 0) initialMinDif + 24*60 else initialMinDif


            currentTp.setIs24HourView(DateFormat.is24HourFormat(activity))
            currentTp.currentHour = initialHour
            currentTp.currentMinute = initialMinute
            title.text = resources.getString(R.string.start_in_string, initialCorrected)
            currentTp.setOnTimeChangedListener { _, h, m ->

                val ldt = LocalDateTime.now()
                val minDif = (h * 60 + m) - (ldt.hour * 60 + ldt.minute)
                val corrected = if(minDif <= 0) minDif + 24*60 else minDif
                title.text = resources.getString(R.string.start_in_string, corrected)
                //Toast.makeText(requireContext(), "${currentTp.currentHour}:${currentTp.currentMinute}", Toast.LENGTH_SHORT).show()
            }

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(v)
                    // Add action buttons
                    .setPositiveButton(R.string.ok) { dialog, id ->

                        val ld = LocalDate.now()
                        val ldt2 = LocalDateTime.of(ld.year, ld.month, ld.dayOfMonth, currentTp.currentHour, currentTp.currentMinute)

                        timeTrialViewModel.startTime.value = ZonedDateTime.of(ldt2, ZoneId.systemDefault()).toOffsetDateTime()
                    }
                    .setNegativeButton(R.string.cancel) { dialog, id ->
                        getDialog()?.cancel()
                    }
            builder.create()

        } ?: throw IllegalStateException("Activity cannot be null")
    }

}
