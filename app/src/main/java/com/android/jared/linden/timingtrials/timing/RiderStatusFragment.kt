package com.android.jared.linden.timingtrials.timing


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager

import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.databinding.FragmentTimerRiderStatusBinding
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
            val newList = tt.timeTrial.riderList.asSequence().filter { tt.getRiderStatus(it.timeTrialData) != RiderStatus.FINISHED }.sortedBy { it.timeTrialData.number }.map { r -> RiderStatusViewWrapper(r.timeTrialData, tt ).apply {
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

        adapter.onLongClick = {

        }


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

class RiderStatusDialog: DialogFragment(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

}
