package com.android.jared.linden.timingtrials.viewdata

import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.data.IRiderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

//class RiderListViewModel @Inject constructor(private val repository: IRiderRepository) : ViewModel() {
//
//
//    // Using LiveData and caching what getAllRiders returns has several benefits:
//    // - We can put an observer on the data (instead of polling for changes) and only update the
//    //   the UI when the data actually changes.
//    // - Repository is completely separated from the UI through the ViewModel.
//    val mRiderList: LiveData<List<Rider>> = repository.allRiders
//
//}

