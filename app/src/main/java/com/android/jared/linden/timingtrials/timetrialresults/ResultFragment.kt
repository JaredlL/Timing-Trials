package com.android.jared.linden.timingtrials.timetrialresults

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.data.ITEM_ID_EXTRA
import com.android.jared.linden.timingtrials.databinding.FragmentTimetrialResultBinding
import com.android.jared.linden.timingtrials.util.argument
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector
import kotlinx.android.synthetic.main.fragment_title.*

class ResultFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val timeTrialId by argument<Long>(ITEM_ID_EXTRA)
        val binding = DataBindingUtil.inflate<FragmentTimetrialResultBinding>(inflater, R.layout.fragment_timetrial_result, container, false)

        val resultViewModel = requireActivity().getViewModel {  requireActivity().injector.resultViewModel() }.apply { initialise(timeTrialId) }

        val viewManager = GridLayoutManager(requireActivity(), 2)

        val adapter = ResultListAdapter(requireActivity())

        adapter.setHasStableIds(true)

        return binding.root
    }

}