package com.android.jared.linden.timingtrials.fragments

import android.content.Intent
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
import com.android.jared.linden.timingtrials.EditItemActivity
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.RIDER_EXTRA
import com.android.jared.linden.timingtrials.TimingTrialsDbActivity
import com.android.jared.linden.timingtrials.adapters.RiderListAdapter
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.databinding.RiderListFragmentBinding
import com.android.jared.linden.timingtrials.viewmodels.RidersViewModel
import kotlinx.android.synthetic.main.rider_list_fragment.*
import kotlinx.android.synthetic.main.rider_list_fragment.view.*

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

        viewModel.editRider = ::editRider

        viewManager = LinearLayoutManager(activity)

        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = viewManager

        binding.root.fab2.setOnClickListener {
            editRider( Rider("", "", "", 0))
        }

        return binding.root
    }

    fun editRider(rider: Rider){
        val intent = Intent(context, EditItemActivity::class.java)
        intent.putExtra(RIDER_EXTRA, rider)
        startActivity(intent)
    }



}
