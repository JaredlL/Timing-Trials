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
import com.android.jared.linden.timingtrials.*
import com.android.jared.linden.timingtrials.adapters.RiderListAdapter
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.databinding.RiderListFragmentBinding
import com.android.jared.linden.timingtrials.util.InjectorUtils
import com.android.jared.linden.timingtrials.viewmodels.RiderListViewModel

class RiderListFragment : Fragment() {

    companion object {
        fun newInstance() = RiderListFragment()
    }

    private lateinit var viewModel: RiderListViewModel
    private  lateinit var adapter: RiderListAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    var selectable = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {


        //need activity context for the viewmodel
        val factory = InjectorUtils.provideRiderListViewModelFactory(requireActivity())
        viewModel = ViewModelProviders.of(requireActivity(), factory).get(RiderListViewModel::class.java)
        viewManager = LinearLayoutManager(context)

        adapter = RiderListAdapter(requireContext())
        adapter.selectable = viewModel.getSelectable()
        viewModel.getAllRiders().observe(viewLifecycleOwner, Observer { riders ->
            riders?.let {adapter.setRiders(it, viewModel.selectedIds)}
        })

        adapter.editRider = ::editRider

        val binding = DataBindingUtil.inflate<RiderListFragmentBinding>(inflater, R.layout.rider_list_fragment, container, false).apply {
            heading.rider = Rider("First Name", "Last Name", "Club", 0)
            heading.checkBox.visibility = if(viewModel.getSelectable()) View.INVISIBLE else View.GONE
            recyclerview.adapter = adapter
            recyclerview.layoutManager = viewManager

            riderListFab.setOnClickListener {
                editRider(Rider.createBlank())
            }
        }


        return binding.root
    }

    private fun editRider(rider: Rider){
        val intent = Intent(context, EditItemActivity::class.java).apply {
            putExtra(ITEM_TYPE_EXTRA, ITEM_RIDER)
            putExtra(ITEM_ID_EXTRA, rider.Id)
        }
        startActivity(intent)
    }
}
