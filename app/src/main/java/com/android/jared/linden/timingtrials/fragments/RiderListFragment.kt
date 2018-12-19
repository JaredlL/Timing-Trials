package com.android.jared.linden.timingtrials.fragments

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.Observer
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.adapters.RiderListAdapter
import com.android.jared.linden.timingtrials.databinding.RiderListFragmentBinding
import com.android.jared.linden.timingtrials.viewmodels.RiderListViewModel
import com.android.jared.linden.timingtrials.viewmodels.RidersViewModel

class RiderListFragment : Fragment() {

    companion object {
        fun newInstance() = RiderListFragment()
    }

    private lateinit var viewModel: RidersViewModel
    private  lateinit var adapter: RiderListAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding: RiderListFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.rider_list_fragment, container, false)
        viewModel = ViewModelProviders.of(this).get(RidersViewModel::class.java)
        adapter = RiderListAdapter(binding.root.context)

        viewModel.getAllRiders().observe(viewLifecycleOwner, Observer { riders ->
            riders?.let {adapter.setRiders(it)}
        })
        viewManager = LinearLayoutManager(activity)

        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = viewManager

        return binding.root
    }



}
