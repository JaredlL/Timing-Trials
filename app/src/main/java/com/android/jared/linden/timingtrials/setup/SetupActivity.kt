package com.android.jared.linden.timingtrials.setup

import com.google.android.material.tabs.TabLayout
import androidx.appcompat.app.AppCompatActivity

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import android.os.Bundle
import android.view.*
import android.widget.Toast
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector
import com.android.jared.linden.timingtrials.util.ITEM_ID_EXTRA

import kotlinx.android.synthetic.main.activity_setup.*
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime

class SetupActivity : AppCompatActivity() {

    /**
     * The [androidx.viewpager.widget.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * androidx.fragment.app.FragmentStatePagerAdapter.
     */


    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    //private lateinit var riderListViewModel: RiderListViewModel
    private lateinit var setupViewModel: SetupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        setupViewModel = getViewModel { injector.timeTrialSetupViewModel() }


        intent.getLongExtra(ITEM_ID_EXTRA, 0L).let {
            setupViewModel.initialise(it)
        }



        setupViewModel.timeTrialPropertiesViewModel.onBeginTt = {

            setupViewModel.timeTrial.value?.let {
                if(it.riderList.count() == 0){
                    Toast.makeText(this, "TT Needs at least 1 rider", Toast.LENGTH_LONG).show()
                    container.currentItem = 1
                    return@let
                }
                if(it.timeTrialHeader.startTime.isBefore(OffsetDateTime.now())){
                    Toast.makeText(this, "TT must start in the future, select start time", Toast.LENGTH_LONG).show()
                    TimePickerFragment().show(supportFragmentManager, "timePicker")
                    return@let
                }
                val confDialog: SetupConfirmationFragment = supportFragmentManager
                        .findFragmentByTag("confdialog") as? SetupConfirmationFragment ?: SetupConfirmationFragment()

                if(confDialog.dialog?.isShowing != true){
                    confDialog.show(supportFragmentManager, "confdialog")
                }

            }

        }

        setSupportActionBar(toolbar)
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)


        // Set up the ViewPager with the sections adapter.
        container.adapter = mSectionsPagerAdapter
        container.offscreenPageLimit = 2

        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))


    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_setup, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }


    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).


           return when (position){
                0 ->  SetupTimeTrialFragment.newInstance()
                1 ->  SelectRidersFragment.newInstance()
                2 ->  OrderRidersFragment.newInstance()
                else ->  SetupTimeTrialFragment.newInstance()

            }
        }

        override fun getCount(): Int {
            // Show 3 total pages.
            return 3
        }
    }


    class DuelHostFragment : Fragment() {

        private val TTTAG = "tt_tag"
        private val COURSETAG = "course_tag"

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.fragment_host, container, false)

            /**
             * Check if the fragemts already exist in child fragment manager
             * To make sure we do not recreate fragments unnecessarily
             */

            childFragmentManager.findFragmentByTag(TTTAG)?: SetupTimeTrialFragment.newInstance().also {
                childFragmentManager.beginTransaction().apply{
                    add(R.id.higherFrame, it, TTTAG)
                    commit()
                }
            }

            childFragmentManager.findFragmentByTag(COURSETAG)?: SelectCourseFragment.newInstance().also {
                childFragmentManager.beginTransaction().apply{
                    add(R.id.lowerFrame, it, COURSETAG)
                    commit()
                }
            }


            return rootView
        }

        companion object {


            fun newInstance(): DuelHostFragment {
                return DuelHostFragment()
            }
        }
    }
}
