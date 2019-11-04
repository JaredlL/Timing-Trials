package com.android.jared.linden.timingtrials.viewdata
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.data.ITEM_COURSE
import com.android.jared.linden.timingtrials.data.ITEM_RIDER
import com.android.jared.linden.timingtrials.data.ITEM_TIMETRIAL
import kotlinx.android.synthetic.main.activity_database.*


class TimingTrialsDbActivity : AppCompatActivity()  {


    //private lateinit var ridersViewModel: RiderListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_database)

        val navController = findNavController(R.id.nav_host_fragment_database)
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        databaseToolbar.setupWithNavController(navController, appBarConfiguration)
        setSupportActionBar(databaseToolbar)
        supportActionBar?.title = resources.getString(R.string.timingtrials_database)

//
//        mSectionsPagerAdapter = DbActivitySectionsPagerAdapter(supportFragmentManager)
//
//
//        // Set up the ViewPager with the sections adapter.
//        dbcontainer.adapter = mSectionsPagerAdapter
//        dbcontainer.offscreenPageLimit = 2
//
//        dbcontainer.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(dbtabs))
//        dbtabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(dbcontainer))

    }



    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
}