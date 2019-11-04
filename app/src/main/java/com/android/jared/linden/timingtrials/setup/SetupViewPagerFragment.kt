package com.android.jared.linden.timingtrials.setup

import android.content.Intent
import com.android.jared.linden.timingtrials.viewdata.GenericListFragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.data.ITEM_COURSE
import com.android.jared.linden.timingtrials.data.ITEM_RIDER
import com.android.jared.linden.timingtrials.data.ITEM_TIMETRIAL
import com.android.jared.linden.timingtrials.data.TimeTrialStatus
import com.android.jared.linden.timingtrials.databinding.FragmentDatabaseViewPagerBinding
import com.android.jared.linden.timingtrials.timing.TimingActivity
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector
import com.android.jared.linden.timingtrials.viewdata.COURSE_PAGE_INDEX
import androidx.lifecycle.Observer
import com.google.android.material.tabs.TabLayoutMediator
import org.threeten.bp.OffsetDateTime

class SetupViewPagerFragment: Fragment() {


    private lateinit var setupViewModel: SetupViewModel

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

        //(activity as AppCompatActivity).setSupportActionBar(binding.toolbar)

        setupViewModel = getViewModel { injector.timeTrialSetupViewModel() }

        setupViewModel.timeTrial.observe(this, Observer { tt->
            tt?.let {
                if(tt.timeTrialHeader.status == TimeTrialStatus.IN_PROGRESS){
                    val intent = Intent(requireActivity(), TimingActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
            }
        })

        setupViewModel.timeTrialPropertiesViewModel.onBeginTt = {

            setupViewModel.timeTrial.value?.let {
                if(it.riderList.count() == 0){
                    Toast.makeText(requireActivity(), "TT Needs at least 1 rider", Toast.LENGTH_LONG).show()
                    //container.currentItem = 1
                    return@let
                }
                if(it.timeTrialHeader.startTime.isBefore(OffsetDateTime.now())){
                    Toast.makeText(requireActivity(), "TT must start in the future, select start time", Toast.LENGTH_LONG).show()
                    TimePickerFragment().show(requireActivity().supportFragmentManager, "timePicker")
                    return@let
                }
                val confDialog: SetupConfirmationFragment = requireActivity().supportFragmentManager
                        .findFragmentByTag("confdialog") as? SetupConfirmationFragment ?: SetupConfirmationFragment()

                if(confDialog.dialog?.isShowing != true){
                    confDialog.show(requireActivity().supportFragmentManager, "confdialog")
                }

            }

        }

        return binding.root
    }

    private fun getTabIcon(position: Int): Int {
        return when (position) {
            RIDER_PAGE_INDEX -> R.drawable.ic_action_done
            ORDER_RIDER_INDEX -> R.drawable.ic_dashboard_black_24dp
            TIMETRIAL_PAGE_INDEX -> R.drawable.ic_home_black_24dp
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