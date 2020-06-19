package com.jaredlinden.timingtrials.setup


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.jaredlinden.timingtrials.IFabCallbacks
import com.jaredlinden.timingtrials.R

import com.jaredlinden.timingtrials.adapters.SelectableRiderListAdapter
import com.jaredlinden.timingtrials.data.*
import com.jaredlinden.timingtrials.databinding.FragmentSelectriderListBinding
import com.jaredlinden.timingtrials.edititem.EditResultFragmentArgs
import com.jaredlinden.timingtrials.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [SelectRidersFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */


class SelectRidersFragment : Fragment() {



    private val args: SelectRidersFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val viewModel = if(args.selectionMode == SELECT_RIDER_FRAGMENT_MULTI) {
            requireActivity().getViewModel { requireActivity().injector.timeTrialSetupViewModel() }.selectRidersViewModel
        }else{
            requireActivity().getViewModel { requireActivity().injector.timeTrialSetupViewModel() }.selectRidersViewModel
        }

        val viewManager = LinearLayoutManager(context)
        val adapter = SelectableRiderListAdapter(requireContext())

        adapter.setHasStableIds(true)
        adapter.editRider = ::editRider

        (requireActivity() as IFabCallbacks).apply {
            setVisibility(View.VISIBLE)
            setImage(R.drawable.ic_add_white_24dp)
            setAction {
                editRider(0)
            }
        }

        val binding = DataBindingUtil.inflate<FragmentSelectriderListBinding>(inflater, R.layout.fragment_selectrider_list, container, false).apply {
            lifecycleOwner = (this@SelectRidersFragment)
            riderHeading.rider =  Rider.createBlank().copy( firstName = "Name", club = "Club")
            riderHeading.checkBox.visibility =  View.INVISIBLE
            riderRecyclerView.adapter = adapter
            riderRecyclerView.layoutManager = viewManager

//            riderListFab.setOnClickListener {
//                editRider(0)
//            }
        }

        adapter.addRiderToSelection = {
            viewModel.riderSelected(it)
        }
        adapter.removeRiderFromSelection = {
            viewModel.riderUnselected(it)
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
        val SELECT_RIDER_FRAGMENT_SINGLE = 0
        val SELECT_RIDER_FRAGMENT_MULTI = 1

        fun newInstance(selectionMode: SelectRidersFragmentArgs) =
                SelectRidersFragment().apply {
                    arguments = selectionMode.toBundle()
                }
    }
}
