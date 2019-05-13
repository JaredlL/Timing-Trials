package com.android.jared.linden.timingtrials.timing


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager

import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.databinding.FragmentTimerRiderStatusBinding
import com.android.jared.linden.timingtrials.ui.RiderStatus
import com.android.jared.linden.timingtrials.ui.RiderStatusViewWrapper
import com.android.jared.linden.timingtrials.util.*
import com.android.jared.linden.timingtrials.viewdata.TimingTrialsDbActivity
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


        timingViewModel = requireActivity().getViewModel { injector.timingViewModel() }

        val adapter = RiderStatusAdapter(requireActivity())
        val viewManager = GridLayoutManager(context, 4)

        timingViewModel.timeTrial.observe(viewLifecycleOwner, Observer {tt->
            val newList = tt.helper.unfinishedRiders.map { r -> RiderStatusViewWrapper(r, tt ).apply {
                onPressedCallback = {
                   timingViewModel.tryAssignRider(it).let {res->
                       if(!res.succeeded && res.message != "Null") Toast.makeText(requireContext(), res.message, Toast.LENGTH_LONG).show()
                   }
                }
            }
            }
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
                val ttId = timingViewModel.timeTrial.value?.timeTrialHeader?.id
                timingViewModel.finishTt()
                val resultIntent = Intent(requireActivity(), TimingTrialsDbActivity::class.java)
                resultIntent.putExtra(ITEM_ID_EXTRA, ttId)
                resultIntent.putExtra(ITEM_TYPE_EXTRA, ITEM_COURSE)
                startActivity(resultIntent)
                activity?.finish()
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
