package com.jaredlinden.timingtrials.select

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.adapters.SelectableRiderListAdapter
import com.jaredlinden.timingtrials.data.Rider
import com.jaredlinden.timingtrials.databinding.FragmentSelectriderListBinding
import com.jaredlinden.timingtrials.setup.SelectRidersFragment
import com.jaredlinden.timingtrials.setup.SelectRidersFragmentDirections
import com.jaredlinden.timingtrials.setup.SetupViewPagerFragmentDirections
import com.jaredlinden.timingtrials.util.EventObserver
import com.jaredlinden.timingtrials.util.getViewModel
import com.jaredlinden.timingtrials.util.injector

class SelectRiderFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val viewModel = getViewModel { injector.selectRiderViewModel() }

        val viewManager = LinearLayoutManager(context)
        val adapter = SelectableRiderListAdapter(requireContext())

        adapter.setHasStableIds(true)
        adapter.editRider = ::editRider

        val binding = DataBindingUtil.inflate<FragmentSelectriderListBinding>(inflater, R.layout.fragment_selectrider_list, container, false).apply {
            lifecycleOwner = (this@SelectRiderFragment)
            riderHeading.rider =  Rider.createBlank().copy( firstName = getString(R.string.name), club = getString(R.string.club))
            riderHeading.checkBox.visibility =  View.INVISIBLE
            riderRecyclerView.adapter = adapter
            riderRecyclerView.layoutManager = viewManager

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
                result.selectedIds.firstOrNull()?.let {fs->
                    val pos = result.allRiderList.indexOfFirst { it.id == fs }
                    if(pos >=0) {
                        viewManager.scrollToPositionWithOffset(pos, binding.root.height / 2)
                    }
                }

            }

        })

        viewModel.showMessage.observe(viewLifecycleOwner, EventObserver{
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        })

        return binding.root

    }

    private fun editRider(riderId: Long){
        Toast.makeText(requireContext(), "Edit Rider $riderId", Toast.LENGTH_SHORT).show()
    }

}