package com.android.jared.linden.timingtrials.timing


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.jared.linden.timingtrials.BR

import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.data.EventType
import com.android.jared.linden.timingtrials.databinding.FragmentTimerBinding
import com.android.jared.linden.timingtrials.ui.EventViewWrapper
import com.android.jared.linden.timingtrials.util.ITEM_ID_EXTRA
import com.android.jared.linden.timingtrials.util.argument
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector
import kotlinx.android.synthetic.main.fragment_timer.*

/**
 * A simple [Fragment] subclass.
 *
 */
class TimerFragment : Fragment() {

    private lateinit var timingViewModel: TimingViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        timingViewModel = getViewModel { requireActivity().injector.timingViewModel() }

        val adapter = EventListAdapter(requireActivity())
        val viewManager = LinearLayoutManager(context)

        timingViewModel.timeTrial.observe(viewLifecycleOwner, Observer { res->
            res?.let {tt->
                val oldCount  = adapter.itemCount
                val newList = (tt.eventList.map {ev -> EventViewWrapper(ev, res)})

                newList.forEach {evw->

                    evw.getSelected = {e -> timingViewModel.eventAwaitingSelection == e.timeStamp}

                    evw.onSelectionChanged = {e, b ->
                        val old = timingViewModel.eventAwaitingSelection
                        val new = e.timeStamp
                        if(b) timingViewModel.eventAwaitingSelection = e.timeStamp
                        else timingViewModel.eventAwaitingSelection = null

                    }
                }

                adapter.setEvents(newList)
                val newcount = adapter.itemCount
                if(oldCount < newcount) eventRecyclerView?.scrollToPosition(newcount - 1)
            }
        })


        val binding =  DataBindingUtil.inflate<FragmentTimerBinding>(inflater, R.layout.fragment_timer, container, false).apply{
            lifecycleOwner = this@TimerFragment
            viewModel = timingViewModel
            eventRecyclerView.layoutManager = viewManager
            eventRecyclerView.adapter = adapter

        }

        return binding.root
    }

    companion object {
        fun newInstance(): TimerFragment {
            return TimerFragment()
        }
    }


}
