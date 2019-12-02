package com.android.jared.linden.timingtrials

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setSupportActionBar(toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)
        //toolbar.setupWithNavController(navController, appBarConfiguration)



//        navController.addOnDestinationChangedListener{ controller, destination, arguments ->
//            title = when (destination.id) {
//            R.id.navigation_home -> "My title"
//            R.id.dataBaseViewPagerFragment -> getString(R.string.timingtrials_database)
//            R.id.editRiderFragment -> getString(R.string.edit_rider)
//            R.id.editCourseFragment -> getString(R.string.edit_course)
//            R.id.setupViewPagerFragment -> getString(R.string.setup_timetrial)
//            R.id.selectCourseFragment -> getString(R.string.select_course)
//            else -> "Default title"
//        }
//        }

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


//    var setupTimeTrial: TimeTrial? = null
//
//
//    private fun setDefaultSetupClickListner(){
//        ma_butt_begintt.text = getString(R.string.start_tt)
//        ma_butt_begintt.setOnClickListener {
//            val tt = setupTimeTrial
//                if(tt != null){
//                    if(tt.timeTrialHeader.ttName != "" && tt.timeTrialHeader.course != null){
//
//                        val confDialog: UseOldConfirmationFragment = supportFragmentManager
//                                .findFragmentByTag("useold") as? UseOldConfirmationFragment ?: UseOldConfirmationFragment()
//
//                        if(confDialog.dialog?.isShowing != true){
//                            confDialog.show(supportFragmentManager, "useold")
//                        }
//                    }else{
//                        val mIntent = Intent(this@MainActivity, SetupActivity::class.java)
//                        startActivity(mIntent)
//
//                    }
//                }else{
//                    val mIntent = Intent(this@MainActivity, SetupActivity::class.java)
//                    startActivity(mIntent)
//                }
//            }
//
//
//        }


//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        val vm = getViewModel { injector.mainViewModel() }
//
//        vm.setupTimeTrial.observe(this, Observer {
//            setupTimeTrial = it
//        })
//
//        vm.timingTimeTrial.observe(this, Observer { tt->
//            if(tt != null) {
//                ma_butt_begintt.setOnClickListener {
//                    val tIntent = Intent(this@MainActivity, TimingActivity::class.java)
//                    startActivity(tIntent)
//                }
//                ma_butt_begintt.text = "Resume ${tt.timeTrialHeader.ttName}"
//            }else{
//                setDefaultSetupClickListner()
//            }
//        })
//
//
//        ma_butt_manageRiders.setOnClickListener{
//            val intent = Intent(this@MainActivity, TimingTrialsDbActivity::class.java)
//            startActivity(intent)
//        }
//
//
//
//
//        createSetupButton.setOnClickListener {
//            val tvm = getViewModel { injector.testViewModel() }
//            tvm.testSetup()
//
//
//        }
//
//        createTimingButton.setOnClickListener {
//            val tvm = getViewModel { injector.testViewModel() }
//            tvm.insertTimingTt()
//            val tIntent = Intent(this@MainActivity, TimingActivity::class.java)
//            startActivity(tIntent)
//
//        }
//
//        testResult2.setOnClickListener {
//            val tvm = getViewModel { injector.testViewModel() }
//            tvm.insertFinishedTt2()
//            tvm.newId.observe(this, Observer {res->
//                res?.let {
//                    val mIntent = Intent(this@MainActivity, ResultActivity::class.java)
//                    mIntent.putExtra(ITEM_ID_EXTRA, it)
//                    startActivity(mIntent)
//                    tvm.newId.removeObservers(this)
//                }
//
//            })
//        }
//
//        createFinishedButton.setOnClickListener {
//            val tvm = getViewModel { injector.testViewModel() }
//            tvm.insertFinishedTt()
//            tvm.newId.observe(this, Observer {res->
//                res?.let {
//                    val mIntent = Intent(this@MainActivity, ResultActivity::class.java)
//                    mIntent.putExtra(ITEM_ID_EXTRA, it)
//                    startActivity(mIntent)
//                    tvm.newId.removeObservers(this)
//                }
//
//            })
//
//        }
//        setDefaultSetupClickListner()
//
//
//
//
//    }
}
