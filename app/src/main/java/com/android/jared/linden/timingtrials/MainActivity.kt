package com.android.jared.linden.timingtrials

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.android.jared.linden.timingtrials.result.ResultActivity
import com.android.jared.linden.timingtrials.setup.*
import com.android.jared.linden.timingtrials.timing.TimingActivity
import com.android.jared.linden.timingtrials.util.ITEM_ID_EXTRA
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector
import com.android.jared.linden.timingtrials.viewdata.TimingTrialsDbActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {



    var setupId: Long? = null
    var inProgressId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupId = null
        val vm = getViewModel { injector.mainViewModel() }
        vm.setupTimeTrial.observe(this, Observer { tt->
            tt?.let {
                setupId = tt.timeTrialHeader.id
            }

        })

        vm.timingTimeTrial.observe(this, Observer { tt->
            tt?.let {
                val tIntent = Intent(this@MainActivity, TimingActivity::class.java)
                startActivity(tIntent)
                vm.timingTimeTrial.removeObservers(this)
            }
        })


        ma_butt_manageRiders.setOnClickListener{
            val intent = Intent(this@MainActivity, TimingTrialsDbActivity::class.java)
            startActivity(intent)
        }


        ma_butt_begintt.setOnClickListener {


                if(setupId != null){

                    val confDialog: UseOldConfirmationFragment = supportFragmentManager
                            .findFragmentByTag("useold") as? UseOldConfirmationFragment ?: UseOldConfirmationFragment()

                    if(confDialog.dialog?.isShowing != true){
                        confDialog.show(supportFragmentManager, "useold")
                    }
                }else{
                    val mIntent = Intent(this@MainActivity, SetupActivity::class.java)
                    startActivity(mIntent)
                }




        }

        createSetupButton.setOnClickListener {
            val tvm = getViewModel { injector.testViewModel() }

            tvm.medTimeTrial.observe(this, Observer {
                it?.let {tt->
                    if(tt.riderList.count() > 0 && tt.timeTrialHeader.course != null){
                        tvm.insertSetupTt()
                        tvm.medTimeTrial.removeObservers(this)
                    }
                }
            })

        }

        createTimingButton.setOnClickListener {
            val tvm = getViewModel { injector.testViewModel() }

            tvm.medTimeTrial.observe(this, Observer {
                it?.let {tt->
                    if(tt.riderList.isNotEmpty() && tt.timeTrialHeader.course != null){
                        val id = tt.timeTrialHeader.id
                            tvm.insertTimingTt()
                        tvm.medTimeTrial.removeObservers(this)
                    }
                }
            })

            //tvm.insertTimingTt()

        }

        createFinishedButton.setOnClickListener {
            val tvm = getViewModel { injector.testViewModel() }

            tvm.medTimeTrial.observe(this, Observer {
                it?.let {tt->
                    if(tt.riderList.isNotEmpty() && tt.timeTrialHeader.course != null){
                        val id = tt.timeTrialHeader.id
                        tvm.insertFinishedTt()
                        val tIntent = Intent(this@MainActivity, ResultActivity::class.java).apply { putExtra(ITEM_ID_EXTRA, id) }
                        startActivity(tIntent)
                        tvm.medTimeTrial.removeObservers(this)
                    }
                }
            })

            //tvm.insertTimingTt()

        }




    }
}
