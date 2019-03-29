package com.android.jared.linden.timingtrials.setup


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
import com.android.jared.linden.timingtrials.R

import com.android.jared.linden.timingtrials.adapters.SelectableRiderListAdapter
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.databinding.FragmentSelectriderListBinding
import com.android.jared.linden.timingtrials.domain.TimeTrialSetup
import com.android.jared.linden.timingtrials.edititem.EditItemActivity
import com.android.jared.linden.timingtrials.viewdata.ITEM_ID_EXTRA
import com.android.jared.linden.timingtrials.viewdata.ITEM_RIDER
import com.android.jared.linden.timingtrials.viewdata.ITEM_TYPE_EXTRA

import org.koin.androidx.viewmodel.ext.android.viewModel


/**
 * A simple [Fragment] subclass.
 * Use the [SelectRidersFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class SelectRidersFragment : Fragment() {

    private val viewModel: SelectRidersViewModel by viewModel()
    private lateinit var adapter: SelectableRiderListAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {


        viewManager = LinearLayoutManager(context)
        adapter = SelectableRiderListAdapter(requireContext())
        viewModel.getAllRiders().observe(viewLifecycleOwner, Observer { riders ->
            riders?.let {adapter.setRiders(it)}
        })

        adapter.editRider = ::editRider

        val binding = DataBindingUtil.inflate<FragmentSelectriderListBinding>(inflater, R.layout.fragment_selectrider_list, container, false).apply {
            riderHeading.selectableRider =
                    SelectRidersViewModel.SelectableRiderViewWrapper(Rider("Name", "", "Club", 0))
            riderHeading.checkBox.visibility =  View.INVISIBLE
            riderRecyclerView.adapter = adapter
            riderRecyclerView.layoutManager = viewManager

            riderListFab.setOnClickListener {
                editRider(Rider.createBlank())
            }
        }

        return binding.root
    }

    private fun editRider(rider: Rider){
        val intent = Intent(context, EditItemActivity::class.java).apply {
            putExtra(ITEM_TYPE_EXTRA, ITEM_RIDER)
            putExtra(ITEM_ID_EXTRA, rider.id)
        }
        startActivity(intent)
    }


    companion object {
        @JvmStatic
        fun newInstance() =
                SelectRidersFragment().apply {
                    arguments = Bundle().apply {
                    }
                }
    }
}
