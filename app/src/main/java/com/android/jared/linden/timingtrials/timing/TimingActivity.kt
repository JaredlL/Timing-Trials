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

    private lateinit var mService: TimingService
    private var mBound: Boolean = false

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as TimingService.TimingServiceBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    var prevRemoved: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timing)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val viewModel = getViewModel { injector.timingViewModel() }

        viewModel.showMessage = {msg -> Toast.makeText(this, msg, Toast.LENGTH_LONG).show()}

        val serviceIntent = Intent(this, TimingService::class.java).also { intent -> bindService(intent, connection, Context.BIND_AUTO_CREATE) }
        val service = startService(serviceIntent)


        viewModel.timeString.observe(this, Observer {
            val removed = it.subSequence(0, it.length - 2).toString()
            if(prevRemoved!= removed){
                mService.updateNotificationTitle(viewModel.timeTrial.value?.timeTrialHeader?.ttName?:"", removed)
            }
            prevRemoved = removed


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

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

}
