package com.jaredlinden.timingtrials.timing

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.jaredlinden.timingtrials.util.ConverterUtils.toSecondsDisplayString
import org.threeten.bp.Instant
import java.util.*
import android.app.NotificationManager
import android.media.MediaPlayer
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.data.TimeTrial
import com.jaredlinden.timingtrials.data.TimeTrialHeader
import com.jaredlinden.timingtrials.util.Event
import kotlin.math.abs


const val NOTIFICATION_ID = 2

const val CHANNEL_ID = "timing_service"

class TimingService : Service(){

    private var timer: Timer = Timer()
    private var timerTask: TimeTrialTask? = null
    private val TIMER_PERIOD_MS = 33L
    private lateinit var notificationManager: NotificationManager

    override fun onBind(intent: Intent?): IBinder? {

        return binder
    }

    var timerTick: MutableLiveData<Long> = MutableLiveData()



    inner class TimeTrialTask(var timeTrial: TimeTrial) : TimerTask(){
        var prevSecs = 0L
        var soundEvent: Int? = null
        override fun run() {
            val now = Instant.now()
            val millisSinceStart = now.toEpochMilli() - timeTrial.timeTrialHeader.startTimeMilis
            val millis = abs(millisSinceStart)
            val secsLong =  (millis/1000)
            //val secs = toSecondsDisplayString(millisSinceStart)
            if(prevSecs != secsLong){
                updateNotificationTitle(timeTrial.timeTrialHeader.ttName, toSecondsDisplayString(millisSinceStart))
                prevSecs = secsLong
            }
            val sparse = timeTrial.helper.sparseRiderStartTimes
            timerTick.postValue(millisSinceStart)

            val index = sparse.indexOfKey(millisSinceStart)
            val prevIndex = if(index >= 0){ index }else{ Math.abs(index) - 2 }
            val currentSoundEventVal = soundEvent

            val timeSincePrev = if(prevIndex >=0) millisSinceStart - sparse.keyAt(prevIndex) else Long.MAX_VALUE

            if(currentSoundEventVal != null){
                if(currentSoundEventVal != prevIndex && timeSincePrev < 200){
                    soundEvent = prevIndex
                    playSound()
                }
            }else if(prevIndex >= 0 && timeSincePrev < 200){
                soundEvent = prevIndex
                playSound()
            }
        }

        fun updateTimeTrial(newTt: TimeTrial){
            PreferenceManager.getDefaultSharedPreferences(this@TimingService).registerOnSharedPreferenceChangeListener{prefs,key->
                if(key == getString(R.string.p_mainpref_sound)){
                    playSound = prefs.getBoolean(key, true)
                }
            }
            soundEvent = null
            timeTrial = newTt
        }
    }


    var startPlayer: MediaPlayer? = null
    var playSound :Boolean = true
    fun playSound(){

        startPlayer?.let {player->

            if(playSound){
                if (player.isPlaying ) {
                    player.pause()
                    player.seekTo(0);
                }else{
                    player.start()
                }
            }
        }

    }

    fun startTiming(newTimeTrial: TimeTrial){


        if(timerTask == null){
            timerTask?.cancel()
            timer.cancel()
            println("JAREDMSG -> Timing Service -> Creating New Timer")
            timer = Timer()
            timerTask= TimeTrialTask(newTimeTrial)
            timer.scheduleAtFixedRate(timerTask, 0L, TIMER_PERIOD_MS)
        }else{
            timerTask?.updateTimeTrial(newTimeTrial)
        }


    }



    fun stop(){
        System.out.println("JAREDMSG -> Timing Service -> Trying to end service")
        timerTask?.cancel()
        timer.cancel()
        timerTask = null
        notificationManager.cancel(NOTIFICATION_ID)
        stopForeground(true)
        stopSelf()
        System.out.println("JAREDMSG -> Timing Service -> Service Stopped")
    }


    override fun onCreate() {
        playSound = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.p_mainpref_sound), true)
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener{prefs,key->
            if(key == getString(R.string.p_mainpref_sound)){
                playSound = prefs.getBoolean(key, true)
            }
        }
        System.out.println("JAREDMSG -> Timing Service -> Creating Timer")
        setInForeground()
        startPlayer  = MediaPlayer.create(this, R.raw.start)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }



    private val binder: IBinder = TimingServiceBinder()



    inner class TimingServiceBinder: Binder(){
        fun getService(): TimingService{
            return this@TimingService
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timerTask?.cancel()
        timerTask = null
        timer.cancel()
    }


    fun updateNotificationTitle(newTitle:String, newContentText:String){

        val not = getNotification().setContentTitle(newTitle).setContentText(newContentText).build()
        notificationManager.notify(NOTIFICATION_ID, not)
    }


    private fun setInForeground(){

        val channelId =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    createNotificationChannel(CHANNEL_ID, "TimingTrials Service")
                } else {
                    // If earlier version channel ID is not used
                    // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                    ""
                }

       val notification = getNotification().build()
        startForeground(NOTIFICATION_ID, notification)


    }

    private fun getNotification():NotificationCompat.Builder{
        val timingIntent = PendingIntent.getActivity(this, 0,Intent(this, TimingActivity::class.java), 0)
        return NotificationCompat.Builder(this, CHANNEL_ID).setSmallIcon(com.jaredlinden.timingtrials.R.drawable.tt_logo_notification)
                .setTicker(getString(R.string.timing_trials))
                .setContentText(getString(R.string.time_trial_in_progress))
                .setContentIntent(timingIntent)
                .setContentTitle(getString(R.string.time_trial_in_progress))

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }


}