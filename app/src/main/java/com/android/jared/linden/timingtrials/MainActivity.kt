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


    fun setDefaultSetupClickListner(){
        ma_butt_begintt.text = getString(R.string.start_tt)
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
    }

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
            if(tt != null) {
                ma_butt_begintt.setOnClickListener {
                    val tIntent = Intent(this@MainActivity, TimingActivity::class.java)
                    startActivity(tIntent)
                }
                ma_butt_begintt.text = "Resume ${tt.timeTrialHeader.ttName}"
            }else{
                setDefaultSetupClickListner()
            }
        })


        ma_butt_manageRiders.setOnClickListener{
            val intent = Intent(this@MainActivity, TimingTrialsDbActivity::class.java)
            startActivity(intent)
        }




        createSetupButton.setOnClickListener {
            val tvm = getViewModel { injector.testViewModel() }
            tvm.insertSetupTt()


        }

        createTimingButton.setOnClickListener {
            val tvm = getViewModel { injector.testViewModel() }
            tvm.insertTimingTt()
            val tIntent = Intent(this@MainActivity, TimingActivity::class.java)
            startActivity(tIntent)

        }

        createFinishedButton.setOnClickListener {
            val tvm = getViewModel { injector.testViewModel() }
            tvm.insertFinishedTt()
            tvm.newId.observe(this, Observer {res->
                res?.let {
                    val mIntent = Intent(this@MainActivity, ResultActivity::class.java)
                    mIntent.putExtra(ITEM_ID_EXTRA, it)
                    startActivity(mIntent)
                    tvm.newId.removeObservers(this)
                }

            })

        }
        setDefaultSetupClickListner()




    }
}
