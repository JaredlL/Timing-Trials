package com.jaredlinden.timingtrials.setup


import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import com.jaredlinden.timingtrials.IFabCallbacks
import com.jaredlinden.timingtrials.R

import com.jaredlinden.timingtrials.adapters.SelectableRiderListAdapter
import com.jaredlinden.timingtrials.data.*
import com.jaredlinden.timingtrials.databinding.FragmentSelectriderListBinding
import com.jaredlinden.timingtrials.edititem.EditResultViewModel
import com.jaredlinden.timingtrials.util.*
import dagger.hilt.android.AndroidEntryPoint


/**
 * A simple [Fragment] subclass.
 * Use the [SelectRidersFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */

@AndroidEntryPoint
class SelectRidersFragment : Fragment() {



    private val args: SelectRidersFragmentArgs by navArgs()
    private lateinit var viewModel: ISelectRidersViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        val singleRiderVm: EditResultViewModel by activityViewModels()
        val setupViewModel: SetupViewModel by activityViewModels()

        viewModel = if(args.selectionMode == SELECT_RIDER_FRAGMENT_MULTI) {
            setupViewModel.selectRidersViewModel
        }else{
            singleRiderVm.selectRiderVm
        }

        val viewManager = LinearLayoutManager(context)
        val adapter = SelectableRiderListAdapter(requireContext())

        adapter.setHasStableIds(true)
        adapter.editRider = ::editRider

        val binding = FragmentSelectriderListBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            riderHeading.rider =  Rider.createBlank().copy( firstName = getString(R.string.name), club = getString(R.string.club))
            riderHeading.checkBox.visibility =  View.INVISIBLE
            riderRecyclerView.adapter = adapter
            riderRecyclerView.layoutManager = viewManager
        }

        if(args.selectionMode == SELECT_RIDER_FRAGMENT_MULTI){
            setHasOptionsMenu(false)
            if(setupViewModel.currentPage == RIDER_PAGE_INDEX ){
                (requireActivity() as IFabCallbacks).apply {
                    setFabVisibility(View.VISIBLE)
                    setFabImage(R.drawable.ic_add_white_24dp)
                }
            }
        }else{
            setHasOptionsMenu(true)
            (requireActivity() as IFabCallbacks).apply {
                setFabVisibility(View.VISIBLE)
                setFabImage(R.drawable.ic_add_white_24dp)

            }
        }

        (requireActivity() as IFabCallbacks).fabClickEvent.observe(viewLifecycleOwner, EventObserver {
            if(it){
                editRider(0)
            }

        })

        adapter.addRiderToSelection = {
            viewModel.riderSelected(it)
        }

        adapter.removeRiderFromSelection = {
            viewModel.riderUnselected(it)
        }

        viewModel.selectedRidersInformation.observe(viewLifecycleOwner, Observer {result->
            result?.let {
                adapter.setRiders(it)

                if(args.selectionMode != SELECT_RIDER_FRAGMENT_MULTI){
                    result.selectedIds.firstOrNull()?.let {fs->
                        val pos = result.allRiderList.indexOfFirst { it.id == fs }
                        if(pos >=0) viewManager.scrollToPositionWithOffset(pos, binding.root.height / 2)
                        //viewManager.scrollToPosition()
                    }

                }

            }

        })

        viewModel.close.observe(viewLifecycleOwner, EventObserver{
            if(it){
                findNavController().popBackStack()
            }
        })

        viewModel.showMessage.observe(viewLifecycleOwner, EventObserver{
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        })


        viewModel.setSortMode(PreferenceManager.getDefaultSharedPreferences(requireActivity()).getInt(SORT_KEY, SORT_DEFAULT))

        return binding.root
    }

    var sv: SearchView? = null
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
        inflater.inflate(R.menu.menu_setup, menu)
        val svMenuItem = menu.findItem(R.id.settings_app_bar_search)
        svMenuItem.setOnActionExpandListener(expandListener)
        svMenuItem.isVisible = true
        menu.findItem(R.id.settings_menu_number_options).isVisible = false
        val searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (svMenuItem.actionView as SearchView).apply {
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

        menu.findItem(R.id.settings_menu_sort).setOnMenuItemClickListener {
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
        if(findNavController().currentDestination?.id == R.id.selectRidersFragment){
            val action = SelectRidersFragmentDirections.actionSelectRidersFragmentToEditRiderFragment(riderId)
            findNavController().navigate(action)
        }else{
            val action = SetupViewPagerFragmentDirections.actionSetupViewPagerFragment2ToEditRiderFragment(riderId)
            findNavController().navigate(action)
        }

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
