package com.android.jared.linden.timingtrials

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.android.jared.linden.timingtrials.setup.*
import com.android.jared.linden.timingtrials.timing.TimingActivity
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
            setupId = tt?.id
        })

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
            val vm = getViewModel { injector.testViewModel() }

            vm.medTimeTrial.observe(this, Observer {
                vm.insertTt()
            })

        }

        button.setOnClickListener {
            val vm = getViewModel { injector.testViewModel() }
            vm.medTimeTrial.observe(this, Observer {
                vm.insertTt()
                intent.putExtra(TIMETRIAL_ID_EXTRA, it.id)
                val intent = Intent(this, TimingActivity::class.java)
                startActivity(intent)
            })

        }




    }
}
