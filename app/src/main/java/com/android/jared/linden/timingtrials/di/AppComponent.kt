package com.android.jared.linden.timingtrials.di

import android.content.Context
import com.android.jared.linden.timingtrials.MainViewModel
import com.android.jared.linden.timingtrials.TestViewModel
import com.android.jared.linden.timingtrials.edititem.CourseViewModel
import com.android.jared.linden.timingtrials.edititem.RiderViewModel
import com.android.jared.linden.timingtrials.setup.SetupViewModel
import com.android.jared.linden.timingtrials.timing.TimingViewModel
import com.android.jared.linden.timingtrials.viewdata.ListViewModel
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [
    RoomDatabaseModule::class,
    RepositoryModule::class])
interface AppComponent{

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun applicationContext(applicationContext: Context): Builder
        fun build(): AppComponent
    }

    fun listViewModel(): ListViewModel

    fun riderViewModel(): RiderViewModel

    fun courseViewModel(): CourseViewModel

    fun timeTrialSetupViewModel(): SetupViewModel

    fun timingViewModel(): TimingViewModel

    fun mainViewModel(): MainViewModel

    fun testViewModel(): TestViewModel

}




