package com.android.jared.linden.timingtrials.setup


import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.android.jared.linden.timingtrials.IFabCallbacks
import com.android.jared.linden.timingtrials.MainActivity
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.databinding.FragmentDatabaseViewPagerBinding
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayoutMediator


class SetupViewPagerFragment: Fragment() {


    //private lateinit var setupViewModel: SetupViewModel

    private val args: SetupViewPagerFragmentArgs by navArgs()

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

        setHasOptionsMenu(true)

        viewPager.offscreenPageLimit = 2
        // Set the icon and text for each tab
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.setIcon(getTabIcon(position))
            tab.text = getTabTitle(position)
            //manageFabVisibility(position)
        }.attach()




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
                optionsMenu?.clear()
                setMenuSearch()
            }
            ORDER_RIDER_INDEX ->  {
                act.setVisibility(View.GONE)
                optionsMenu?.clear()
            }
            TIMETRIAL_PAGE_INDEX-> {
                act.setVisibility(View.GONE)
                optionsMenu?.clear()
            }
        }
    }

    var optionsMenu: Menu? = null
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_database, menu)
        optionsMenu = menu
    }

    private fun setMenuSearch(){
        val searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val b = optionsMenu?.findItem(R.id.app_bar_search)
        (optionsMenu?.findItem(R.id.app_bar_search)?.actionView as SearchView).apply {
            // Assumes current activity is the searchable activity
            setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
            isIconified=false // Do not iconify the widget; expand it by default
            isIconifiedByDefault = false


            setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(searchText: String?): Boolean {
                    //val listViewModel = requireActivity().getViewModel { requireActivity().injector.listViewModel() }
                    //listViewModel.setFilterString(searchText?:"")
                    return true
                }

                override fun onQueryTextChange(searchText: String?): Boolean {
                    //val listViewModel = requireActivity().getViewModel { requireActivity().injector.listViewModel() }
                    //listViewModel.setFilterString(searchText?:"")
                    return true
                }

            })
        }
    }

//    private fun setupNewFab() {
//        val rootCoordinator: CoordinatorLayout = (activity as MainActivity).rootCoordinator
//        val inflater = requireActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//        val fab = inflater.inflate(R.layout.my_fab_definition, rootCoordinator, false) as FloatingActionButton
//        fab.setOnClickListener { v: View? -> myAction() }
//        rootCoordinator.addView(fab)
//    }

//    private fun manageFabVisibility(position: Int){
//        when (position) {
//            RIDER_PAGE_INDEX -> (requireActivity() as MainActivity).mMainFab.visibility = View.VISIBLE
//            ORDER_RIDER_INDEX -> (requireActivity() as MainActivity).mMainFab.visibility = View.GONE
//            TIMETRIAL_PAGE_INDEX -> (requireActivity() as MainActivity).mMainFab.visibility = View.GONE
//            else -> throw IndexOutOfBoundsException()
//        }
//    }

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