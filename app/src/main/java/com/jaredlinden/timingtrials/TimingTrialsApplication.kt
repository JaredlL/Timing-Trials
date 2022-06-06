package com.jaredlinden.timingtrials

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.jaredlinden.timingtrials.di.AppComponent
import com.jaredlinden.timingtrials.di.DaggerAppComponent
import dagger.hilt.android.HiltAndroidApp
import com.jakewharton.threetenabp.AndroidThreeTen
import timber.log.Timber

@HiltAndroidApp
class TimingTrialsApplication: Application() {

    //override val component: AppComponent by lazy { DaggerAppComponent.builder().applicationContext(applicationContext).build() }
    //override val component: AppComponent = DaggerAppComponent.builder().applicationContext(applicationContext).build()

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)


        if(BuildConfig.DEBUG){
            Timber.plant(Timber.DebugTree())
        }

        when(PreferenceManager.getDefaultSharedPreferences(this).getString("dayNight", "System Default")){
            "Light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "Dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "System Default" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            "Follow Battery Saver Feature" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

    }




}

interface DaggerComponentProvider {

    val component: AppComponent
}


