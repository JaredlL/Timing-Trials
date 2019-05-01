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
import javax.inject.Inject

class MainActivity : AppCompatActivity() {



    var setupId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupId = null
        val vm = getViewModel { injector.mainViewModel() }
        vm.timeTrial.observe(this, Observer {tt->
            tt?.let {
                setupId = tt.timeTrialDefinition.id
            }

        })

        val tvm = getViewModel { injector.testViewModel() }
        var i = 1
        tvm.medTimeTrial.observe(this, Observer { i++ })

        ma_butt_manageRiders.setOnClickListener{
            val intent = Intent(this@MainActivity, TimingTrialsDbActivity::class.java)
            startActivity(intent)
        }


        ma_butt_begintt.setOnClickListener {
            val intent = Intent(this@MainActivity, SetupActivity::class.java)

                if(setupId != null){

                    val confDialog: UseOldConfirmationFragment = supportFragmentManager
                            .findFragmentByTag("useold") as? UseOldConfirmationFragment ?: UseOldConfirmationFragment()

                    if(confDialog.dialog?.isShowing != true){
                        confDialog.show(supportFragmentManager, "useold")
                    }
                }else{
                    startActivity(intent)
                }




        }

        button2.setOnClickListener {
            val tvm = getViewModel { injector.testViewModel() }

            tvm.medTimeTrial.observe(this, Observer {
                tvm.insertTt{Unit}
            })

        }

        button.setOnClickListener {
            val tvm = getViewModel { injector.testViewModel() }
            tvm.insertTt {
                val intent = Intent(this@MainActivity, TimingActivity::class.java).apply { putExtra(ITEM_ID_EXTRA, tvm.medTimeTrial.value?.timeTrialDefinition?.id)}
                startActivity(intent)
            }

        }




    }
}
