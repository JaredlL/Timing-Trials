package com.android.jared.linden.timingtrials.di

import android.content.Context
import com.android.jared.linden.timingtrials.TitleViewModel
import com.android.jared.linden.timingtrials.edititem.EditCourseViewModel
import com.android.jared.linden.timingtrials.edititem.EditRiderViewModel
import com.android.jared.linden.timingtrials.globalresults.GlobalResultViewModel
import com.android.jared.linden.timingtrials.timetrialresults.ResultViewModel
import com.android.jared.linden.timingtrials.setup.SetupViewModel
import com.android.jared.linden.timingtrials.timing.TimingViewModel
import com.android.jared.linden.timingtrials.viewdata.ListViewModel
import com.android.jared.linden.timingtrials.TestViewModel
import com.android.jared.linden.timingtrials.transfer.ImportViewModel
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

    fun riderViewModel(): EditRiderViewModel

    fun courseViewModel(): EditCourseViewModel

    fun timeTrialSetupViewModel(): SetupViewModel

    fun timingViewModel(): TimingViewModel

    fun mainViewModel(): TitleViewModel

    fun testViewModel(): TestViewModel

    fun resultViewModel(): ResultViewModel

    fun globalResultViewModel(): GlobalResultViewModel

    fun importViewModel(): ImportViewModel

}




