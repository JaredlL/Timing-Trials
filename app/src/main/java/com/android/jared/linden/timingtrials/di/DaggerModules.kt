package com.android.jared.linden.timingtrials.di

import android.content.Context
import androidx.room.Room
import com.android.jared.linden.timingtrials.data.IRiderRepository
import com.android.jared.linden.timingtrials.data.RoomRiderRepository
import com.android.jared.linden.timingtrials.data.source.CourseDao
import com.android.jared.linden.timingtrials.data.source.RiderDao
import com.android.jared.linden.timingtrials.data.source.TimeTrialDao
import com.android.jared.linden.timingtrials.data.source.TimingTrialsDatabase
import dagger.Module
import dagger.Provides
import org.koin.android.ext.koin.androidApplication
import javax.inject.Singleton

@Module
class RoomDatabaseModule{
    @Provides @Singleton
    fun timingTrialsDatabase(context: Context): TimingTrialsDatabase = TimingTrialsDatabase.getDatabase(context, scope)

    @Provides @Singleton
    fun riderDao(db: TimingTrialsDatabase): RiderDao{
        return db.riderDao()
    }

    @Provides @Singleton
    fun courseDao(db: TimingTrialsDatabase): CourseDao{
        return db.courseDao()
    }

    @Provides @Singleton
    fun timeTrialDao(db: TimingTrialsDatabase): TimeTrialDao{
        return db.timeTrialDao()
    }
}

@Module
class RepositoryModule{
    @Provides @Singleton
    fun riderRepository(riderDao: RiderDao): IRiderRepository{
        return RoomRiderRepository(riderDao)
    }
}

@Module
class ContextModule(private val appContext: Context) {
    @Provides
    fun appContext(): Context = appContext

}