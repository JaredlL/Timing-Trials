package com.android.jared.linden.timingtrials.viewdata
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.data.ITEM_COURSE
import com.android.jared.linden.timingtrials.data.ITEM_RIDER
import com.android.jared.linden.timingtrials.data.ITEM_TIMETRIAL
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_database.*
import kotlinx.android.synthetic.main.activity_global_result.*


class TimingTrialsDbActivity : AppCompatActivity()  {


    private var mSectionsPagerAdapter: DbActivitySectionsPagerAdapter? = null
    //private lateinit var ridersViewModel: RiderListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_database)


        setSupportActionBar(databaseToolbar)
        supportActionBar?.title = resources.getString(R.string.timingtrials_database)

        mSectionsPagerAdapter = DbActivitySectionsPagerAdapter(supportFragmentManager)


        // Set up the ViewPager with the sections adapter.
        dbcontainer.adapter = mSectionsPagerAdapter
        dbcontainer.offscreenPageLimit = 2

        dbcontainer.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(dbtabs))
        dbtabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(dbcontainer))

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
                0 -> return GenericListFragment.newInstance(ITEM_RIDER)
                1 -> return GenericListFragment.newInstance(ITEM_COURSE)
                2 -> return GenericListFragment.newInstance(ITEM_TIMETRIAL)
                else -> return GenericListFragment.newInstance(ITEM_RIDER)

            }
        }

        override fun getCount(): Int {
            // Show 3 total pages.
            return 3
        }
    }
}