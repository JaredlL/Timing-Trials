package com.android.jared.linden.timingtrials.di

import androidx.room.Room
import com.android.jared.linden.timingtrials.data.*
import com.android.jared.linden.timingtrials.data.source.*
import com.android.jared.linden.timingtrials.domain.TimeTrialSetup
import com.android.jared.linden.timingtrials.edititem.CourseViewModel
import com.android.jared.linden.timingtrials.edititem.RiderViewModel
import com.android.jared.linden.timingtrials.setup.*
import com.android.jared.linden.timingtrials.viewdata.CourseListViewModel
import com.android.jared.linden.timingtrials.viewdata.RiderListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

val scope = CoroutineScope(Dispatchers.Main + Job())

val timingTrialsAppModule = module {


    single { Room.databaseBuilder(androidApplication(), TimingTrialsDatabase::class.java,"timingtrials_database")
            .fallbackToDestructiveMigration()
            .addCallback(TimingTrialsDatabase.Companion.TimingTrialsDatabaseCallback(scope))
            .build()
    }

    single{ get<TimingTrialsDatabase>().riderDao()  }

    single{ get<TimingTrialsDatabase>().courseDao()  }

    single{ get<TimingTrialsDatabase>().timeTrialDao()  }

    single<IRiderRepository> { RoomRiderRepository(get()) }

    single<ICourseRepository> { RoomCourseRepository(get()) }

    single<ITimeTrialRepository> { RoomTimeTrialRepository(get()) }

    scope("setup") { TimeTrialSetup(get(), get(), get()) }

    viewModel { (riderId: Long) -> RiderViewModel(get(), riderId) }

    viewModel { RiderListViewModel(get()) }

    viewModel { SelectRidersViewModel(get()) }

    viewModel { SetupConfirmationViewModel(get()) }

    viewModel { OrderRidersViewModel(get()) }

    viewModel { SetupTimeTrialViewModel(get()) }

    viewModel { (courseId: Long) -> CourseViewModel(get(), courseId) }

    viewModel { CourseListViewModel(get()) }

    viewModel { SelectCourseViewModel(get()) }
}


