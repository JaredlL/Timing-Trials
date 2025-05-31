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
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.data.TimeTrialStatus
import com.jaredlinden.timingtrials.databinding.FragmentSetupTimeTrialBinding
import com.jaredlinden.timingtrials.util.ConverterUtils
import dagger.hilt.android.AndroidEntryPoint
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

@AndroidEntryPoint
class SetupTimeTrialFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val setupVm:SetupViewModel by activityViewModels()
        val propsViewModel = setupVm.timeTrialPropertiesViewModel

        // Cant remember why this is needed... to force a livedata computation?
        propsViewModel.setupMediator.observe(viewLifecycleOwner)  {

        }

        val mAdapter = ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, listOf("15", "30", "60", "90", "120"))
        val binding = FragmentSetupTimeTrialBinding.inflate(inflater, container, false).apply {
            viewModel = propsViewModel
            lifecycleOwner = viewLifecycleOwner
            autocomplete.threshold = 1
            autocomplete.setAdapter(mAdapter)
            coursebutton.setOnClickListener {
                showCourseFrag()
            }
            startTimeButton2.setOnClickListener {
                TimePickerFragment2().show(childFragmentManager, "timePicker")
            }

            startTtButton.setOnClickListener{
                propsViewModel.timeTrial.value?.let {tt->
                    if(tt.riderList.isEmpty()){
                        Toast.makeText(requireActivity(), getString(R.string.tt_needs_one_rider), Toast.LENGTH_LONG).show()
                        return@let
                    }
                    if(tt.timeTrialHeader.startTime == null || tt.timeTrialHeader.startTime.isBefore(OffsetDateTime.now())){
                        Toast.makeText(requireActivity(), getString(R.string.tt_must_start_in_the_future), Toast.LENGTH_LONG).show()
                        TimePickerFragment2().show(requireActivity().supportFragmentManager, "timePicker")
                        return@let
                    }

                    val courseString = "${tt.timeTrialHeader.laps} laps of ${tt.course?.courseName?:"Unknown Course"}"

                    val riderString  = if(tt.timeTrialHeader.interval == 0){
                        "${tt.riderList.count()} riders starting at 0 second intervals, mass start!"
                    }else
                    {
                        "${tt.riderList.size} riders starting at ${tt.timeTrialHeader.interval} second intervals"
                    }

                    val startString = "First rider starting at ${tt.let{ ConverterUtils.instantToSecondsDisplayString(tt.timeTrialHeader.startTime.toInstant())}}"

                    AlertDialog.Builder(requireContext())
                        .setTitle(getString(R.string.starting_tt))
                        .setMessage(courseString
                                + System.lineSeparator()
                                + System.lineSeparator()
                                + riderString
                                + System.lineSeparator()
                                + System.lineSeparator()
                                + startString)
                        .setPositiveButton(R.string.ok){_,_->
                            if(tt.timeTrialHeader.startTime.isAfter(OffsetDateTime.now())){
                                val newTt = tt.updateHeader(tt.timeTrialHeader.copy(status = TimeTrialStatus.IN_PROGRESS))
                                setupVm.updateTimeTrial(newTt)
                            }else{
                                Toast.makeText(requireActivity(), getString(R.string.tt_must_start_in_the_future), Toast.LENGTH_LONG).show()
                            }
                        }
                        .setNegativeButton(R.string.dismiss){_,_-> }
                        .create()
                        .show()
                }
            }
        }

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

@AndroidEntryPoint
class  TimePickerFragment2 : DialogFragment(){

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val setupVm:SetupViewModel by activityViewModels()
            // Get the layout inflater
            val inflater = it.layoutInflater

            val v = inflater.inflate(R.layout.fragment_timepicker, null)
            val currentTp: TimePicker = v.findViewById(R.id.timePicker1)
            val title: TextView = v.findViewById(R.id.timePickerTitle)

            val timeTrialViewModel = setupVm.timeTrialPropertiesViewModel

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
