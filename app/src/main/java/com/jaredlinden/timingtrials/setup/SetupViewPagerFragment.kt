package com.jaredlinden.timingtrials.setup


import android.app.SearchManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.jaredlinden.timingtrials.IFabCallbacks
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.databinding.FragmentDatabaseViewPagerBinding
import com.jaredlinden.timingtrials.util.EventObserver
import com.jaredlinden.timingtrials.util.showKeyboard
import dagger.hilt.android.AndroidEntryPoint


const val SORT_RECENT_ACTIVITY = 0
const val SORT_ALPHABETICAL = 1
const val SORT_DEFAULT = SORT_ALPHABETICAL
const val SORT_KEY = "sorting"

@AndroidEntryPoint
class SetupViewPagerFragment: Fragment() {


    private val setupViewModel: SetupViewModel by activityViewModels()

    private val args: SetupViewPagerFragmentArgs by navArgs()

    lateinit var tabLayoutMediator: TabLayoutMediator

    var setupMenu: Menu? = null

    lateinit var prefListner : SharedPreferences.OnSharedPreferenceChangeListener
    lateinit var viewPager : ViewPager2

    private val mPageChangeCallback = object :ViewPager2.OnPageChangeCallback(){
        override fun onPageSelected(position: Int) {
            setFabStatus(position)
            setupViewModel.currentPage = position
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentDatabaseViewPagerBinding.inflate(inflater, container, false)
        val tabLayout = binding.tabs
        viewPager = binding.viewPager2
        val pagerAdapter = SetupPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle){
            setFabStatus(setupViewModel.currentPage)
        }
        viewPager.adapter = pagerAdapter


        setupViewModel.changeTimeTrial(args.timeTrialId)

        viewPager.registerOnPageChangeCallback(mPageChangeCallback)


        setHasOptionsMenu(true)

        viewPager.offscreenPageLimit = 2
        // Set the icon and text for each tab
        tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.setIcon(getTabIcon(position))
            tab.text = getTabTitle(position)
            //manageFabVisibility(position)
        }
        tabLayoutMediator.attach()

        prefListner =  object : SharedPreferences.OnSharedPreferenceChangeListener{
            override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
                setupViewModel.selectRidersViewModel.setSortMode(p0?.getInt(SORT_KEY, 0)?:0)
            }
        }
        val prefMan = PreferenceManager.getDefaultSharedPreferences(requireContext())
        prefMan.registerOnSharedPreferenceChangeListener(prefListner)
        prefMan.getInt(SORT_KEY, 0).let {
            setupViewModel.selectRidersViewModel.setSortMode(it)
        }


        (activity as? IFabCallbacks)?.fabClickEvent?.observe(viewLifecycleOwner, EventObserver{
            if(it && viewPager.currentItem == RIDER_PAGE_INDEX){
                val action = SetupViewPagerFragmentDirections.actionSetupViewPagerFragment2ToEditRiderFragment(0)
                findNavController().navigate(action)
            }

        })

        viewPager.setCurrentItem(setupViewModel.currentPage, false)
        setFabStatus(setupViewModel.currentPage)


        return binding.root
    }

    fun setFabStatus(position: Int){
        val act = requireActivity() as IFabCallbacks?
        when (position) {

            RIDER_PAGE_INDEX -> {
                act?.setFabVisibility(View.VISIBLE)
                act?.setFabImage(R.drawable.ic_add_white_24dp)
                setHasOptionsMenu(true)
                setupMenu?.let {
                    it.findItem(R.id.settings_app_bar_search)?.isVisible = true
                    it.findItem(R.id.settings_menu_sort)?.isVisible = true
                }
            }
            ORDER_RIDER_INDEX ->  {
                act?.setFabVisibility(View.GONE)
                setHasOptionsMenu(true)
                setupMenu?.let {
                    it.findItem(R.id.settings_app_bar_search)?.isVisible = false
                    it.findItem(R.id.settings_menu_sort)?.isVisible = true
                }

            }
            TIMETRIAL_PAGE_INDEX-> {
                act?.setFabVisibility(View.GONE)
                setupMenu?.let {
                    it.findItem(R.id.settings_app_bar_search)?.isVisible = false
                    it.findItem(R.id.settings_menu_sort)?.isVisible = true
                }
                //setHasOptionsMenu(false)

            }
        }
    }


    var searchView: SearchView? = null

