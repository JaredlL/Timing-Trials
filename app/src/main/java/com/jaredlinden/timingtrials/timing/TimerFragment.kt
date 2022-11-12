package com.jaredlinden.timingtrials.timing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.activityViewModels
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.databinding.FragmentTimerBinding
import com.jaredlinden.timingtrials.domain.ITimelineEvent
import com.jaredlinden.timingtrials.ui.EventViewWrapper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TimerFragment : Fragment() {

    private val timingViewModel: TimingViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val adapter = EventListAdapter(requireActivity()).apply {
            setHasStableIds(true)
            longClick = {
                showEventDialog(it)
            }
        }
        val viewManager = LinearLayoutManager(requireActivity())

        val binding =  FragmentTimerBinding.inflate(inflater, container, false).apply{
            lifecycleOwner = viewLifecycleOwner
            viewModel = timingViewModel
            eventRecyclerView.layoutManager = viewManager
            eventRecyclerView.adapter = adapter
        }

        timingViewModel.timeLine.observe(viewLifecycleOwner) { res->
            res?.let {timeLine->
                val oldCount  = adapter.itemCount
                val newList = timeLine.timeLine.map { ev -> EventViewWrapper(ev, res.timeTrial)}
                newList.forEach {eventViewWrapper->
                    eventViewWrapper.getSelected = {e -> timingViewModel.eventAwaitingSelection == e.timeStamp}
                    eventViewWrapper.onSelectionChanged = {e, b ->
                        val oldTimestamp = timingViewModel.eventAwaitingSelection
                        if(b)
                        {
                            timingViewModel.eventAwaitingSelection = e.timeStamp
                        }
                        else
                        {
                            if(oldTimestamp == e.timeStamp)timingViewModel.eventAwaitingSelection = null
                        }
                        newList.find { it.event.timeStamp == oldTimestamp }?.notifyPropertyChanged(BR.eventSelected)
                    }
                }
                adapter.setEvents(newList)
                val newCount = adapter.itemCount
                if(oldCount < newCount)
                    binding.eventRecyclerView.scrollToPosition(newCount - 1)
            }

            if(res == null)
            {
                binding.textView18.text = getString(R.string.ttIsNull)
            }
        }
        timingViewModel.timeString.observe(viewLifecycleOwner) {}

        return binding.root
    }

    fun showEventDialog(event: ITimelineEvent){
        AlertDialog.Builder(requireActivity())
                .setTitle("Unassign Event")
                .setMessage("Unassign ${event.rider?.riderData?.fullName()} from event?")
                .setPositiveButton("Yes, unassign"){_,_-> timingViewModel.unassignRiderFromEvent(event) }
                .setNegativeButton("Dismiss"){_,_-> }
            .create()
            .show()
    }

    companion object {
        fun newInstance(): TimerFragment {
            return TimerFragment()
        }
    }


}
