package com.android.jared.linden.timingtrials.setup


import android.app.SearchManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.android.jared.linden.timingtrials.IFabCallbacks
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.databinding.FragmentDatabaseViewPagerBinding
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_database_view_pager.*


class SetupViewPagerFragment: Fragment() {


    //private lateinit var setupViewModel: SetupViewModel

    private val args: SetupViewPagerFragmentArgs by navArgs()

    val SORT_RECENT_ACTIVITY = 0
    val SORT_ALPHABETICAL = 1
    val SORT_KEY = "sorting"

    var setupMenu: Menu? = null

    lateinit var prefListner : SharedPreferences.OnSharedPreferenceChangeListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentDatabaseViewPagerBinding.inflate(inflater, container, false)
        val tabLayout = binding.tabs
        val viewPager = binding.viewPager2
        viewPager.adapter = SetupPagerAdapter(this)

        val setupViewModel = requireActivity().getViewModel { requireActivity().injector.timeTrialSetupViewModel() }

        setupViewModel.changeTimeTrial(args.timeTrialId)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                setFabStatus(position)
            }
        })


        viewPager.offscreenPageLimit = 2
        // Set the icon and text for each tab
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.setIcon(getTabIcon(position))
            tab.text = getTabTitle(position)
            //manageFabVisibility(position)
        }.attach()

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




        return binding.root
    }

    fun setFabStatus(position: Int){
        val act = requireActivity() as IFabCallbacks
        when (position) {

            RIDER_PAGE_INDEX -> {
                act.setVisibility(View.VISIBLE)
                act.setImage(R.drawable.ic_add_white_24dp)
                act.setAction {
                    val action = SetupViewPagerFragmentDirections.actionSetupViewPagerFragment2ToEditRiderFragment(0)
                    findNavController().navigate(action)
                }
                setHasOptionsMenu(true)
                setupMenu?.let {
                    it.findItem(R.id.settings_app_bar_search)?.isVisible = true
                    it.findItem(R.id.settings_menu_ordering)?.isVisible = true
                }
            }
            ORDER_RIDER_INDEX ->  {
                act.setVisibility(View.GONE)
                setHasOptionsMenu(true)
                setupMenu?.let {
                    it.findItem(R.id.settings_app_bar_search)?.isVisible = false
                    it.findItem(R.id.settings_menu_ordering)?.isVisible = true
                }

            }
            TIMETRIAL_PAGE_INDEX-> {
                act.setVisibility(View.GONE)
                setupMenu?.let {
                    it.findItem(R.id.settings_app_bar_search)?.isVisible = false
                    it.findItem(R.id.settings_menu_ordering)?.isVisible = false
                }
                //setHasOptionsMenu(false)

            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        setupMenu = menu
        when(view_pager2.currentItem){
            RIDER_PAGE_INDEX->{
                inflateRiderPageMenu(menu, inflater)
            }
            ORDER_RIDER_INDEX ->  {
                inflateOrderPageMenu(menu, inflater)
            }
            TIMETRIAL_PAGE_INDEX-> {


            }

        }


    }

    private fun inflateOrderPageMenu(menu: Menu, inflater: MenuInflater){
        inflater.inflate(R.menu.menu_setup, menu)
        menu.findItem(R.id.settings_app_bar_search).isVisible = false
    }

    private fun inflateRiderPageMenu(menu: Menu, inflater: MenuInflater){
        inflater.inflate(R.menu.menu_setup, menu)
        menu.findItem(R.id.settings_app_bar_search).isVisible = true
        val searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.settings_app_bar_search).actionView as SearchView).apply {
            // Assumes current activity is the searchable activity
            setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
            isIconified=false // Do not iconify the widget; expand it by default
            isIconifiedByDefault = false
            val selectRiderVm = requireActivity().getViewModel { requireActivity().injector.timeTrialSetupViewModel() }.selectRidersViewModel
            setQuery(selectRiderVm.riderFilter.value?:"", false)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(searchText: String?): Boolean {
                    //val listViewModel = requireActivity().getViewModel { requireActivity().injector.listViewModel() }
                    selectRiderVm.setRiderFilter(searchText?:"")
                    return true
                }

                override fun onQueryTextChange(searchText: String?): Boolean {
                    //val listViewModel = requireActivity().getViewModel { requireActivity().injector. listViewModel() }
                    selectRiderVm.setRiderFilter(searchText?:"")
                    return true
                }

            })
        }

        menu.findItem(R.id.settings_menu_ordering).setOnMenuItemClickListener {
            val current = PreferenceManager.getDefaultSharedPreferences(requireActivity()).getInt(SORT_KEY, SORT_RECENT_ACTIVITY)
            AlertDialog.Builder(requireContext())
                    .setTitle(resources.getString(R.string.choose_sort))
                    //.setMessage("Choose Sorting")
                    .setSingleChoiceItems(R.array.sortingArray, current) { _, j->
                        PreferenceManager.getDefaultSharedPreferences(requireContext()).edit().putInt(SORT_KEY, j).apply()
                    }
                    .setPositiveButton(R.string.ok){_,_->

                    }
                    .create().show()
            true
        }
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


const val TIMETRIAL_PAGE_INDEX = 0
const val RIDER_PAGE_INDEX = 1
const val ORDER_RIDER_INDEX = 2


class SetupPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    /**
     * Mapping of the ViewPager page indexes to their respective Fragments
     */
    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
            TIMETRIAL_PAGE_INDEX to {SetupTimeTrialFragment.newInstance()},
            RIDER_PAGE_INDEX to { SelectRidersFragment.newInstance()},
            ORDER_RIDER_INDEX to { OrderRidersFragment.newInstance() }

    )

    override fun getItemCount() = tabFragmentsCreators.size

    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }
}