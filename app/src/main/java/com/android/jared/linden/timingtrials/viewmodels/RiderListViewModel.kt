package com.android.jared.linden.timingtrials.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.data.RiderRepository
import com.android.jared.linden.timingtrials.data.TimingTrialsDatabase
import com.android.jared.linden.timingtrials.util.InjectorUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class RiderListViewModelFactory(private val repository: RiderRepository): ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>) = RiderListViewModel(repository) as T
    }


class RiderListViewModel(private val repository: RiderRepository) : ViewModel() {

    private var parentJob = Job()

    // By default all the coroutines launched in this scope should be using the Main dispatcher

    private val scope = CoroutineScope(Main + parentJob)


    // Using LiveData and caching what getAllRiders returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    private val mRiderList: LiveData<List<Rider>> = repository.allRiders

    val selectedIds: ArrayList<Long> = ArrayList()


    init {
        //val riderDao = TimingTrialsDatabase.getDatabase(application, scope).riderDao()
       // mRiderList = repository.allRiders
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */

    fun insertOrUpdate(rider: Rider) = scope.launch(Dispatchers.IO) {
        if(rider.firstName != ""){
            repository.insertOrUpdate(rider)
        }

    }

    fun deleteRider(rider: Rider) = scope.launch(Dispatchers.IO) {
        repository.delete(rider)
    }


//    fun getAllRiders(): LiveData<List<RiderViewModel>>{
//        return Transformations.map(mRiderList){ x -> x.map { r ->
//            RiderViewModel(r).also { it.editRider  = editRider  } }
//        }}

    fun getAllRiders(): LiveData<List<Rider>>{
        return mRiderList
    }

    var mSelectable = false

    fun setSelectable(value: Boolean){
        mSelectable = value
    }

    fun  getSelectable(): Boolean{
        return  mSelectable
    }

    var editRider = {(rider):Rider -> Unit}

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }

}