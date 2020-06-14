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
import androidx.lifecycle.MutableLiveData
import com.jaredlinden.timingtrials.data.TimeTrialHeader
import kotlin.math.abs


const val NOTIFICATION_ID = 2

class TimingService : Service(){

    private var timer: Timer = Timer()
    private var timerTask: TimeTrialTask? = null
    private val TIMER_PERIOD_MS = 33L
    private lateinit var notificationManager: NotificationManager

    override fun onBind(intent: Intent?): IBinder? {

        return binder
    }

    var timerTick: MutableLiveData<Long> = MutableLiveData()

    var prevSecs = 0L
    inner class TimeTrialTask(val timeTrial: TimeTrialHeader) : TimerTask(){
        override fun run() {
            val now = Instant.now()
            val millisSinceStart = now.toEpochMilli() - timeTrial.startTimeMilis
            val millis = abs(millisSinceStart)
            val secsLong =  (millis/1000)
            //val secs = toSecondsDisplayString(millisSinceStart)
            if(prevSecs != secsLong){
                updateNotificationTitle(timeTrial.ttName, toSecondsDisplayString(millisSinceStart))
                prevSecs = secsLong
            }
            timerTick.postValue(millisSinceStart)
        }
    }

    fun startTiming(timeTrial: TimeTrialHeader){

        if(timerTask?.timeTrial?.id != timeTrial.id){
            timerTask?.cancel()
            timer.cancel()
            println("JAREDMSG -> Timing Service -> Creating New Timer")
            timer = Timer()
            timerTask= TimeTrialTask(timeTrial)
            timer.scheduleAtFixedRate(timerTask, 0L, TIMER_PERIOD_MS)
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
        System.out.println("JAREDMSG -> Timing Service -> Creating Timer")
        setInForeground()
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
                    createNotificationChannel("timing_service", "TimingTrials Service")
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
        return NotificationCompat.Builder(this, "timing_service").setSmallIcon(com.jaredlinden.timingtrials.R.drawable.ic_timer_black_24dp)
                .setTicker("TimingTrials")
                .setContentText("TimeTrial in progress")
                .setContentIntent(timingIntent)
                .setContentTitle("TimeTrial in progress")

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