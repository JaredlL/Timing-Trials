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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.jaredlinden.timingtrials.IFabCallbacks
import com.jaredlinden.timingtrials.R

import com.jaredlinden.timingtrials.adapters.SelectableRiderListAdapter
import com.jaredlinden.timingtrials.data.*
import com.jaredlinden.timingtrials.databinding.FragmentSelectriderListBinding
import com.jaredlinden.timingtrials.edititem.EditResultFragmentArgs
import com.jaredlinden.timingtrials.util.*
import kotlinx.android.synthetic.main.fragment_selectrider_list.*


/**
 * A simple [Fragment] subclass.
 * Use the [SelectRidersFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */


class SelectRidersFragment : Fragment() {



    private val args: SelectRidersFragmentArgs by navArgs()

    private lateinit var viewModel: ISelectRidersViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        viewModel = if(args.selectionMode == SELECT_RIDER_FRAGMENT_MULTI) {
            requireActivity().getViewModel { requireActivity().injector.timeTrialSetupViewModel() }.selectRidersViewModel

        }else{
            requireActivity().getViewModel { requireActivity().injector.editResultViewModel() }.selectRiderVm
            //setHasOptionsMenu(true)
        }



        val viewManager = LinearLayoutManager(context)
        val adapter = SelectableRiderListAdapter(requireContext())

        adapter.setHasStableIds(true)
        adapter.editRider = ::editRider



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

        if(args.selectionMode == SELECT_RIDER_FRAGMENT_MULTI){
            setHasOptionsMenu(false)
            if(requireActivity().getViewModel { requireActivity().injector.timeTrialSetupViewModel() }.currentPage == RIDER_PAGE_INDEX ){
                (requireActivity() as IFabCallbacks).apply {
                    setVisibility(View.VISIBLE)
                    setImage(R.drawable.ic_add_white_24dp)
                    setAction {
                        editRider(0)
                    }
                }
            }
        }else{
            setHasOptionsMenu(true)
            (requireActivity() as IFabCallbacks).apply {
                setVisibility(View.VISIBLE)
                setImage(R.drawable.ic_add_white_24dp)
                setAction {
                    editRider(0)
                }
            }
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
            //Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
        })




        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_setup, menu)
        menu.findItem(R.id.settings_app_bar_search).isVisible = true
        menu.findItem(R.id.settings_menu_number_options).isVisible = false
        val searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.settings_app_bar_search).actionView as SearchView).apply {
            // Assumes current activity is the searchable activity
            setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
            isIconified = false // Do not iconify the widget; expand it by default
            isIconifiedByDefault = false
            setQuery(viewModel.riderFilter.value ?: "", false)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(searchText: String?): Boolean {
                    //val listViewModel = requireActivity().getViewModel { requireActivity().injector.listViewModel() }
                    viewModel.setRiderFilter(searchText ?: "")
                    return true
                }

                override fun onQueryTextChange(searchText: String?): Boolean {
                    //val listViewModel = requireActivity().getViewModel { requireActivity().injector. listViewModel() }
                    viewModel.setRiderFilter(searchText ?: "")
                    return true
                }

            })
        }

        menu.findItem(R.id.settings_menu_ordering).setOnMenuItemClickListener {
            val current = PreferenceManager.getDefaultSharedPreferences(requireActivity()).getInt(SORT_KEY, SORT_RECENT_ACTIVITY)
            AlertDialog.Builder(requireContext())
                    .setTitle(resources.getString(R.string.choose_sort))
                    //.setMessage("Choose Sorting")
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
