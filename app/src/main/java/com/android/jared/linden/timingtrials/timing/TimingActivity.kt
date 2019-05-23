package com.android.jared.linden.timingtrials.timing

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.OneShotPreDrawListener.add
import androidx.lifecycle.Observer
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.setup.SelectCourseFragment
import com.android.jared.linden.timingtrials.setup.SetupTimeTrialFragment
import com.android.jared.linden.timingtrials.util.ITEM_ID_EXTRA
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector

import kotlinx.android.synthetic.main.activity_timing.*

class TimingActivity : AppCompatActivity() {

    private val TIMERTAG = "timing_tag"
    private val STATUSTAG = "status_tag"

    private var mService: TimingService? = null
    private lateinit var viewModel: TimingViewModel
    private var mBound: Boolean = false

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as TimingService.TimingServiceBinder
            mService = binder.getService()
           // mBound = true
            Toast.makeText(this@TimingActivity, "Service Connected", Toast.LENGTH_SHORT).show()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
            Toast.makeText(this@TimingActivity, "Service DISCONNECTD", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timing)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = getViewModel { injector.timingViewModel() }

        viewModel.showMessage = {msg -> Toast.makeText(this, msg, Toast.LENGTH_LONG).show()}

        mBound = applicationContext.bindService(Intent(applicationContext, TimingService::class.java), connection, Context.BIND_AUTO_CREATE)
//        val serviceIntent = Intent(applicationContext, TimingService::class.java).also { intent ->
//            mBound = bindService(intent, connection, Context.BIND_AUTO_CREATE)
//           // mBound = true
//        }

        //startService(serviceIntent)



        viewModel.timeTrial.observe(this, Observer {tt->
            tt?.let {
                mService?.timerTick = { ts -> viewModel.updateLoop(ts)}
                mService?.startTiming(tt)
            }

        })



        /**
         * Check if the fragemts already exist in child fragment manager
         * To make sure we do not recreate fragments unnecessarily
         */


        supportFragmentManager.findFragmentByTag(TIMERTAG)?: TimerFragment.newInstance().also {
            supportFragmentManager.beginTransaction().apply{
                add(R.id.higherFrame, it, TIMERTAG)
                commit()
            }
        }

        supportFragmentManager.findFragmentByTag(STATUSTAG)?: RiderStatusFragment.newInstance().also {
            supportFragmentManager.beginTransaction().apply{
                add(R.id.lowerFrame, it, STATUSTAG)
                commit()
            }
        }
    }

    var prevBackPress = 0L
    override fun onBackPressed() {
        if(System.currentTimeMillis() > prevBackPress + 2000){
            Toast.makeText(this, "Tap again to exit", Toast.LENGTH_SHORT).show()
            prevBackPress = System.currentTimeMillis()
        }else{
            if(mBound){

                applicationContext.unbindService(connection)
                mService?.stop()
                mBound = false

            }

            viewModel.discardTt()

            finish()
        }
    }

}
