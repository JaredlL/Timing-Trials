package com.android.jared.linden.timingtrials.setup

import android.content.Intent


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.data.TimeTrialStatus
import com.android.jared.linden.timingtrials.databinding.FragmentDatabaseViewPagerBinding
import com.android.jared.linden.timingtrials.timing.TimingActivity
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator

class SetupViewPagerFragment: Fragment() {


    //private lateinit var setupViewModel: SetupViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentDatabaseViewPagerBinding.inflate(inflater, container, false)
        val tabLayout = binding.tabs
        val viewPager = binding.viewPager2
        viewPager.adapter = SetupPagerAdapter(this)

        viewPager.offscreenPageLimit = 2
        // Set the icon and text for each tab
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.setIcon(getTabIcon(position))
            tab.text = getTabTitle(position)
        }.attach()


        return binding.root
    }

    private fun getTabIcon(position: Int): Int {
        return when (position) {
            RIDER_PAGE_INDEX -> R.drawable.ic_directions_bike_black_24dp
            ORDER_RIDER_INDEX -> R.drawable.ic_filter_1_black_24dp
            TIMETRIAL_PAGE_INDEX -> R.drawable.ic_timer_black_24dp
            else -> throw IndexOutOfBoundsException()
        }
    }

    private fun getTabTitle(position: Int): String? {
        return when (position) {
            RIDER_PAGE_INDEX -> getString(R.string.select_riders)
            ORDER_RIDER_INDEX -> getString(R.string.order_riders)
            TIMETRIAL_PAGE_INDEX->getString(R.string.setup_timetrial)
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