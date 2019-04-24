package com.android.jared.linden.timingtrials.timing


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.util.ITEM_ID_EXTRA


/**
 * A simple [Fragment] subclass.
 *
 */
class RiderStatusFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_timer_rider_status, container, false)
    }

    companion object {
        fun newInstance(ttId: Long): RiderStatusFragment {
            val args = Bundle().apply { putLong(ITEM_ID_EXTRA, ttId) }
            return RiderStatusFragment().apply { arguments = args }
        }
    }


}
