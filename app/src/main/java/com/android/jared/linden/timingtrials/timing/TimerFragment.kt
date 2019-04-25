package com.android.jared.linden.timingtrials.timing


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer

import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.databinding.FragmentTimerBinding
import com.android.jared.linden.timingtrials.util.ITEM_ID_EXTRA
import com.android.jared.linden.timingtrials.util.argument
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

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

        timingViewModel.allTtWithEvent.observe(viewLifecycleOwner, Observer {
            val i = it.count()

        })

        timingViewModel.timeTrialWithEvents.observe(viewLifecycleOwner, Observer {
            var name = it.timeTrial.ttName
        })


        val binding =  DataBindingUtil.inflate<FragmentTimerBinding>(inflater, R.layout.fragment_timer, container, false).apply{
            lifecycleOwner = this@TimerFragment
            viewModel = timingViewModel
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