//
//    val closeListner = object : SearchView.OnCloseListener{
//        override fun onClose(): Boolean {
//            val view = activity?.currentFocus
//            view?.let { v ->
//                val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
//                imm?.hideSoftInputFromWindow(v.windowToken, 0)
//            }
//            return true
//        }
//
//    }


    val expandListener = object : MenuItem.OnActionExpandListener {
        override fun onMenuItemActionCollapse(item: MenuItem): Boolean {

            searchView?.clearFocus()
            return true // Return true to collapse action view
        }

        override fun onMenuItemActionExpand(item: MenuItem): Boolean {
            // Do something when expanded
            searchView?.requestFocus()
            showKeyboard()
            return true // Return true to expand action view
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        setupMenu = menu
        inflater.inflate(R.menu.menu_setup, menu)
        menu.findItem(R.id.settings_app_bar_search).setOnActionExpandListener(expandListener)
        menu.findItem(R.id.settings_app_bar_search).isVisible = true
        val searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.settings_app_bar_search).actionView as SearchView).apply {
            searchView = this
            // Assumes current activity is the searchable activity
            setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))

            isIconifiedByDefault = false
            val selectRiderVm = setupViewModel.selectRidersViewModel
            setQuery(selectRiderVm.riderFilter.value?:"", false)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(searchText: String?): Boolean {
                    setupViewModel.selectRidersViewModel.setRiderFilter(searchText?:"")
                    clearFocus()
                    return true
                }

                override fun onQueryTextChange(searchText: String?): Boolean {
                    setupViewModel.selectRidersViewModel.setRiderFilter(searchText?:"")
                    return true
                }

            })
        }

        menu.findItem(R.id.settings_menu_sort).setOnMenuItemClickListener {
            val current = PreferenceManager.getDefaultSharedPreferences(requireActivity()).getInt(SORT_KEY, SORT_RECENT_ACTIVITY)
            AlertDialog.Builder(requireContext())
                    .setTitle(resources.getString(R.string.choose_sort))
                    .setIcon(R.mipmap.tt_logo_round)
                    //.setMessage("Choose Sorting")
                    .setSingleChoiceItems(R.array.sortingArray, current) { _, j->
                        PreferenceManager.getDefaultSharedPreferences(requireContext()).edit().putInt(SORT_KEY, j).apply()
                    }
                    .setPositiveButton(R.string.ok){_,_->

                    }
                    .create().show()
            true
        }

        menu.findItem(R.id.settings_menu_number_options).setOnMenuItemClickListener {
            val action = SetupViewPagerFragmentDirections.actionSetupViewPagerFragmentToNumberOptionsDialog()
            findNavController().navigate(action)
            true
        }

        menu.findItem(R.id.settings_menu_seed_riders).setOnMenuItemClickListener {
            AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.seed_riders))
                    .setIcon(R.mipmap.tt_logo_round)
                    .setMessage(getString(R.string.seed_riders_message))
                    .setPositiveButton(R.string.ok) { _, _->
                        setupViewModel.seedRiders()
                    }.create().show()
            true
        }


    }


    override fun onDestroyView() {

        tabLayoutMediator.detach()
        viewPager?.unregisterOnPageChangeCallback(mPageChangeCallback)
        searchView?.setOnQueryTextListener(null)
        viewPager?.adapter = null
        super.onDestroyView()
    }


    private fun getTabIcon(position: Int): Int {
        return when (position) {
            RIDER_PAGE_INDEX -> R.drawable.ic_done_black_24dp
            ORDER_RIDER_INDEX -> R.drawable.ic_filter_1_black_24dp
            TIMETRIAL_PAGE_INDEX -> R.drawable.ic_build_black_24dp
            else -> throw IndexOutOfBoundsException()
        }
    }

    private fun getTabTitle(position: Int): String? {
        return when (position) {
            RIDER_PAGE_INDEX -> {
                getString(R.string.select_riders)
            }
            ORDER_RIDER_INDEX -> {
                getString(R.string.order_riders)
            }
            TIMETRIAL_PAGE_INDEX->{
                getString(R.string.setup_timetrial)}
            else -> null
        }
    }


}


const val TIMETRIAL_PAGE_INDEX = 2
const val RIDER_PAGE_INDEX = 0
const val ORDER_RIDER_INDEX = 1

class SetupPagerAdapter(fm: FragmentManager, ls: Lifecycle, val fragCreated: () -> Unit) : FragmentStateAdapter(fm,ls) {

    /**
     * Mapping of the ViewPager page indexes to their respective Fragments
     */
    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
            TIMETRIAL_PAGE_INDEX to {SetupTimeTrialFragment.newInstance()},
            RIDER_PAGE_INDEX to { SelectRidersFragment.newInstance(SelectRidersFragmentArgs(SelectRidersFragment.SELECT_RIDER_FRAGMENT_MULTI))},
            ORDER_RIDER_INDEX to { OrderRidersFragment.newInstance() }

    )

    override fun getItemCount() = tabFragmentsCreators.size

    override fun createFragment(position: Int): Fragment {
        val f = tabFragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
        //fragCreated()
        return f
    }
}

