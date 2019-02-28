package com.android.jared.linden.timingtrials.viewmodels

import android.view.View
import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.data.RiderRepository
import com.android.jared.linden.timingtrials.util.ObservableViewModel
import kotlinx.coroutines.*

class RiderViewModelFactory(private val riderRepository: RiderRepository,
                            private val riderId: Long
) : ViewModelProvider.NewInstanceFactory(){
    //@Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return RiderViewModel(riderRepository, riderId) as T
    }
}

class RiderViewModel(val riderRepository: RiderRepository, riderId: Long): ViewModel() {

    private var parentJob = Job()


    val rider: LiveData<Rider> = riderRepository.getRider(riderId)
    val mutableRider: MediatorLiveData<Rider> = MediatorLiveData()



    init{
        mutableRider.addSource(rider){result: Rider -> result.let { mutableRider.value = result }}
    }

    fun addOrUpdate(){
        viewModelScope.launch(Dispatchers.IO) {
            mutableRider.value?.let { riderRepository.insertOrUpdate(it) }
        }
    }

    @ExperimentalCoroutinesApi
    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }




}