package com.android.jared.linden.timingtrials.timing

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.OneShotPreDrawListener.add
import androidx.lifecycle.Observer
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.data.TimeTrialStatus
import com.android.jared.linden.timingtrials.setup.SelectCourseFragment
import com.android.jared.linden.timingtrials.setup.SetupActivity
import com.android.jared.linden.timingtrials.setup.SetupTimeTrialFragment
import com.android.jared.linden.timingtrials.util.ITEM_ID_EXTRA
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector

import kotlinx.android.synthetic.main.activity_timing.*
import org.threeten.bp.Instant
import java.lang.Exception

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
            onBound()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    fun onBound(){
        viewModel.timeTrial.observe(this, Observer {tt->

            if(tt == null){
                if(mBound){
                    applicationContext.unbindService(connection)
                    mService?.stop()
                    mBound = false
                }
                finish()
            }else{
                when(tt.timeTrialHeader.status){
                    TimeTrialStatus.SETTING_UP -> {
                        if(mBound){
                            applicationContext.unbindService(connection)
                            mService?.stop()
                            mBound = false
                        }
                        finish()
                    }
                    TimeTrialStatus.IN_PROGRESS -> {
                        mService?.currentTt = tt
                        if(mService == null) throw Exception("SERVICE IS NULL, BUT WHY")
                        mService?.timerTick =::tick
                        mService?.startTiming()
                    }
                    TimeTrialStatus.FINISHED -> {
                        if(mBound){
                            applicationContext.unbindService(connection)
                            mService?.stop()
                            mBound = false
                        }
                        finish()
                    }
                }

            }
        })

    }

    private fun tick(timeStamp: Long){
         viewModel.updateLoop(timeStamp)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timing)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = getViewModel { injector.timingViewModel() }

        viewModel.showMessage = {msg -> Toast.makeText(this, msg, Toast.LENGTH_LONG).show()}

        mBound = applicationContext.bindService(Intent(applicationContext, TimingService::class.java), connection, Context.BIND_AUTO_CREATE)

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
            Toast.makeText(this, "Tap again to end", Toast.LENGTH_SHORT).show()
            prevBackPress = System.currentTimeMillis()
        }else{

            viewModel.timeTrial.value?.let{
                if(it.timeTrialHeader.startTime.toInstant() > Instant.now()){
                    showExitDialogWithSetup()
                }else{
                    showExitDialog()
                }
            }

        }
    }

    fun showExitDialogWithSetup(){
        AlertDialog.Builder(this)
                .setTitle("End Timing")
                .setMessage("Are you sure you want to end TT?")
                .setNeutralButton("End and discard TT") { _, _ ->
                    if(mBound){
                        applicationContext.unbindService(connection)
                        mService?.stop()
                        mBound = false

                    }
                    viewModel.discardTt()
                    finish()
                }
                .setPositiveButton("End Timing and return to Setup"){_,_->

                    viewModel.timeTrial.value?.let{
                        if(it.timeTrialHeader.startTime.toInstant() > Instant.now()){
                            if(mBound){
                                applicationContext.unbindService(connection)
                                mService?.stop()
                                mBound = false

                            }
                            viewModel.backToSetup()
                            val mIntent = Intent(this@TimingActivity, SetupActivity::class.java)
                            startActivity(mIntent)
                        }else{
                           Toast.makeText(this, "TT has now started, cannot go back to setup!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    finish()
                }
                .setNegativeButton("Dismiss"){_,_->

                }
                .create().show()
    }

    fun showExitDialog(){
        AlertDialog.Builder(this)
                .setTitle("End Timing")
                .setMessage("Are you sure you want to end TT? All information will be lost!")
                .setPositiveButton("End timing and discard") { _, _ ->
                    if(mBound){
                        applicationContext.unbindService(connection)
                        mService?.stop()
                        mBound = false

                    }
                    viewModel.discardTt()
                    finish()
                }
                .setNegativeButton("Dismiss"){_,_->

                }
                .create().show()
    }

}
