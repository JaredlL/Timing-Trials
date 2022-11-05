package com.jaredlinden.timingtrials.select

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.jaredlinden.timingtrials.IFabCallbacks
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.adapters.SelectableRiderListAdapter
import com.jaredlinden.timingtrials.data.Rider
import com.jaredlinden.timingtrials.databinding.FragmentSelectriderListBinding
import com.jaredlinden.timingtrials.setup.*
import com.jaredlinden.timingtrials.util.*
import dagger.hilt.android.AndroidEntryPoint

const val SELECTED_RIDERS = "selected_riders"

@AndroidEntryPoint
class SelectRiderFragment : Fragment() {

    private val args: SelectRiderFragmentArgs by navArgs()
    private val viewModel: SelectRiderViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        setHasOptionsMenu(true)

        val viewManager = LinearLayoutManager(context)
        val adapter = SelectableRiderListAdapter(requireContext())

        adapter.setHasStableIds(true)
        adapter.editRider = ::editRider

        (activity as? IFabCallbacks)?.apply {
            setFabVisibility(View.VISIBLE)
            setFabImage(R.drawable.ic_add_white_24dp)
            fabClickEvent.observe(viewLifecycleOwner, EventObserver{
                val act = SelectRiderFragmentDirections.actionSelectRiderFragmentToEditRiderFragment2()
                findNavController().navigate(act)
            })
        }

        val currentSortMode = PreferenceManager.getDefaultSharedPreferences(requireActivity()).getInt(SORT_KEY, SORT_DEFAULT)
        viewModel.setSortMode(currentSortMode)

        val binding = FragmentSelectriderListBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            riderHeading.rider =  Rider.createBlank().copy( firstName = getString(R.string.name), club = getString(R.string.club))
            riderHeading.checkBox.visibility =  View.INVISIBLE
            riderRecyclerView.adapter = adapter
            riderRecyclerView.layoutManager = viewManager

        }

        adapter.addRiderToSelection = {

            if(args.singleSelectionMode){
                findNavController().previousBackStackEntry?.savedStateHandle?.set(SELECTED_RIDERS, listOf(it.id?:0L).toLongArray())
                findNavController().popBackStack()
            }

            viewModel.riderSelected(it)
        }

        adapter.removeRiderFromSelection = {
            viewModel.riderUnselected(it)
        }

        adapter.editRider = {
            val act = SelectRiderFragmentDirections.actionSelectRiderFragmentToEditRiderFragment2(it)
            findNavController().navigate(act)
        }


        viewModel.liveSortMode.observe(viewLifecycleOwner) {}
        viewModel.riderFilter.observe(viewLifecycleOwner) {}

        viewModel.selectedRidersInformation.observe(viewLifecycleOwner, Observer {result->
            result?.let {
                if(args.singleSelectionMode){
                    adapter.setRiders(it.copy(allRiderList = it.allRiderList.filterNot { args.excludedIds.contains(it.id?:0L) }))
                }else{
                    adapter.setRiders(it)
                }

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
    var sv: SearchView? = null

    //Keep listener ref so we can remove it later (possibly prevent mem leak)
    val expandListener = object : MenuItem.OnActionExpandListener {
        override fun onMenuItemActionCollapse(item: MenuItem): Boolean {

            sv?.clearFocus()
            return true // Return true to collapse action view
        }

        override fun onMenuItemActionExpand(item: MenuItem): Boolean {
            // Do something when expanded
            sv?.requestFocus()
            showKeyboard()
            return true // Return true to expand action view
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_select_riders, menu)

        val searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        menu.findItem(R.id.select_rider_search).setOnActionExpandListener(expandListener)
        (menu.findItem(R.id.select_rider_search).actionView as SearchView).apply {
            // Assumes current activity is the searchable activity
            setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
            isIconifiedByDefault = false
            setQuery(viewModel.riderFilter.value ?: "", false)
            sv = this
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(searchText: String?): Boolean {
                    viewModel.setRiderFilter(searchText ?: "")
                    clearFocus()
                    return true
                }

                override fun onQueryTextChange(searchText: String?): Boolean {
                    viewModel.setRiderFilter(searchText ?: "")
                    return true
                }

            })
        }

        menu.findItem(R.id.select_rider_sort).setOnMenuItemClickListener {
            val current = PreferenceManager.getDefaultSharedPreferences(requireActivity()).getInt(SORT_KEY, SORT_DEFAULT)
            AlertDialog.Builder(requireContext())
                    .setTitle(resources.getString(R.string.choose_sort))
                    .setSingleChoiceItems(R.array.sortingArray, current) { _, j ->
                        PreferenceManager.getDefaultSharedPreferences(requireContext()).edit().putInt(SORT_KEY, j).apply()
                        viewModel.setSortMode(j)
                    }
                    .setPositiveButton(R.string.ok) { _, _ ->

                    }
                    .create().show()
            true
        }

    }

    private fun editRider(riderId: Long){
        Toast.makeText(requireContext(), "Edit Rider $riderId", Toast.LENGTH_SHORT).show()
    }

}