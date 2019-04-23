package com.android.jared.linden.timingtrials

import android.app.Activity
import android.app.Application
import androidx.fragment.app.Fragment
import com.android.jared.linden.timingtrials.di.AppComponent
import com.android.jared.linden.timingtrials.di.DaggerAppComponent
import com.jakewharton.threetenabp.AndroidThreeTen


class TimingTrialsApplication: Application(), DaggerComponentProvider {

    override val component: AppComponent by lazy { DaggerAppComponent.builder().applicationContext(applicationContext).build() }

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
    }


}

interface DaggerComponentProvider {

    val component: AppComponent
}


