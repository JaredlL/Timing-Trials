package com.android.jared.linden.timingtrials.di

import android.content.Context
import com.android.jared.linden.timingtrials.MainActivity
import com.android.jared.linden.timingtrials.TimingTrialsApplication
import com.android.jared.linden.timingtrials.data.source.CourseDao
import com.android.jared.linden.timingtrials.data.source.RiderDao
import com.android.jared.linden.timingtrials.data.source.TimeTrialDao
import com.android.jared.linden.timingtrials.data.source.TimingTrialsDatabase
import com.android.jared.linden.timingtrials.edititem.CourseViewModel
import com.android.jared.linden.timingtrials.edititem.RiderViewModel
import com.android.jared.linden.timingtrials.setup.TimeTrialSetupViewModel
import com.android.jared.linden.timingtrials.viewdata.CourseListViewModel
import com.android.jared.linden.timingtrials.viewdata.RiderListViewModel
import dagger.BindsInstance
import dagger.Component
import dagger.Subcomponent
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

    fun courseListViewModel(): CourseListViewModel

    fun riderListViewModel(): RiderListViewModel

    fun riderViewModel(): RiderViewModel

    fun courseViewModel(): CourseViewModel

    fun timeTrialSetupViewModel(): TimeTrialSetupViewModel

}




