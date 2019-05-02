package com.android.jared.linden.timingtrials.timing


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager

import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.databinding.FragmentTimerRiderStatusBinding
import com.android.jared.linden.timingtrials.ui.RiderStatusViewWrapper
import com.android.jared.linden.timingtrials.util.ITEM_ID_EXTRA
import com.android.jared.linden.timingtrials.util.argument
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector
import kotlinx.android.synthetic.main.fragment_timer_rider_status.*


/**
 * A simple [Fragment] subclass.
 *
 */
class RiderStatusFragment : Fragment() {

    private val timeTrialId by argument<Long>(ITEM_ID_EXTRA)
    private lateinit var timingViewModel: TimingViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {


        timingViewModel = getViewModel { requireActivity().injector.timingViewModel() }

        val adapter = RiderStatusAdapter(requireActivity())
        val viewManager = GridLayoutManager(context, 4)

        timingViewModel.timeTrial.observe(viewLifecycleOwner, Observer {
            adapter.setRiderStatus(it.riderList.map { r -> RiderStatusViewWrapper(r, it ) })
        })

        val binding = DataBindingUtil.inflate<FragmentTimerRiderStatusBinding>(inflater, R.layout.fragment_timer_rider_status, container, false).apply {
            lifecycleOwner=this@RiderStatusFragment
            riderStatuses.adapter = adapter
            riderStatuses.layoutManager = viewManager
        }


        return binding.root
    }

    companion object {
        fun newInstance(): RiderStatusFragment {
            return RiderStatusFragment()
        }
    }


}
