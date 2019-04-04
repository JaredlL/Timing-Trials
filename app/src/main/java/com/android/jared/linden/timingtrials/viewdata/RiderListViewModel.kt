package com.android.jared.linden.timingtrials.viewdata

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.data.IRiderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class RiderListViewModel @Inject constructor(private val repository: IRiderRepository) : ViewModel() {

    private var parentJob = Job()

    // By default all the coroutines launched in this scope should be using the Main dispatcher

    //private val scope = CoroutineScope(Main + parentJob)


    // Using LiveData and caching what getAllRiders returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val mRiderList: LiveData<List<Rider>> = Transformations.map(repository.allRiders){r -> r}


    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */

    fun insertOrUpdate(rider: Rider) = viewModelScope.launch(Dispatchers.IO) {
        if (rider.firstName != "") {
            repository.insertOrUpdate(rider)
        }
    }

    var selectable: Boolean = false


    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }

}

