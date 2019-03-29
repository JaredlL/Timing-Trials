package com.android.jared.linden.timingtrials.setup


import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.BR
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.domain.TimeTrialSetup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SelectRidersViewModel(private val timeTrialSetup: TimeTrialSetup) : ViewModel(){

    private var parentJob = Job()


    private val mRiderViewWrapperList: MediatorLiveData<List<SelectableRiderViewWrapper>> = MediatorLiveData()

    init {
        mRiderViewWrapperList.addSource(timeTrialSetup.allRiders){ result: List<Rider>? ->
          result?.let{ mRiderViewWrapperList.postValue( result.map {r ->
              SelectableRiderViewWrapper(r).apply {
                  onSelectionChanged = {r,s -> timeTrialSetup.riderSelectionChangeHandler(r,s)}
                  getSelected = {r -> timeTrialSetup.riderIsSelected(r)}
              }})
          }
        }
        mRiderViewWrapperList.addSource(timeTrialSetup.selectedOrderedRiders){
            mRiderViewWrapperList.value?.forEach {
                it.notifyCheckChanged()
            }
        }
    }


    fun getAllRiders(): LiveData<List<SelectableRiderViewWrapper>> {
        return mRiderViewWrapperList
    }


    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }

    class SelectableRiderViewWrapper(val rider: Rider): BaseObservable(){

        var getSelected: (Rider) -> Boolean = { _ -> false}
        var onSelectionChanged = { _: Rider, _:Boolean -> Unit}

        fun notifyCheckChanged(){
            notifyPropertyChanged(BR.riderIsSelected)
        }

        @Bindable
        fun getRiderIsSelected():Boolean {
            return getSelected(rider)
        }
        fun setRiderIsSelected(value:Boolean) {
            onSelectionChanged(rider, value)
            //notifyPropertyChanged(BR.riderIsSelected)
        }
    }

}



