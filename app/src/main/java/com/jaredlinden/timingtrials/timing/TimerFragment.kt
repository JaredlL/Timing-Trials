package com.jaredlinden.timingtrials.timing


import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceManager


import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.data.NumberMode
import com.jaredlinden.timingtrials.databinding.FragmentTimerBinding
import com.jaredlinden.timingtrials.domain.ITimelineEvent
import com.jaredlinden.timingtrials.ui.EventViewWrapper
import com.jaredlinden.timingtrials.util.EventObserver
import com.jaredlinden.timingtrials.util.PREF_NUMBERING_MODE


/**
 * A simple [Fragment] subclass.
 *
 */
class TimerFragment : Fragment() {

    private val timingViewModel: TimingViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val adapter = EventListAdapter(requireActivity())
        val viewManager = LinearLayoutManager(context)

        val binding =  DataBindingUtil.inflate<FragmentTimerBinding>(inflater, R.layout.fragment_timer, container, false).apply{
            lifecycleOwner = this@TimerFragment
            viewModel = timingViewModel
            eventRecyclerView.layoutManager = viewManager
            eventRecyclerView.adapter = adapter

        }

        timingViewModel.timeLine.observe(viewLifecycleOwner, Observer { res->
            res?.let {tl->
                val oldCount  = adapter.itemCount
                val newList = tl.timeLine.map { ev -> EventViewWrapper(ev, res.timeTrial)}

                adapter.setEvents(newList)

                newList.forEach {evw->

                    evw.getSelected = {e -> timingViewModel.eventAwaitingSelection == e.timeStamp}
                    evw.onSelectionChanged = {e, b ->
                        val oldTimestamp = timingViewModel.eventAwaitingSelection
                        if(b){
                            timingViewModel.eventAwaitingSelection = e.timeStamp
                        }else{
                            if(oldTimestamp == e.timeStamp)timingViewModel.eventAwaitingSelection = null
                        }

                        newList.find { it.event.timeStamp == oldTimestamp }?.notifyPropertyChanged(BR.eventSelected)
                    }
                }
                val newcount = adapter.itemCount
                if(oldCount < adapter.itemCount) binding.eventRecyclerView?.scrollToPosition(newcount - 1)
            }
            if(res == null){
                binding.textView18.text = "TT is null"
            }
        })

        adapter.setHasStableIds(true)

        adapter.longClick = {

                showEventDialog(it)
        }

        timingViewModel.timeString.observe(viewLifecycleOwner, Observer {

        })


        return binding.root
    }



    fun showEventDialog(event: ITimelineEvent){
        AlertDialog.Builder(requireActivity())
                .setTitle("Unassign Event")
                .setMessage("Unassign ${event.rider?.riderData?.fullName()} from event?")
                .setPositiveButton("Yes, unassign"){_,_->
                    timingViewModel.unassignRiderFromEvent(event)
                }
                .setNegativeButton("Dismiss"){_,_->

                }
                .create().show()
    }

    companion object {
        fun newInstance(): TimerFragment {
            return TimerFragment()
        }
    }


}
