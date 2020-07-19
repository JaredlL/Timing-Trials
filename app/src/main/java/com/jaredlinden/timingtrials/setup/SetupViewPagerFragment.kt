package com.jaredlinden.timingtrials.setup


import android.app.SearchManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.jaredlinden.timingtrials.IFabCallbacks
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.data.NumberMode
import com.jaredlinden.timingtrials.data.NumbersDirection
import com.jaredlinden.timingtrials.databinding.FragmentDatabaseViewPagerBinding
import com.jaredlinden.timingtrials.databinding.FragmentNumberOptionsBinding
import com.jaredlinden.timingtrials.util.EventObserver
import com.jaredlinden.timingtrials.util.getViewModel
import com.jaredlinden.timingtrials.util.injector
import kotlinx.android.synthetic.main.fragment_number_options.*


const val SORT_RECENT_ACTIVITY = 0
const val SORT_ALPHABETICAL = 1
const val SORT_KEY = "sorting"

class SetupViewPagerFragment: Fragment() {


    //private lateinit var setupViewModel: SetupViewModel

    private val args: SetupViewPagerFragmentArgs by navArgs()



    var setupMenu: Menu? = null

    lateinit var prefListner : SharedPreferences.OnSharedPreferenceChangeListener
    lateinit var viewPager : ViewPager2

//    override fun onResume() {
//        super.onResume()
//        viewPager.adapter = SetupPagerAdapter(this)
//    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val setupViewModel = requireActivity().getViewModel { requireActivity().injector.timeTrialSetupViewModel() }
        val binding = FragmentDatabaseViewPagerBinding.inflate(inflater, container, false)
        val tabLayout = binding.tabs
        viewPager = binding.viewPager2
        val pagerAdapter = SetupPagerAdapter(this){
            setFabStatus(setupViewModel.currentPage)
        }
        viewPager.adapter = pagerAdapter


        setupViewModel.changeTimeTrial(args.timeTrialId)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                setFabStatus(position)
                setupViewModel.currentPage = position
            }
        })

//        setupViewModel.orderRidersViewModel.numberRulesMediator.observe(requireActivity(), Observer {
//
//        })

        setHasOptionsMenu(true)

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
        val act = requireActivity() as IFabCallbacks
        when (position) {

            RIDER_PAGE_INDEX -> {
                act.setVisibility(View.VISIBLE)
                act.setImage(R.drawable.ic_add_white_24dp)
                setHasOptionsMenu(true)
                setupMenu?.let {
                    it.findItem(R.id.settings_app_bar_search)?.isVisible = true
                    it.findItem(R.id.settings_menu_sort)?.isVisible = true
                }
            }
            ORDER_RIDER_INDEX ->  {
                act.setVisibility(View.GONE)
                setHasOptionsMenu(true)
                setupMenu?.let {
                    it.findItem(R.id.settings_app_bar_search)?.isVisible = false
                    it.findItem(R.id.settings_menu_sort)?.isVisible = true
                }

            }
            TIMETRIAL_PAGE_INDEX-> {
                act.setVisibility(View.GONE)
                setupMenu?.let {
                    it.findItem(R.id.settings_app_bar_search)?.isVisible = false
                    it.findItem(R.id.settings_menu_sort)?.isVisible = true
                }
                //setHasOptionsMenu(false)

            }
        }
    }


    var searchView: SearchView? = null

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        setupMenu = menu
        inflater.inflate(R.menu.menu_setup, menu)
        menu.findItem(R.id.settings_app_bar_search).isVisible = true
        val searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.settings_app_bar_search).actionView as SearchView).apply {
            searchView = this
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

        menu.findItem(R.id.settings_menu_sort).setOnMenuItemClickListener {
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

        menu.findItem(R.id.settings_menu_number_options).setOnMenuItemClickListener {
            val action = SetupViewPagerFragmentDirections.actionSetupViewPagerFragmentToNumberOptionsDialog()
            findNavController().navigate(action)
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


const val TIMETRIAL_PAGE_INDEX = 2
const val RIDER_PAGE_INDEX = 0
const val ORDER_RIDER_INDEX = 1


class SetupPagerAdapter(fragment: Fragment, val fragCreated: () -> Unit) : FragmentStateAdapter(fragment) {

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
        fragCreated()
        return f
    }
}

