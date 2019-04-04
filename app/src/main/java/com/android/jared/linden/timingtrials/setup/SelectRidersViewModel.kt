package com.android.jared.linden.timingtrials.setup


import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.BR
import com.android.jared.linden.timingtrials.data.Rider
import java.util.ArrayList


interface ISelectRidersViewModel{
    fun getAllRiders(): LiveData<List<SelectableRiderViewWrapper>>
}

class SelectRidersViewModelImpl(private val ttSetup: TimeTrialSetupViewModel):ISelectRidersViewModel {

    private val mRiderViewWrapperList: MediatorLiveData<List<SelectableRiderViewWrapper>> = MediatorLiveData()
    private fun selectedRiders() = ttSetup.timeTrial.value?.riders
    override fun getAllRiders(): LiveData<List<SelectableRiderViewWrapper>> {
        return  mRiderViewWrapperList
    }

    init {
        mRiderViewWrapperList.addSource(ttSetup.riderRepository.allRiders){ result: List<Rider>? ->
            result?.let{ mRiderViewWrapperList.value = ( result.map {r ->
                SelectableRiderViewWrapper(r).apply {
                    onSelectionChanged = {r,s -> riderSelectionChangeHandler(r,s)}
                    getSelected = {r -> riderIsSelected(r)}
                }})
            }
        }
        mRiderViewWrapperList.addSource(ttSetup.timeTrial){
            mRiderViewWrapperList.value?.forEach {
                it.notifyCheckChanged()
            }
        }
    }

    private fun riderSelectionChangeHandler(rider: Rider, sel:Boolean){
        val oldList = selectedRiders()?.toMutableList()?: ArrayList()
        val newList = ArrayList<Rider>()
        oldList.forEach { r -> if(r.id != rider.id){newList.add(r)}}

        if(sel){newList.add(rider)}

        ttSetup.timeTrial.value = ttSetup.timeTrial.value?.apply {
            riders = newList
        }
    }

    private fun riderIsSelected(rider: Rider): Boolean{
        return selectedRiders()?.map { r -> r.id }?.contains(rider.id) ?: false
    }
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



