package com.android.jared.linden.timingtrials.setup


import androidx.lifecycle.*
import com.android.jared.linden.timingtrials.data.FilledTimeTrialRider
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.data.TimeTrial
import com.android.jared.linden.timingtrials.data.TimeTrialRider


interface ISelectRidersViewModel{
    //var allSelectableRiders: LiveData<List<SelectableRiderViewWrapper>>
    val selectedRidersInformation: LiveData<SelectedRidersInformation>
    //fun updateSelectedRiders(selectedRiders: List<Rider>)
    fun addRiderToTt(newSelectedRider: Rider)
    fun removeRiderFromTt(riderToRemove: Rider)
}

class SelectRidersViewModelImpl(private val ttSetup: SetupViewModel):ISelectRidersViewModel {


//    var mRiderViewWrapperList: MediatorLiveData<List<SelectableRiderViewWrapper>> = MediatorLiveData()
//
//    override var allSelectableRiders: LiveData<List<SelectableRiderViewWrapper>> = mRiderViewWrapperList
//
//    init {
//        mRiderViewWrapperList.addSource(ttSetup.riderRepository.allRidersLight){ result: List<Rider>? ->
//            result?.let{ mRiderViewWrapperList.value = ( result.map {r ->
//                SelectableRiderViewWrapper(r).apply {
//                    onSelectionChanged = {r,s -> riderSelectionChangeHandler(r,s)}
//                }})
//            }
//        }
//        mRiderViewWrapperList.addSource(ttSetup._mTimeTrial){tt->
//            tt?.let {
//                val selectedIds = tt.riderList.mapNotNull {r -> r.rider.id }
//                mRiderViewWrapperList.value?.forEach {
//
//                    it.changeSelectionStatus(selectedIds.contains(it.rider.id))
//                }
//            }
//
//        }
//    }

    private val selectedRidersMediator: MediatorLiveData<SelectedRidersInformation> = MediatorLiveData()

    override val selectedRidersInformation: LiveData<SelectedRidersInformation> = selectedRidersMediator

//    override fun updateSelectedRiders(selectedRiders: List<Rider>) {
//        ttSetup.timeTrial.value?.let { tt->
//            val updatedTimeTrial = tt.helper.addRidersAsTimeTrialRiders(selectedRiders)
//            ttSetup.updateTimeTrial(updatedTimeTrial)
//        }
//
//    }

    override fun addRiderToTt(newSelectedRider: Rider) {
        ttSetup.timeTrial.value?.let { tt->
            ttSetup.updateTimeTrial(tt.addRider(newSelectedRider))
        }
    }

    override fun removeRiderFromTt(riderToRemove: Rider) {
        ttSetup.timeTrial.value?.let { tt->
            ttSetup.updateTimeTrial(tt.removeRider(riderToRemove))
        }
    }

    init {
//        selectedRidersMediator.addSource(ttSetup.timeTrial){
//            it?.let {
//                selectedRidersMediator.value = SelectedRidersInformation(it.riderList, it)
//            }
//        }
        selectedRidersMediator.addSource(ttSetup.riderRepository.allRidersLight){result->
            val tt = ttSetup.timeTrial.value
            if(result!=null && tt!=null){
                selectedRidersMediator.value = SelectedRidersInformation(result, tt)
            }
        }
        selectedRidersMediator.addSource(ttSetup.timeTrial){ tt->
            val riders = ttSetup.riderRepository.allRidersLight.value
            if(tt!=null && riders!=null){
                selectedRidersMediator.value = SelectedRidersInformation(riders, tt)
            }
        }
    }




    /**
     * Need to remember order of ids were selected when a selection is changed
     *
     */
//    private fun riderSelectionChangeHandler(rider: Rider, sel:Boolean){
//
//
//        ttSetup._mTimeTrial.value?.let{tt->
//            val containsRider = tt.riderList.asSequence().map { it.rider.id }.contains(rider.id)
//            if(sel && !containsRider){
//                val newList = tt.riderList.map { it.rider } + rider
//                ttSetup.testTiming(tt.helper.addRidersAsTimeTrialRiders(newList))
//            }else if(!sel && containsRider){
//                val newList = tt.riderList.filter { it.rider.id != rider.id}
//                ttSetup.testTiming(tt.copy(riderList = newList))
//            }
//
//        }
//
//    }

}

data class SelectedRidersInformation(val allRiderList: List<Rider>, val timeTrial: TimeTrial)


//class SelectableRiderViewWrapper(val rider: Rider): BaseObservable(){
//
//    var getSelected: (Rider) -> Boolean = { _ -> false}
//    var onSelectionChanged = { _: Rider, _:Boolean -> Unit}
//
//
//    val catString = rider.getCategoryStandard().categoryId()
//    var mIsSel: Boolean = false
//
//    fun changeSelectionStatus(newCheckStatus: Boolean){
//        if(mIsSel != newCheckStatus){
//            mIsSel = newCheckStatus
//            notifyPropertyChanged(BR.riderIsSelected)
//        }
//
//    }
//
//    @Bindable
//    fun getRiderIsSelected():Boolean {
//        return mIsSel
//    }
//    fun setRiderIsSelected(value:Boolean) {
//        if(mIsSel != value){
//            mIsSel = value
//            onSelectionChanged(rider, value)
//        }
//    }
//}



