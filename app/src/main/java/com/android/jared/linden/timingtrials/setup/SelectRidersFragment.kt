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
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.android.jared.linden.timingtrials.R

import com.android.jared.linden.timingtrials.adapters.SelectableRiderListAdapter
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.databinding.FragmentSelectriderListBinding
import com.android.jared.linden.timingtrials.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [SelectRidersFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class SelectRidersFragment : Fragment() {

    private lateinit var  viewModel: ISelectRidersViewModel
    private lateinit var adapter: SelectableRiderListAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        viewModel = requireActivity().getViewModel { requireActivity().injector.timeTrialSetupViewModel() }.selectRidersViewModel

        viewManager = LinearLayoutManager(context)
        adapter = SelectableRiderListAdapter(requireContext())

        adapter.setHasStableIds(true)
        adapter.editRider = ::editRider

        val binding = DataBindingUtil.inflate<FragmentSelectriderListBinding>(inflater, R.layout.fragment_selectrider_list, container, false).apply {
            lifecycleOwner = (this@SelectRidersFragment)
            riderHeading.rider =  Rider.createBlank().copy( firstName = "Name", club = "Club")
            riderHeading.checkBox.visibility =  View.INVISIBLE
            riderRecyclerView.adapter = adapter
            riderRecyclerView.layoutManager = viewManager

            riderListFab.setOnClickListener {
                editRider(0)
            }
        }

        adapter.addRiderToSelection = {
            viewModel.addRiderToTt(it)
        }
        adapter.removeRiderFromSelection = {
            viewModel.removeRiderFromTt(it)
        }

        viewModel.selectedRidersInformation.observe(viewLifecycleOwner, Observer {result->
            result?.let {
                adapter.setRiders(it)
            }
        })





        return binding.root
    }

    private fun editRider(riderId: Long){
        val action = SetupViewPagerFragmentDirections.actionSetupViewPagerFragment2ToEditRiderFragment(riderId)
        findNavController().navigate(action)
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
