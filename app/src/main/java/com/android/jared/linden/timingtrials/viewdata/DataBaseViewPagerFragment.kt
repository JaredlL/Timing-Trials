package com.android.jared.linden.timingtrials.viewdata

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.android.jared.linden.timingtrials.IFabCallbacks
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.data.ITEM_COURSE
import com.android.jared.linden.timingtrials.data.ITEM_RIDER
import com.android.jared.linden.timingtrials.data.ITEM_TIMETRIAL
import com.android.jared.linden.timingtrials.databinding.FragmentDatabaseViewPagerBinding
import com.android.jared.linden.timingtrials.domain.Filter
import com.android.jared.linden.timingtrials.util.EventObserver
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator


class DataBaseViewPagerFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentDatabaseViewPagerBinding.inflate(inflater, container, false)
        val tabLayout = binding.tabs
        val viewPager = binding.viewPager2
        viewPager.adapter = TimeTrialDBPagerAdapter(this)
        setHasOptionsMenu(true)

        // Set the icon and text for each tab
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.setIcon(getTabIcon(position))
            tab.text = getTabTitle(position)
        }.attach()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                setFabStatus(position)
            }
        })
        val listViewModel = requireActivity().getViewModel { requireActivity().injector.listViewModel() }
        listViewModel.setFilter(Filter(""))

        return binding.root
    }

    private fun getTabIcon(position: Int): Int {
        return when (position) {
            RIDER_PAGE_INDEX -> R.drawable.ic_directions_bike_black_24dp
            COURSE_PAGE_INDEX -> R.drawable.ic_directions_black_24dp
            TIMETRIAL_PAGE_INDEX -> R.drawable.ic_timer_black_24dp
            else -> throw IndexOutOfBoundsException()
        }
    }

    private fun getTabTitle(position: Int): String? {
        return when (position) {
            RIDER_PAGE_INDEX -> getString(R.string.riders)
            COURSE_PAGE_INDEX -> getString(R.string.courses)
            TIMETRIAL_PAGE_INDEX->getString(R.string.time_trials)
            else -> null
        }
    }


    private  fun setFabStatus(position: Int){
        val act = requireActivity() as IFabCallbacks
        act.setVisibility(View.VISIBLE)
        act.setImage(R.drawable.ic_add_white_24dp)
        when (position) {
            RIDER_PAGE_INDEX -> {
                act.setAction {
                val action = DataBaseViewPagerFragmentDirections.actionDataBaseViewPagerFragment2ToEditRiderFragment(0)
                findNavController().navigate(action)
            }
                act.setImage(R.drawable.ic_add_white_24dp)
            }
            COURSE_PAGE_INDEX -> {
                act.setImage(R.drawable.ic_add_white_24dp)
                act.setAction {
                    val action = DataBaseViewPagerFragmentDirections.actionDataBaseViewPagerFragmentToEditCourseFragment(0, requireActivity().getString(R.string.new_course))
                    findNavController().navigate(action)
                }
            }
            TIMETRIAL_PAGE_INDEX->{
                act.setImage(R.drawable.ic_timer_white_24dp)
                act.setAction {
                val viewModel = requireActivity().getViewModel { requireActivity().injector.listViewModel() }
                viewModel.timeTrialInsertedEvent.observe(viewLifecycleOwner, EventObserver {
                    val action = DataBaseViewPagerFragmentDirections.actionDataBaseViewPagerFragmentToSelectCourseFragment2(it)
                    findNavController().navigate(action)
                })
                viewModel.insertNewTimeTrial()
            }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_database, menu)

        val searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.app_bar_search).actionView as SearchView).apply {
            // Assumes current activity is the searchable activity
            setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
            isIconified=false // Do not iconify the widget; expand it by default
            isIconifiedByDefault = false
            val listViewModel = requireActivity().getViewModel { requireActivity().injector.listViewModel() }

            setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(searchText: String?): Boolean {
                    //val listViewModel = requireActivity().getViewModel { requireActivity().injector.listViewModel() }
                    listViewModel.setFilter(Filter(searchText?:""))
                    return true
                }

                override fun onQueryTextChange(searchText: String?): Boolean {
                   //val listViewModel = requireActivity().getViewModel { requireActivity().injector. listViewModel() }
                    listViewModel.setFilter(Filter(searchText?:""))
                    return true
                }

            })
        }



    }

}


const val TIMETRIAL_PAGE_INDEX = 0
const val RIDER_PAGE_INDEX = 1
const val COURSE_PAGE_INDEX = 2


class TimeTrialDBPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    /**
     * Mapping of the ViewPager page indexes to their respective Fragments
     */
    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
            RIDER_PAGE_INDEX to { GenericListFragment.newInstance(ITEM_RIDER) },
            COURSE_PAGE_INDEX to { GenericListFragment.newInstance(ITEM_COURSE) },
            TIMETRIAL_PAGE_INDEX to {GenericListFragment.newInstance(ITEM_TIMETRIAL)}
    )

    override fun getItemCount() = tabFragmentsCreators.size

    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }

}