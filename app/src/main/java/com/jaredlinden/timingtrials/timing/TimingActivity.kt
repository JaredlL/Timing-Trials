package com.jaredlinden.timingtrials.timing

import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jaredlinden.timingtrials.BuildConfig
import com.jaredlinden.timingtrials.IFabCallbacks
import com.jaredlinden.timingtrials.MainActivity
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.data.TimeTrial
import com.jaredlinden.timingtrials.data.TimeTrialHeader
import com.jaredlinden.timingtrials.data.TimeTrialStatus
import com.jaredlinden.timingtrials.databinding.ActivityTimingBinding
import com.jaredlinden.timingtrials.setup.SetupViewPagerFragmentArgs
import com.jaredlinden.timingtrials.timetrialresults.ResultFragmentArgs
import com.jaredlinden.timingtrials.util.*
import org.threeten.bp.Instant
import timber.log.Timber

interface ITimingActivity{
    fun showExitDialog()
    fun showExitDialogWithSetup()
    fun endTiming()
}

class TimingActivity : AppCompatActivity(), ITimingActivity, IFabCallbacks {

    private val TIMERTAG = "timing_tag"
    private val STATUSTAG = "status_tag"

    private val mService: MutableLiveData<TimingService?> = MutableLiveData()
    private val viewModel: TimingViewModel by viewModels()
    private var mBound: Boolean = false
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityTimingBinding
    private var timingFab: FloatingActionButton? = null
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as TimingService.TimingServiceBinder
            mService.value = binder.getService()
            serviceCreated.value = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    val serviceCreated: MutableLiveData<Boolean> = MutableLiveData(false)


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

        binding = ActivityTimingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        timingFab = binding.timingFab
        setSupportActionBar(binding.timingToolBar)

        applicationContext.startService(Intent(applicationContext, TimingService::class.java))

        mBound = applicationContext.bindService(Intent(applicationContext, TimingService::class.java), connection, Context.BIND_AUTO_CREATE)


        val navController = findNavController(R.id.nav_host_timer_fragment)

        appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.timingToolBar.setupWithNavController(navController, appBarConfiguration)
        title = getString(R.string.time_trial_in_progress)

        val liveTick = Transformations.switchMap(mService){result->
            result?.timerTick
        }

        viewModel.messageData.observe(this, EventObserver{
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        })

        binding.timingFab.setOnClickListener {
            fabClickEvent.postValue(Event(true))
        }

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
                if(res != null){
                    mService.value?.let {
                        viewModelChange(res, it)
                    }
                }else{
                    endTiming()
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
                    try {
                        Timber.d("Got new timetrial, Stopping ${timeTrialHeader.ttName} ${timeTrialHeader.status}")
                        applicationContext.unbindService(connection)
                        service.stop()
                        mBound = false
                    }catch (e:Exception){
                        Timber.e(e)
                    }




                }

                val args = ResultFragmentArgs(timeTrialHeader.id?:0)
                val pendingIntent = NavDeepLinkBuilder(this)
                        .setGraph(R.navigation.nav_graph)
                        .setDestination(R.id.resultFragment)
                        .setArguments(args.toBundle())
                        .setComponentName(MainActivity::class.java)
                        .createPendingIntent()
                pendingIntent.send()

                finish()
            }
        }
    }


    override fun endTiming() {
        try {
            if(mBound){
                applicationContext.unbindService(connection)
                mBound = false
            }
            mService.value?.stop()
        }catch (e:Exception){
            Timber.e(e)
        }

        finish()
    }

    override fun onStop() {
        try{
            if(mBound){
                applicationContext.unbindService(connection)
                mBound = false
            }
        }catch (e:Exception){
            Timber.e(e)
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

    override fun currentVisibility(): Int {
       return timingFab?.visibility?: View.INVISIBLE
    }

    override fun setFabVisibility(visibility: Int) {
        if(visibility == View.GONE){
            timingFab?.tag = "hide"
        }else{
            timingFab?.tag = "show"
        }
        if(visibility == View.VISIBLE){
            timingFab?.show()
        }
        timingFab?.visibility = visibility
    }

    override fun setFabImage(resourceId: Int) {
        timingFab?.setImageResource(resourceId)
    }

    override val fabClickEvent: MutableLiveData<Event<Boolean>> = MutableLiveData()


}
