package com.jaredlinden.timingtrials.di

import android.content.Context
import com.jaredlinden.timingtrials.TitleViewModel
import com.jaredlinden.timingtrials.edititem.EditCourseViewModel
import com.jaredlinden.timingtrials.edititem.EditRiderViewModel
import com.jaredlinden.timingtrials.globalresults.GlobalResultViewModel
import com.jaredlinden.timingtrials.timetrialresults.ResultViewModel
import com.jaredlinden.timingtrials.setup.SetupViewModel
import com.jaredlinden.timingtrials.timing.TimingViewModel
import com.jaredlinden.timingtrials.viewdata.ListViewModel
import com.jaredlinden.timingtrials.TestViewModel
import com.jaredlinden.timingtrials.viewdata.IOViewModel
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

    fun importViewModel(): IOViewModel

}

//@Singleton
//@Subcomponent(modules = [PrefsModule::class])
//interface PrefsComponant{
//
//    @Subcomponent.Builder
//    interface Builder{
//        @BindsInstance
//        fun activityPrefs(): Builder
//        fun build(): PrefsComponant
//    }
//
//}



