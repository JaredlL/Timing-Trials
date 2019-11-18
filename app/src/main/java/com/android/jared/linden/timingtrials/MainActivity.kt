package com.android.jared.linden.timingtrials

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        toolbar.setupWithNavController(navController, appBarConfiguration)
        setSupportActionBar(toolbar)
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
