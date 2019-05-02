package com.android.jared.linden.timingtrials

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.android.jared.linden.timingtrials.setup.*
import com.android.jared.linden.timingtrials.timing.TimingActivity
import com.android.jared.linden.timingtrials.util.ITEM_ID_EXTRA
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector
import com.android.jared.linden.timingtrials.viewdata.TimingTrialsDbActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {



    var setupId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupId = null
        val vm = getViewModel { injector.mainViewModel() }
        vm.timeTrial.observe(this, Observer {tt->
            tt?.let {
                setupId = tt.timeTrialHeader.id
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

        button2.setOnClickListener {
            val tvm = getViewModel { injector.testViewModel() }

            tvm.medTimeTrial.observe(this, Observer {
                tvm.insertTt()
            })

        }

        button.setOnClickListener {
            val tvm = getViewModel { injector.testViewModel() }

            tvm.medTimeTrial.observe(this, Observer {
                it?.let {tt->
                    if(tt.riderList.count() > 0 && tt.timeTrialHeader.course != null){
                        val id = tt.timeTrialHeader.id
                        if(id !=null){
                            val intent = Intent(this@MainActivity, TimingActivity::class.java).apply { putExtra(ITEM_ID_EXTRA, id) }
                            startActivity(intent)
                        }else{
                            tvm.insertTt()
                        }


                    }


                }
            })

            //tvm.insertTt()

        }




    }
}
