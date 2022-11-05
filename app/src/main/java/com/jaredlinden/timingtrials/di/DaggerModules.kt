package com.jaredlinden.timingtrials.di

import android.content.Context
import com.jaredlinden.timingtrials.data.roomrepo.*
import com.jaredlinden.timingtrials.data.source.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class RoomDatabaseModule{

    @Provides
    @Singleton
    fun timingTrialsDatabase(@ApplicationContext context: Context):
            TimingTrialsDatabase = TimingTrialsDatabase.getDatabase(context, CoroutineScope(Dispatchers.Main + Job()))

    @Provides
    @Singleton
    fun riderDao(db: TimingTrialsDatabase): RiderDao{
        return db.riderDao()
    }

    @Provides
    @Singleton
    fun courseDao(db: TimingTrialsDatabase): CourseDao{
        return db.courseDao()
    }

    @Provides
    @Singleton
    fun timeTrialDao(db: TimingTrialsDatabase): TimeTrialDao{
        return db.timeTrialDao()
    }

    @Provides
    @Singleton
    fun timeTrialRiderDao(db: TimingTrialsDatabase): TimeTrialRiderDao{
        return db.timeTrialRiderDao()
    }
}

@InstallIn(SingletonComponent::class)
@Module
abstract class RepositoryModule {

    @Binds
    abstract fun riderRepository(riderDao: RoomRiderRepository): IRiderRepository

    @Binds
    abstract fun courseRepository(courseDao: RoomCourseRepository): ICourseRepository

    @Binds
    abstract fun timeTrialRepository(timeTrialDao: RoomTimeTrialRepository): ITimeTrialRepository

    //@Binds
    //abstract fun timeTrialRiderRepository(timeTrialRiderDao: TimeTrialRiderRepository): TimeTrialRiderRepository

}
