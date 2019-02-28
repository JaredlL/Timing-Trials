package com.android.jared.linden.timingtrials.util

import android.content.Context
import com.android.jared.linden.timingtrials.data.RiderRepository
import com.android.jared.linden.timingtrials.data.TimingTrialsDatabase
import com.android.jared.linden.timingtrials.viewmodels.RiderListViewModelFactory
import com.android.jared.linden.timingtrials.viewmodels.RiderViewModel
import com.android.jared.linden.timingtrials.viewmodels.RiderViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job


/**
 * Static methods used to inject classes needed for various Activities and Fragments.
 */

object InjectorUtils {

    private var parentJob = Job()

    // By default all the coroutines launched in this scope should be using the Main dispatcher

    private val scope = CoroutineScope(Dispatchers.Main + parentJob)

    private fun getRiderRepository(context: Context): RiderRepository{
        return RiderRepository.getInstance(TimingTrialsDatabase.getDatabase(context, scope).riderDao())
    }

    fun provideRiderListViewModelFactory(context: Context): RiderListViewModelFactory {
        val repository = getRiderRepository(context)
        return RiderListViewModelFactory(repository)
    }

    fun provideRiderViewModelFactory(context: Context, riderId: Long): RiderViewModelFactory{
        val repository = getRiderRepository(context)
        return RiderViewModelFactory(repository, riderId)
    }

}