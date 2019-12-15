package com.android.jared.linden.timingtrials.timing


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.NavDeepLinkBuilder
import androidx.recyclerview.widget.GridLayoutManager
import com.android.jared.linden.timingtrials.MainActivity

import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.data.ITEM_ID_EXTRA
import com.android.jared.linden.timingtrials.databinding.FragmentTimerRiderStatusBinding
import com.android.jared.linden.timingtrials.timetrialresults.ResultFragmentArgs
import com.android.jared.linden.timingtrials.ui.RiderStatus
import com.android.jared.linden.timingtrials.ui.RiderStatusViewWrapper
import com.android.jared.linden.timingtrials.util.*
import kotlinx.android.synthetic.main.fragment_timer_rider_status.*


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
            val newList = tt.timeTrial.riderList.asSequence().filter { tt.getRiderStatus(it.timeTrialData) != RiderStatus.FINISHED }.map { r -> RiderStatusViewWrapper(r.timeTrialData, tt ).apply {
                onPressedCallback = {
                    timingViewModel.tryAssignRider(it)
                }
            }
            }.toList()
            if (newList.isNotEmpty()){
                viewResultsButton.visibility = View.GONE
            }else{
                viewResultsButton.visibility = View.VISIBLE
            }
            adapter.setRiderStatus(newList)
        })



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

    companion object {
        fun newInstance(): RiderStatusFragment {
            return RiderStatusFragment()
        }
    }


}
