package com.android.jared.linden.timingtrials.timing

import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import android.os.Build
import android.os.Build.VERSION_CODES.N
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.util.ConverterUtils
import com.android.jared.linden.timingtrials.util.ConverterUtils.toSecondsDisplayString
import org.threeten.bp.Instant
import java.util.*
import android.R.string.cancel
import android.app.NotificationManager
import android.R



const val NOTIFICATION_ID = 2

class TimingService : Service(){

    private var timer: Timer = Timer()
    private val TIMER_PERIOD_MS = 25L
    private lateinit var notificationManager: NotificationManager

    override fun onBind(intent: Intent?): IBinder? {

        return binder
    }

    var timerTick: (Long) -> Unit = {}
    var currentTt: TimeTrial? = null

    fun startTiming(){

            timer.cancel()
        System.out.println("JAREDMSG -> Creating Timer")
            timer = Timer()
            val task = object : TimerTask(){
                override fun run() {

                    currentTt?.let {
                        val now = Instant.now()
                        val millisSinceStart = now.toEpochMilli() - it.timeTrialHeader.startTime.toInstant().toEpochMilli()
                        val secs = toSecondsDisplayString(millisSinceStart)
                        if(prevString != secs){
                            updateNotificationTitle(it.timeTrialHeader.ttName, secs)
                            prevString = secs
                        }
                        timerTick.invoke(millisSinceStart)
                    }

                }
            }
            timer.scheduleAtFixedRate(task, 0L, TIMER_PERIOD_MS)
        }



    fun stop(){
        System.out.println("JAREDMSG -> Trying to end service")
        timer.cancel()
        notificationManager.cancel(NOTIFICATION_ID)
        stopForeground(true)
        stopSelf()
        System.out.println("JAREDMSG -> Service Stopped")
    }


    override fun onCreate() {
        setInForeground()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }



    private val binder: IBinder = TimingServiceBinder()



    inner class TimingServiceBinder: Binder(){
        fun getService(): TimingService{
            return this@TimingService
        }
    }

    var prevString = ""
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
        return NotificationCompat.Builder(this, "timing_service").setSmallIcon(com.android.jared.linden.timingtrials.R.drawable.ic_dashboard_black_24dp)
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