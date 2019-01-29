package com.android.jared.linden.timingtrials.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.data.RiderRepository
import com.android.jared.linden.timingtrials.data.TimingTrialsDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class RidersViewModel(application: Application) : AndroidViewModel(application) {

    private var parentJob = Job()

    // By default all the coroutines launched in this scope should be using the Main dispatcher

    private val scope = CoroutineScope(Main + parentJob)

    private val repository: RiderRepository
    // Using LiveData and caching what getAllRiders returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    private val mRiderList: LiveData<List<Rider>>

    var selectedRider: RiderViewModel

    init {
        val riderDao = TimingTrialsDatabase.getDatabase(application, scope).riderDao()
        repository = RiderRepository(riderDao)
        mRiderList = repository.allRiders
        selectedRider = getAllRiders().value?.get(0) ?: RiderViewModel(Rider("","","",0))
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insert(rider: Rider) = scope.launch(Dispatchers.IO) {
        repository.insert(rider)
    }

    fun getAllRiders(): LiveData<List<RiderViewModel>>{
        return Transformations.map(mRiderList){ x -> x.map { r ->
            RiderViewModel(r).also { it.editRider  = editRider  } }
        }}



    var editRider = {(rider):Rider -> Unit}

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }

}