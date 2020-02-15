package com.android.jared.linden.timingtrials.viewdata

import android.os.Bundle
import android.view.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
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
import com.android.jared.linden.timingtrials.util.EventObserver
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector
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
            RIDER_PAGE_INDEX -> act.setAction {
                val action = DataBaseViewPagerFragmentDirections.actionDataBaseViewPagerFragment2ToEditRiderFragment(0)
                findNavController().navigate(action)
            }
            COURSE_PAGE_INDEX -> act.setAction {
                val action = DataBaseViewPagerFragmentDirections.actionDataBaseViewPagerFragmentToEditCourseFragment(0, requireActivity().getString(R.string.new_course))
                findNavController().navigate(action)
            }
            TIMETRIAL_PAGE_INDEX->act.setAction {
                val viewModel = requireActivity().getViewModel { requireActivity().injector.listViewModel() }
                viewModel.timeTrialInsertedEvent.observe(viewLifecycleOwner, EventObserver{
                    val action = DataBaseViewPagerFragmentDirections.actionDataBaseViewPagerFragmentToSelectCourseFragment2(it)
                    findNavController().navigate(action)
                })
                viewModel.insertNewTimeTrial()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //menu.clear()
        inflater.inflate(R.menu.menu_database, menu)
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.app_bar_import -> {
//                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//                intent.type = "text/*"
//                startActivityForResult(intent, REQUEST_IMPORT_FILE)
//
//                Toast.makeText(requireContext(), "Select CSV File", Toast.LENGTH_SHORT).show()
//                true
//            }
//            R.id.app_bar_test->{
//                val action = DataBaseViewPagerFragmentDirections.actionDataBaseViewPagerFragmentToTitleFragment()
//                findNavController().navigate(action)
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        when(requestCode){
//            REQUEST_IMPORT_FILE ->{
//                data?.data?.let {uri->
//                  //  try {
//                    val importVm = requireActivity().getViewModel { requireActivity().injector.importViewModel()}
//                        val inputStream = requireActivity().contentResolver.openInputStream(uri)
//                        if(inputStream != null){
//                            importVm.readInput(uri.path, inputStream)
//                        }
//           //         }
////                    catch(e: IOException)
////                    {
////                        e.printStackTrace()
////                        Toast.makeText(requireActivity(), "Save failed - ${e.message}", Toast.LENGTH_SHORT).show()
////                    }
//                }
//            }
//        }
//    }





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