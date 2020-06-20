package com.jaredlinden.timingtrials.setup


import android.app.SearchManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
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
import com.jaredlinden.timingtrials.IFabCallbacks
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.data.NumbersDirection
import com.jaredlinden.timingtrials.databinding.FragmentDatabaseViewPagerBinding
import com.jaredlinden.timingtrials.databinding.FragmentNumberOptionsBinding
import com.jaredlinden.timingtrials.util.getViewModel
import com.jaredlinden.timingtrials.util.injector
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_number_options.*


const val SORT_RECENT_ACTIVITY = 0
const val SORT_ALPHABETICAL = 1
const val SORT_KEY = "sorting"

class SetupViewPagerFragment: Fragment() {


    //private lateinit var setupViewModel: SetupViewModel

    private val args: SetupViewPagerFragmentArgs by navArgs()



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


const val TIMETRIAL_PAGE_INDEX = 0
const val RIDER_PAGE_INDEX = 1
const val ORDER_RIDER_INDEX = 2


class SetupPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

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
        return tabFragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }
}

class NumberOptionsDialog: DialogFragment(){



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mViewModel = requireActivity().getViewModel { requireActivity().injector.timeTrialSetupViewModel() }.orderRidersViewModel

        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_AppCompat_Dialog_Alert)

        val binding = DataBindingUtil.inflate<FragmentNumberOptionsBinding>(inflater, R.layout.fragment_number_options, container, false).apply {
            viewModel = mViewModel
            radioGroup.setOnCheckedChangeListener { group, checkedId ->
                when(checkedId){
                    ascendingRadioButton.id -> mViewModel.numberDirection.value = NumbersDirection.ASCEND
                    else -> mViewModel.numberDirection.value = NumbersDirection.DESCEND
                }
            }
            button.setOnClickListener {
                this@NumberOptionsDialog.dismiss()
            }
        }

        mViewModel.numberRulesMediator.observe(viewLifecycleOwner, Observer {
            val n = it
        })
        mViewModel.numberDirection.observe(viewLifecycleOwner, Observer {
            it?.let {
                when(it){
                    NumbersDirection.ASCEND -> {
                        binding.radioGroup.check(ascendingRadioButton.id)
                    }
                    else ->{
                        binding.radioGroup.check(descendingRadioButton.id)

                    }

                }


            }
        })

        return binding.root
    }




}