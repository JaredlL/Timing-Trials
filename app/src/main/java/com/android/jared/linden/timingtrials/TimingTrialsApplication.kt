package com.android.jared.linden.timingtrials

import android.app.Application
import com.android.jared.linden.timingtrials.di.timingTrialsAppModule
import org.koin.android.ext.android.startKoin

class TimingTrialsApplication: Application(){
    override fun onCreate() {
        super.onCreate()

        // Start Koin
        startKoin(this, listOf(timingTrialsAppModule) )
    }
}