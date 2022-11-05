package com.jaredlinden.timingtrials.viewdata

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.jaredlinden.timingtrials.IFabCallbacks
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.data.NumberMode
import com.jaredlinden.timingtrials.databinding.FragmentDatabaseViewPagerBinding
import com.jaredlinden.timingtrials.domain.Filter
import com.jaredlinden.timingtrials.setup.SetupViewModel
import com.jaredlinden.timingtrials.util.*
import com.jaredlinden.timingtrials.viewdata.listfragment.CourseListFragment
import com.jaredlinden.timingtrials.viewdata.listfragment.RiderListFragment
import com.jaredlinden.timingtrials.viewdata.listfragment.TimeTrialListFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DataBaseViewPagerFragment: Fragment() {

    var mViewPager: ViewPager2? = null

    lateinit var tabLayoutMediator: TabLayoutMediator


    private val mPageChangeCallback = object :ViewPager2.OnPageChangeCallback(){
        override fun onPageSelected(position: Int) {
            setFabStatus(position)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentDatabaseViewPagerBinding.inflate(inflater, container, false)
        val tabLayout = binding.tabs
        mViewPager = binding.viewPager2
        val viewpager = binding.viewPager2
        viewpager.adapter = TimeTrialDBPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
        setHasOptionsMenu(true)



        // Set the icon and text for each tab
        tabLayoutMediator = TabLayoutMediator(tabLayout, viewpager) { tab, position ->
            tab.setIcon(getTabIcon(position))
            tab.text = getTabTitle(position)
        }
        tabLayoutMediator.attach()

        viewpager.registerOnPageChangeCallback(mPageChangeCallback)
        val listViewModel:ListViewModel by viewModels()
        listViewModel.setFilter(Filter(""))

        val setupViewModel:SetupViewModel by viewModels()
        setupViewModel.currentPage = 0

        (activity as? IFabCallbacks)?.fabClickEvent?.observe(viewLifecycleOwner, EventObserver{
            if(it){
                when(viewpager.currentItem){
                    RIDER_PAGE_INDEX -> {
                            val action = DataBaseViewPagerFragmentDirections.actionDataBaseViewPagerFragment2ToEditRiderFragment(0)
                            findNavController().navigate(action)
                    }
                    COURSE_PAGE_INDEX -> {
                            val action = DataBaseViewPagerFragmentDirections.actionDataBaseViewPagerFragmentToEditCourseFragment(requireActivity().getString(R.string.new_course), 0)
                            findNavController().navigate(action)
                    }
                    TIMETRIAL_PAGE_INDEX->{

                        listViewModel.timeTrialInsertedEvent.observe(viewLifecycleOwner, EventObserver {
                                val action = DataBaseViewPagerFragmentDirections.actionDataBaseViewPagerFragmentToSelectCourseFragment2(it)
                                findNavController().navigate(action)
                            })
                            val mode = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString(PREF_NUMBERING_MODE, NumberMode.INDEX.name)?.let {
                                NumberMode.valueOf(it)
                            } ?: NumberMode.INDEX
                        listViewModel.insertNewTimeTrial(mode)
                    }
                }
            }
        })

        return binding.root
    }

    override fun onDestroyView() {
        tabLayoutMediator.detach()
        mViewPager?.unregisterOnPageChangeCallback(mPageChangeCallback)
        sv?.setOnQueryTextListener(null)
        mViewPager?.adapter = null
        super.onDestroyView()
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
        (activity as? IFabCallbacks)?.let { act->
            act.setFabVisibility(View.VISIBLE)
            act.setFabImage(R.drawable.ic_add_white_24dp)
            when (position) {
                RIDER_PAGE_INDEX -> {
                    act.setFabImage(R.drawable.ic_add_white_24dp)
                }
                COURSE_PAGE_INDEX -> {
                    act.setFabImage(R.drawable.ic_add_white_24dp)
                }
                TIMETRIAL_PAGE_INDEX->{
                    act.setFabImage(R.drawable.ic_timer_white_24dp)
                }
            }
        }

    }



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

    var sv: SearchView? = null
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_database, menu)

        val searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager

        menu.findItem(R.id.app_bar_search).setOnActionExpandListener(expandListener)

        (menu.findItem(R.id.app_bar_search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
           isIconifiedByDefault = false
            val listViewModel:ListViewModel by viewModels()

            sv = this
            setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(searchText: String?): Boolean {
                    listViewModel.setFilter(Filter(searchText?:""))
                    clearFocus()
                    return true
                }

                override fun onQueryTextChange(searchText: String?): Boolean {
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


class TimeTrialDBPagerAdapter(fm: FragmentManager, ls:Lifecycle) : FragmentStateAdapter(fm,ls) {

    /**
     * Mapping of the ViewPager page indexes to their respective Fragments
     */
    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
            RIDER_PAGE_INDEX to { RiderListFragment() },
            COURSE_PAGE_INDEX to { CourseListFragment() },
            TIMETRIAL_PAGE_INDEX to { TimeTrialListFragment() }
    )

    override fun getItemCount() = tabFragmentsCreators.size

    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }

}