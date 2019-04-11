package com.android.jared.linden.timingtrials.viewdata
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.android.jared.linden.timingtrials.R
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_database.*
import kotlinx.android.synthetic.main.activity_setup.*


const val RIDER_EXTRA = "rider_extra"
const val ITEM_TYPE_EXTRA = "item_type"
const val ITEM_ID_EXTRA = "item_id"
const val ITEM_RIDER = "item_rider"
const val ITEM_COURSE = "item_course"

class TimingTrialsDbActivity : AppCompatActivity()  {


    private var mSectionsPagerAdapter: DbActivitySectionsPagerAdapter? = null
    //private lateinit var ridersViewModel: RiderListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_database)


        mSectionsPagerAdapter = DbActivitySectionsPagerAdapter(supportFragmentManager)


        // Set up the ViewPager with the sections adapter.
        dbcontainer.adapter = mSectionsPagerAdapter
        dbcontainer.offscreenPageLimit = 2

        dbcontainer.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(dbtabs))
        dbtabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))

    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class DbActivitySectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).


            when (position){
                0 -> return RiderListFragment.newInstance()
                1 -> return GenericListFragment.newInstance()
                2 -> return RiderListFragment.newInstance()
                else -> return RiderListFragment.newInstance()

            }
        }

        override fun getCount(): Int {
            // Show 3 total pages.
            return 3
        }
    }
}