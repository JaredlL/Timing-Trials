package com.jaredlinden.timingtrials.timing

import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.jaredlinden.timingtrials.BuildConfig
import com.jaredlinden.timingtrials.MainActivity
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.data.TimeTrial
import com.jaredlinden.timingtrials.data.TimeTrialHeader
import com.jaredlinden.timingtrials.data.TimeTrialStatus
import com.jaredlinden.timingtrials.setup.SetupViewPagerFragmentArgs
import com.jaredlinden.timingtrials.timetrialresults.ResultFragmentArgs
import com.jaredlinden.timingtrials.util.EventObserver
import com.jaredlinden.timingtrials.util.Utils
import com.jaredlinden.timingtrials.util.getViewModel
import com.jaredlinden.timingtrials.util.injector
import kotlinx.android.synthetic.main.activity_main.*

import kotlinx.android.synthetic.main.activity_timing.*
import org.threeten.bp.Instant
import timber.log.Timber

interface ITimingActivity{
    fun showExitDialog()
    fun showExitDialogWithSetup()
    fun endTiming()
}

class TimingActivity : AppCompatActivity(), ITimingActivity {

    private val TIMERTAG = "timing_tag"
    private val STATUSTAG = "status_tag"

    private val mService: MutableLiveData<TimingService?> = MutableLiveData()
    private lateinit var viewModel: TimingViewModel
    private var mBound: Boolean = false

    private lateinit var appBarConfiguration: AppBarConfiguration

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

   override fun showExitDialog(){
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
                }
                .setNegativeButton("Dismiss"){_,_->

                }
                .create().show()
    }

    val timeMed = MediatorLiveData<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timing)
        setSupportActionBar(timingToolBar)

        //supportActionBar?.setDisplayHomeAsUpEnabled(false)

        viewModel = getViewModel { injector.timingViewModel() }

        applicationContext.startService(Intent(applicationContext, TimingService::class.java))

        mBound = applicationContext.bindService(Intent(applicationContext, TimingService::class.java), connection, Context.BIND_AUTO_CREATE)


        val navController = findNavController(R.id.nav_host_timer_fragment)

        appBarConfiguration = AppBarConfiguration(navController.graph)

        timingToolBar.setupWithNavController(navController, appBarConfiguration)
        timingToolBar.title = getString(R.string.time_trial_in_progress)

//        /**
//         * Check if the fragemts already exist in child fragment manager
//         * To make sure we do not recreate fragments unnecessarily
//         */

//        supportFragmentManager.findFragmentByTag(TIMERTAG)?: TimerFragment.newInstance().also {
//            supportFragmentManager.beginTransaction().apply{
//                add(R.id.higherFrame, it, TIMERTAG)
//                commit()
//            }
//        }
//
//        supportFragmentManager.findFragmentByTag(STATUSTAG)?: RiderStatusFragment.newInstance().also {
//            supportFragmentManager.beginTransaction().apply{
//                add(R.id.lowerFrame, it, STATUSTAG)
//                commit()
//            }
//        }

        val liveTick = Transformations.switchMap(mService){result->
            result?.timerTick
        }

        viewModel.messageData.observe(this, EventObserver{
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        })

        viewModel.ttDeleted.observe(this, EventObserver{
            if(it){
                if(mBound){
                    applicationContext.unbindService(connection)
                    mService.value?.stop()
                    mBound = false

                }
                val pendingIntent = NavDeepLinkBuilder(this)
                        .setGraph(R.navigation.nav_graph)
                        .setDestination(R.id.dataBaseViewPagerFragment)
                        .setComponentName(MainActivity::class.java)
                        .createPendingIntent()
                pendingIntent.send()
                finish()
            }
        })


        timeMed.apply {
            addSource(mService){service->
                   viewModel.timeTrial.value?.let{tt->
                       service?.let {
                           viewModelChange(tt, it)
                       }
                   }
            }
            addSource(viewModel.timeTrial){res->
                res?.let { tt->
                    mService.value?.let {
                        viewModelChange(tt, it)
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

    fun viewModelChange(timeTrial: TimeTrial, service: TimingService){
        val timeTrialHeader = timeTrial.timeTrialHeader
        when(timeTrialHeader.status){
            TimeTrialStatus.SETTING_UP -> {
                if(mBound){
                    Timber.d("Got new timetrial, Stopping ${timeTrialHeader.ttName} ${timeTrialHeader.status}")
                    applicationContext.unbindService(connection)
                    service.stop()
                    mBound = false
                }
                val args = SetupViewPagerFragmentArgs(timeTrialHeader.id?:0)
                val pendingIntent = NavDeepLinkBuilder(this)
                        .setGraph(R.navigation.nav_graph)
                        .setArguments(args.toBundle())
                        .setDestination(R.id.setupViewPagerFragment)
                        .setComponentName(MainActivity::class.java)
                        .createPendingIntent()
                pendingIntent.send()
                finish()
            }
            TimeTrialStatus.IN_PROGRESS -> {
                service.startTiming(timeTrial)
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


    override fun endTiming() {
        if(mBound){
            applicationContext.unbindService(connection)
            mBound = false
        }
        mService.value?.stop()
        finish()
    }

    override fun onStop() {
        if(mBound){
            applicationContext.unbindService(connection)
            mBound = false
        }

        super.onStop()
    }

    override fun showExitDialogWithSetup(){
        AlertDialog.Builder(this)
                .setTitle(getString(R.string.end_timing))
                .setMessage(getString(R.string.are_you_sure_you_want_to_end_tt))
                .setNeutralButton(getString(R.string.end_and_discard_tt)) { _, _ ->
                    if(mBound){
                        applicationContext.unbindService(connection)
                        mService.value?.stop()
                        mBound = false

                    }
                    viewModel.discardTt()
                }
                .setPositiveButton(getString(R.string.end_timing_and_return_to_setup)){ _, _->

                    viewModel.timeTrial.value?.let{
                        if(it.timeTrialHeader.startTime?.toInstant()?:Instant.MAX > Instant.now()){
                            if(mBound){
                                applicationContext.unbindService(connection)
                                mService.value?.stop()
                                mBound = false

                            }
                            viewModel.backToSetup()
                        }else{
                           Toast.makeText(this, getString(R.string.tt_has_started_cannot_go_back), Toast.LENGTH_SHORT).show()
                        }
                    }

                }
                .setNegativeButton(getString(R.string.dismiss)){ _, _->

                }
                .create().show()
    }





}
