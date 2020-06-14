package com.jaredlinden.timingtrials

import android.app.Application
import com.jaredlinden.timingtrials.di.AppComponent
import com.jaredlinden.timingtrials.di.DaggerAppComponent
import com.jakewharton.threetenabp.AndroidThreeTen
import timber.log.Timber


class TimingTrialsApplication: Application(), DaggerComponentProvider {

    override val component: AppComponent by lazy { DaggerAppComponent.builder().applicationContext(applicationContext).build() }
    //override val component: AppComponent = DaggerAppComponent.builder().applicationContext(applicationContext).build()

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)


        if(BuildConfig.DEBUG){
            Timber.plant(Timber.DebugTree())
        }
    }




}

interface DaggerComponentProvider {

    val component: AppComponent
}


