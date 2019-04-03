package com.android.jared.linden.timingtrials

import android.app.Activity
import android.app.Application
import androidx.fragment.app.Fragment
import com.android.jared.linden.timingtrials.di.AppComponent
import com.android.jared.linden.timingtrials.di.DaggerAppComponent


class TimingTrialsApplication: Application(), DaggerComponentProvider {

    override val component: AppComponent by lazy { DaggerAppComponent.builder().applicationContext(applicationContext).build() }

    companion object {
        private var INSTANCE: TimingTrialsApplication? = null
        @JvmStatic
        fun get(): TimingTrialsApplication = INSTANCE!!
    }

}

interface DaggerComponentProvider {

    val component: AppComponent
}

val Activity.injector get() = (TimingTrialsApplication.get()).component
val Fragment.injector get() = (TimingTrialsApplication.get()).component
