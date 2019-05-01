package com.android.jared.linden.timingtrials.timing


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager

import com.android.jared.linden.timingtrials.R
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


        timingViewModel = getViewModel { requireActivity().injector.timingViewModel() }.apply { initialise(timeTrialId) }

//        timingViewModel.allTtWithEvent.observe(viewLifecycleOwner, Observer {
//
//        })

       // val adapter = RiderStatusAdapter(requireActivity())
        //val viewManager = GridLayoutManager(context, 4)

        //riderStatuses.adapter = adapter
        //riderStatuses.layoutManager = viewManager
        return inflater.inflate(R.layout.fragment_timer_rider_status, container, false)
    }

    companion object {
        fun newInstance(ttId: Long): RiderStatusFragment {
            val args = Bundle().apply { putLong(ITEM_ID_EXTRA, ttId) }
            return RiderStatusFragment().apply { arguments = args }
        }
    }


}
