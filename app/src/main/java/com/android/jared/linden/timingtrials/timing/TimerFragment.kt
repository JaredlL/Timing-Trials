package com.android.jared.linden.timingtrials.timing


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager

import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.databinding.FragmentTimerBinding
import com.android.jared.linden.timingtrials.ui.EventViewWrapper
import com.android.jared.linden.timingtrials.util.ITEM_ID_EXTRA
import com.android.jared.linden.timingtrials.util.argument
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector

/**
 * A simple [Fragment] subclass.
 *
 */
class TimerFragment : Fragment() {

    private val timeTrialId by argument<Long>(ITEM_ID_EXTRA)
    private lateinit var timingViewModel: TimingViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        timingViewModel = getViewModel { requireActivity().injector.timingViewModel() }.apply { initialise(timeTrialId) }

        val adapter = EventListAdapter(requireActivity())
        val viewManager = LinearLayoutManager(context)

        timingViewModel.timeTrial.observe(viewLifecycleOwner, Observer { res->
            res?.let {
                adapter.setEvents(it.eventList.map {ev -> EventViewWrapper(ev, res) })
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
        fun newInstance(ttId: Long): TimerFragment {
            val args = Bundle().apply { putLong(ITEM_ID_EXTRA, ttId) }
            return TimerFragment().apply { arguments = args }
        }
    }


}
