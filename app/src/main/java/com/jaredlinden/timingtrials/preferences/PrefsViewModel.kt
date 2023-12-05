package com.jaredlinden.timingtrials.preferences

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaredlinden.timingtrials.data.roomrepo.ICourseRepository
import com.jaredlinden.timingtrials.data.roomrepo.IRiderRepository
import com.jaredlinden.timingtrials.data.roomrepo.ITimeTrialRepository
import com.jaredlinden.timingtrials.data.source.TimingTrialsDatabase
import com.jaredlinden.timingtrials.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrefsViewModel@Inject constructor(val timeTrialRepository: ITimeTrialRepository,
                                        val riderRepository: IRiderRepository,
                                        val courseRepository: ICourseRepository,
                                        val timingTrialsDatabase: TimingTrialsDatabase) : ViewModel(){

    val allDeleted: MutableLiveData<Event<Boolean>> = MutableLiveData()

    fun deleteAll(){
        viewModelScope.launch(Dispatchers.IO) {
            timingTrialsDatabase.timeTrialDao().deleteAllR()
            timingTrialsDatabase.courseDao().deleteAll()
            timingTrialsDatabase.timeTrialDao().deleteAll()
            timingTrialsDatabase.riderDao().deleteAll()
            allDeleted.postValue(Event(true))
        }
    }
}