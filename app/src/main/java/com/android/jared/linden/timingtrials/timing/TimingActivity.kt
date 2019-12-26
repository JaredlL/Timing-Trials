package com.android.jared.linden.timingtrials.timing

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.navigation.NavDeepLinkBuilder
import com.android.jared.linden.timingtrials.MainActivity
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.data.TimeTrialHeader
import com.android.jared.linden.timingtrials.data.TimeTrialRider
import com.android.jared.linden.timingtrials.data.TimeTrialStatus
import com.android.jared.linden.timingtrials.timetrialresults.ResultFragmentArgs
import com.android.jared.linden.timingtrials.util.EventObserver
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector
import com.google.android.material.snackbar.Snackbar

import kotlinx.android.synthetic.main.activity_timing.*
import org.threeten.bp.Instant
import timber.log.Timber
import java.lang.Exception

class TimingActivity : AppCompatActivity() {

    private val TIMERTAG = "timing_tag"
    private val STATUSTAG = "status_tag"

    private val mService: MutableLiveData<TimingService?> = MutableLiveData()
    private lateinit var viewModel: TimingViewModel
    private var mBound: Boolean = false

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            System.out.println("JAREDMSG -> Timing Activity -> Service Connectd")
            val binder = service as TimingService.TimingServiceBinder
            mService.value = binder.getService()
            serviceCreated.value = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            System.out.println("JAREDMSG -> Timing Activity -> Service Disconnected")
            mBound = false
        }
    }

    val serviceCreated: MutableLiveData<Boolean> = MutableLiveData(false)

    override fun onDestroy() {
        super.onDestroy()
        System.out.println("JAREDMSG -> Timing Activity -> DESTROY")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timing)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        viewModel = getViewModel { injector.timingViewModel() }

        supportActionBar?.title = "Timetrial in progress"


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

        val liveTick = Transformations.switchMap(mService){result->
            result?.timerTick
        }

        viewModel.messageData.observe(this, EventObserver{
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        })

        val initMediator = MediatorLiveData<Long>().apply {
            addSource(mService){service->
                   viewModel.timeTrial.value?.timeTrialHeader?.let{tt->
                       service?.let {
                           viewModelChange(tt, it)
                       }
                   }
            }
            addSource(viewModel.timeTrial){res->
                res?.let { tt->
                    mService.value?.let {
                        viewModelChange(tt.timeTrialHeader, it)
                    }
                }
            }
            addSource(liveTick){
                value = it
            }
        }.observe(this, Observer {
            viewModel.updateLoop()
        })
    }

    fun viewModelChange(timeTrialHeader: TimeTrialHeader, service: TimingService){
        when(timeTrialHeader.status){
            TimeTrialStatus.SETTING_UP -> {
                if(mBound){
                    Timber.d("Got new timetrial, Stopping ${timeTrialHeader.ttName} ${timeTrialHeader.status}")
                    applicationContext.unbindService(connection)
                    service.stop()
                    mBound = false
                }
                finish()
            }
            TimeTrialStatus.IN_PROGRESS -> {
                service.startTiming(timeTrialHeader)
            }
            TimeTrialStatus.FINISHED -> {
                if(mBound){
                    Timber.d("Got new timetrial, Stopping ${timeTrialHeader.ttName} ${timeTrialHeader.status}")
                    applicationContext.unbindService(connection)
                    service.stop()
                    mBound = false
                    val args = ResultFragmentArgs(timeTrialHeader.id?:0)
                    val pendingIntent = NavDeepLinkBuilder(this)
                            .setGraph(R.navigation.nav_graph)
                            .setDestination(R.id.resultFragment)
                            .setArguments(args.toBundle())
                            .setComponentName(MainActivity::class.java)
                            .createPendingIntent()
                    pendingIntent.send()
                }
                finish()
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
                        mService.value?.stop()
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
                                mService.value?.stop()
                                mBound = false

                            }
                            viewModel.backToSetup()
                            val mIntent = Intent(this@TimingActivity, MainActivity::class.java)
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
                        mService.value?.stop()
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
