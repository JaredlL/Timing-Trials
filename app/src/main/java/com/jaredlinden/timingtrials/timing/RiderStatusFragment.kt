package com.jaredlinden.timingtrials.timing


import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager

import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.data.ITEM_ID_EXTRA
import com.jaredlinden.timingtrials.databinding.FragmentTimerRiderStatusBinding
import com.jaredlinden.timingtrials.ui.RiderStatus
import com.jaredlinden.timingtrials.ui.RiderStatusViewWrapper
import com.jaredlinden.timingtrials.util.*
import kotlinx.android.synthetic.main.fragment_timer_rider_status.*
import org.threeten.bp.*


/**
 * A simple [Fragment] subclass.
 *
 */
class RiderStatusFragment : Fragment() {

    private lateinit var timingViewModel: TimingViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {


        timingViewModel = requireActivity().getViewModel { requireActivity().injector.timingViewModel() }

        val adapter = RiderStatusAdapter(requireActivity())
        val viewManager = GridLayoutManager(context, 4)

        timingViewModel.timeLine.observe(viewLifecycleOwner, Observer {tt->
            val newList = tt.timeTrial.riderList.asSequence().filter { tt.getRiderStatus(it.timeTrialData) != RiderStatus.FINISHED }.map { r -> RiderStatusViewWrapper(r, tt ).apply {
                onPressedCallback = {
                    timingViewModel.tryAssignRider(it)
                }
            }
            }.sortedBy { it.startTimeMilis }.sortedBy { statusSorter(it.status) }.toList()


            if (newList.isEmpty() || newList.all { it.status == RiderStatus.DNF || it.status == RiderStatus.DNS }){
                viewResultsButton.visibility = View.VISIBLE
            }else{
                viewResultsButton.visibility = View.GONE
            }
            adapter.setRiderStatus(newList)
        })



        adapter.onLongClick = ::chooseRiderOptions

        adapter.setHasStableIds(true)


        val binding = DataBindingUtil.inflate<FragmentTimerRiderStatusBinding>(inflater, R.layout.fragment_timer_rider_status, container, false).apply {
            lifecycleOwner=this@RiderStatusFragment
            viewResultsButton.visibility = View.GONE
            riderStatuses.adapter = adapter
            riderStatuses.layoutManager = viewManager
            viewResultsButton.setOnClickListener {
                timingViewModel.finishTt()
            }
        }


        return binding.root
    }

    fun statusSorter(status: RiderStatus): Int{
       return when(status){
            RiderStatus.NOT_STARTED -> 1
            RiderStatus.RIDING -> 1
            RiderStatus.FINISHED -> 1
            RiderStatus.DNF -> 2
            RiderStatus.DNS -> 2
        }
    }

    companion object {
        fun newInstance(): RiderStatusFragment {
            return RiderStatusFragment()
        }
    }

    fun chooseRiderOptions(riderStatus: RiderStatusViewWrapper){

        when(riderStatus.status){
            RiderStatus.NOT_STARTED -> createRiderActionsDialog(riderStatus)
            RiderStatus.RIDING -> createRiderActionsDialog(riderStatus)
            RiderStatus.FINISHED -> createRiderActionsDialog(riderStatus)
            RiderStatus.DNF -> createDnfRiderDialog(riderStatus)
            RiderStatus.DNS -> createDnfRiderDialog(riderStatus)
        }

    }

    private fun createRiderActionsDialog(rs: RiderStatusViewWrapper){


        val tt = timingViewModel.timeTrial.value ?: return

        val milisNow = System.currentTimeMillis()
        val milisTtLast = tt.timeTrialHeader.startTimeMilis + tt.helper.sortedRiderStartTimes.lastKey()

        val dnfDnsstring = if(milisNow > tt.helper.getRiderStartTime(rs.timeTrialRider) + tt.timeTrialHeader.startTimeMilis){
            getString(R.string.rider_dnf_did_not_finish)
        }else{
            getString(R.string.rider_dns_did_not_start)
        }

        val options = if(milisNow > milisTtLast){
            arrayOf(
                    dnfDnsstring,
                    getString(R.string.rider_missed_start_move_to_next_slot),
                    getString(R.string.set_custom_start_time))

        }else{
            arrayOf(
                    dnfDnsstring,
                    getString(R.string.missed_start_move_to_back),
                    getString(R.string.set_custom_start_time))
        }



        val timeTrialRider = rs.timeTrialRider
         AlertDialog.Builder(requireContext()).
                setTitle("Actions for [${rs.number}] ${rs.filledRider.riderData.fullName()}").
                setItems(options) { dialog, which ->
                    // The 'which' argument contains the index position
                    // of the selected item

                    when(which){
                        0 -> {
                            if(milisNow > tt.helper.getRiderStartTime(timeTrialRider) + tt.timeTrialHeader.startTimeMilis){
                                timingViewModel.riderDnf(timeTrialRider)
                            }else{
                                timingViewModel.riderDns(timeTrialRider)
                            }

                        }
                        1 -> timingViewModel.moveRiderToBack(timeTrialRider)
                        2 -> {
                            TimingTimePickerFragment.newInstance(timeTrialRider.riderId?:0, tt.timeTrialHeader.startTimeMilis).show(requireActivity().supportFragmentManager, "tpd")
                        }
                    }

                }.setNegativeButton(resources.getString(R.string.cancel)){_,_ ->}.show()


    }

    private fun createDnfRiderDialog(rs: RiderStatusViewWrapper){

        val title = if(rs.status == RiderStatus.DNF){
            resources.getString(R.string.undo_dnf)
        }else{
            resources.getString(R.string.undo_dns)
        }

        AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(resources.getString(R.string.undo_dns_description))
                .setPositiveButton(resources.getString(R.string.undo)) { _, _ ->
                    timingViewModel.undoDnf(rs.timeTrialRider)
                }
                .setNegativeButton("Dismiss"){_,_->

                }
                .create().show()
    }

    class TimingTimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

        var riderId :Long = 0
        var ttStartTime :Long = 0
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            // Use the current time as the default values for the picker

            riderId = arguments?.getLong(ITEM_ID_EXTRA)?:0
            ttStartTime = arguments?.getLong("TT_START_TME")?:0
            val dateTime = LocalDateTime.now()

            // Create a new instance of TimePickerDialog and return it
            return TimePickerDialog(activity, this, dateTime.hour, dateTime.minute, DateFormat.is24HourFormat(activity))
        }

        override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
            // Do something with the time chosen by the user
            val ldt = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault())
            val ldt2 = LocalDateTime.of(ldt.year, ldt.month, ldt.dayOfMonth, hourOfDay, minute, 0)

            val selectedMillis = ldt2.toEpochSecond(ZoneId.systemDefault().rules.getOffset(Instant.now()))*1000

            if(selectedMillis < ttStartTime){
                Toast.makeText(requireContext(), "Cannot set start time before TT start", Toast.LENGTH_SHORT).show()
            }else{
                val vm = requireActivity().getViewModel { requireActivity().injector.timingViewModel() }
                vm.setRiderStartTime(riderId, selectedMillis)
            }


        }

        companion object {
            fun newInstance(riderId: Long, ttStartTime: Long): TimingTimePickerFragment{
                val new = TimingTimePickerFragment()
                val args = Bundle().apply {
                    putLong(ITEM_ID_EXTRA, riderId)
                    putLong("TT_START_TME", ttStartTime)
                }
                new.arguments = args
                return new
            }
        }

    }


}
