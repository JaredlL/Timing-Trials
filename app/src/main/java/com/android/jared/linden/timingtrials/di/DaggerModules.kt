package com.android.jared.linden.timingtrials.di

import android.content.Context
import androidx.room.Room
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
    fun timingTrialsDatabase(context: Context): TimingTrialsDatabase = Room.databaseBuilder(context, TimingTrialsDatabase::class.java,"timingtrials_database")
            .fallbackToDestructiveMigration()
            .addCallback(TimingTrialsDatabase.Companion.TimingTrialsDatabaseCallback(scope))
            .build()

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
class ContextModule(private val appContext: Context) {
    @Provides
    fun appContext(): Context = appContext
}