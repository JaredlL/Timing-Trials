package com.android.jared.linden.timingtrials.viewdata

import android.content.Intent
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
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.databinding.FragmentRiderListBinding
import com.android.jared.linden.timingtrials.edititem.EditItemActivity
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector

const val SELECTABLE = "selectable"
//
//class RiderListFragment : Fragment() {
//
//    companion object {
//        fun newInstance(selectable: Boolean = false): RiderListFragment {
//            val args = Bundle().apply { putBoolean(SELECTABLE, selectable) }
//            return RiderListFragment().apply { arguments = args }
//        }
//    }
//
//    private lateinit var viewModel: RiderListViewModel
//    private lateinit var adapter: RiderListAdapter
//    private lateinit var viewManager: RecyclerView.LayoutManager
//
//    //var selectable = false
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
//                              savedInstanceState: Bundle?): View? {
//
//        viewModel = getViewModel { injector.riderListViewModel() }
//        viewManager = LinearLayoutManager(context)
//        adapter = RiderListAdapter(requireContext()).apply { selectable = false }
//
//
//        adapter.editRider = ::editRider
//
//        viewModel.mRiderList.observe(viewLifecycleOwner, Observer { riders ->
//            riders.let {
//                adapter.setRiders(it)
//            }
//        })
//
//        val binding = DataBindingUtil.inflate<FragmentRiderListBinding>(inflater, R.layout.fragment_rider_list, container, false).apply {
//            riderHeading.rider = Rider("First Name", "Surname", "Club", 0)
//            riderRecyclerView.adapter = adapter
//            riderRecyclerView.layoutManager = viewManager
//            riderListFab.setOnClickListener {
//                editRider(Rider.createBlank())
//            }
//        }
//
//
//
//        return binding.root
//    }
//
//    private fun editRider(rider: Rider){
//        val intent = Intent(context, EditItemActivity::class.java).apply {
//            putExtra(ITEM_TYPE_EXTRA, ITEM_RIDER)
//            putExtra(ITEM_ID_EXTRA, rider.id)
//        }
//        startActivity(intent)
//    }
//}
