package com.android.jared.linden.timingtrials.setup


import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.BR
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.data.TimeTrialRider
import java.util.ArrayList


interface ISelectRidersViewModel{
    var allSelectableRiders: LiveData<List<SelectableRiderViewWrapper>>
}

class SelectRidersViewModelImpl(private val ttSetup: SetupViewModel):ISelectRidersViewModel {


    var mRiderViewWrapperList: MediatorLiveData<List<SelectableRiderViewWrapper>> = MediatorLiveData()

    override var allSelectableRiders: LiveData<List<SelectableRiderViewWrapper>> = mRiderViewWrapperList

    init {
        mRiderViewWrapperList.addSource(ttSetup.riderRepository.allRidersLight){ result: List<Rider>? ->
            result?.let{ mRiderViewWrapperList.value = ( result.map {r ->
                SelectableRiderViewWrapper(r).apply {
                    onSelectionChanged = {r,s -> riderSelectionChangeHandler(r,s)}
                }})
            }
        }
        mRiderViewWrapperList.addSource(ttSetup.timeTrial){tt->
            tt?.let {
                val selectedIds = tt.riderList.mapNotNull {r -> r.rider.id }
                mRiderViewWrapperList.value?.forEach {

                    it.changeSelectionStatus(selectedIds.contains(it.rider.id))
                }
            }

        }
    }

    /**
     * Need to remember order of ids were selected when a selection is changed
     *
     */
    private fun riderSelectionChangeHandler(rider: Rider, sel:Boolean){


        ttSetup.timeTrial.value?.let{tt->
            val containsRider = tt.riderList.asSequence().map { it.rider.id }.contains(rider.id)
            if(sel && !containsRider){
                val newList = tt.riderList.map { it.rider } + rider
                ttSetup.updateTimeTrial(tt.helper.addRidersAsTimeTrialRiders(newList))
            }else if(!sel && containsRider){
                val newList = tt.riderList.filter { it.rider.id != rider.id}
                ttSetup.updateTimeTrial(tt.copy(riderList = newList))
            }

        }

    }

}


class SelectableRiderViewWrapper(val rider: Rider): BaseObservable(){

    var getSelected: (Rider) -> Boolean = { _ -> false}
    var onSelectionChanged = { _: Rider, _:Boolean -> Unit}


    val catString = rider.getCategoryStandard().categoryId()
    var mIsSel: Boolean = false

    fun changeSelectionStatus(newCheckStatus: Boolean){
        if(mIsSel != newCheckStatus){
            mIsSel = newCheckStatus
            notifyPropertyChanged(BR.riderIsSelected)
        }

    }

    @Bindable
    fun getRiderIsSelected():Boolean {
        return mIsSel
    }
    fun setRiderIsSelected(value:Boolean) {
        if(mIsSel != value){
            mIsSel = value
            onSelectionChanged(rider, value)
        }
    }
}



