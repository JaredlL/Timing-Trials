package com.jaredlinden.timingtrials.resultexplorer

import androidx.lifecycle.*
import com.jaredlinden.timingtrials.data.IResult
import com.jaredlinden.timingtrials.data.Rider
import com.jaredlinden.timingtrials.data.TimeTrialRiderResult
import com.jaredlinden.timingtrials.data.roomrepo.*
import com.jaredlinden.timingtrials.domain.ColumnData
import com.jaredlinden.timingtrials.domain.GlobalResultData
import com.jaredlinden.timingtrials.domain.ResultDataSource
import com.jaredlinden.timingtrials.spreadsheet.ResultListSpreadSheet
import com.jaredlinden.timingtrials.ui.GenericListItemNext
import com.jaredlinden.timingtrials.util.LengthConverter
import javax.inject.Inject

class GlobalResultViewModel @Inject constructor(private val timeTrialRepository: ITimeTrialRepository, private val riderRepository: IRiderRepository,private val courseRepository: ICourseRepository, private val timeTrialRiderRepository: TimeTrialRiderRepository) : ViewModel() {



    val typeIdLiveData:MutableLiveData<GenericListItemNext>  = MutableLiveData()
    private val dataSource = ResultDataSource(timeTrialRepository, riderRepository, courseRepository, timeTrialRiderRepository)


    val resultSheetMediator: MediatorLiveData<ResultListSpreadSheet> = MediatorLiveData()


    fun getRiderResultList(itemId: Long, itemTypeId: String): LiveData<List<TimeTrialRiderResult>> {
       return if(itemTypeId == Rider::class.java.simpleName){
            timeTrialRiderRepository.getRiderResults(itemId)
        }else{
            timeTrialRiderRepository.getCourseResults(itemId)
        }

    }

    //fun columns(converter: LengthConverter): LiveData<List<ResultFilterViewModel>> = MutableLiveData(ResultFilterViewModel.getAllColumnViewModels(converter))

    val allResults = timeTrialRiderRepository.getAllResults()
    //val columns: MediatorLiveData<List<ResultFilterViewModel>> = MediatorLiveData()
    val columns = Transformations.map(resultSheetMediator){res->
        res?.let {
            val newCols = it.columns.map { ResultFilterViewModel(it) }
            newCols.forEach { resultSheetMediator.addSource(it.mutableColumn){
                updateColumnData(it)
            } }
            return@map newCols
        }
    }

    fun getItemName(itemId: Long, itemTypeId: String) : LiveData<String?>{
        return if(itemTypeId == Rider::class.java.simpleName){
            Transformations.map(riderRepository.getRider(itemId)){it?.fullName()}
        }else{
           Transformations.map(courseRepository.getCourse(itemId)){ it?.courseName}
        }
    }

    fun getResultSheet(conv: LengthConverter): LiveData<ResultListSpreadSheet>{
//        columns.value = ResultFilterViewModel.getAllColumnViewModels(conv)
//        columns.value?.let { cols ->
//            cols.forEach {
//                resultSheetMediator.addSource(it.mutableColumn){
//                    updateResultSheet(allResults.value, columns.value?.mapNotNull { it.mutableColumn.value })
//                }
//            }
//        }
       if(resultSheetMediator.value == null) resultSheetMediator.value  = ResultListSpreadSheet(listOf(), ResultFilterViewModel.getAllColumns(conv), ::newTransform)
        return  resultSheetMediator
    }

    fun updateColumnData(newCol : ColumnData){
        resultSheetMediator.value?.let {
            val newcols = it.columns.map { if(it.key == newCol.key) newCol else it }
            if(newcols != it.columns){
                resultSheetMediator.value = ResultListSpreadSheet(it.results, it.columns.map { if(it.key == newCol.key) newCol else it }, it.onTransform)
            }

        }
    }

    init {
        resultSheetMediator.addSource(allResults){res->
            resultSheetMediator.value?.let {
                val cop = ResultListSpreadSheet(res, it.columns, it.onTransform)
                resultSheetMediator.value = cop
            }
        }
//        resultSheetMediator.addSource(columns){res->
//            updateResultSheet(allResults.value, res.mapNotNull { it.mutableColumn.value  })
//        }

    }

//    private fun updateResultSheet(resultData: List<IResult>?, columnData: List<ColumnData>?){
//        if(resultData != null && columnData != null){
//            resultSheetMediator.value = ResultListSpreadSheet(resultData,columnData, ::newTransform)
//        }
//    }

    fun newTransform(newData: ResultListSpreadSheet){
        //columns.value = newData.columns.map { ResultFilterViewModel(it) }
        resultSheetMediator.value = newData
    }

    fun setTypeIdData(next: GenericListItemNext){
        resMed.value = GlobalResultData("", dataSource.getHeading(next), listOf())
        typeIdLiveData.value = next
    }

    val resMed = MediatorLiveData<GlobalResultData>().apply {

    }

    init {
        resMed.addSource( Transformations.switchMap(typeIdLiveData){dataSource.getResultList(it)}){
           resMed.value = resMed.value?.copy(resultsList =  it)
        }
        resMed.addSource( Transformations.switchMap(typeIdLiveData){dataSource.getResutTitle(it)}){
            resMed.value = resMed.value?.copy(title =  it)
        }
    }
}