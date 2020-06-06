package com.jaredlinden.timingtrials.resultexplorer

import androidx.lifecycle.*
import com.jaredlinden.timingtrials.data.IResult
import com.jaredlinden.timingtrials.data.Rider
import com.jaredlinden.timingtrials.data.TimeTrialRiderResult
import com.jaredlinden.timingtrials.data.roomrepo.*
import com.jaredlinden.timingtrials.domain.*
import com.jaredlinden.timingtrials.spreadsheet.ResultListSpreadSheet
import com.jaredlinden.timingtrials.ui.GenericListItemNext
import com.jaredlinden.timingtrials.util.LengthConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


interface ISheetViewModel{
    val resultSpreadSheet: LiveData<ResultListSpreadSheet>
    fun updateColumn(newColumn: ColumnData)
}

class GlobalResultViewModel @Inject constructor(private val timeTrialRepository: ITimeTrialRepository, private val riderRepository: IRiderRepository,private val courseRepository: ICourseRepository, private val timeTrialRiderRepository: TimeTrialRiderRepository) : ViewModel(), ISheetViewModel {



    val typeIdLiveData:MutableLiveData<GenericListItemNext>  = MutableLiveData()
    private val dataSource = ResultDataSource(timeTrialRepository, riderRepository, courseRepository, timeTrialRiderRepository)


    override val resultSpreadSheet: MediatorLiveData<ResultListSpreadSheet> = MediatorLiveData()


    fun getRiderResultList(itemId: Long, itemTypeId: String): LiveData<List<TimeTrialRiderResult>> {
       return if(itemTypeId == Rider::class.java.simpleName){
            timeTrialRiderRepository.getRiderResults(itemId)
        }else{
            timeTrialRiderRepository.getCourseResults(itemId)
        }

    }


    private val allResults = timeTrialRiderRepository.getAllResults()

    val columns = MutableLiveData<List<ResultFilterViewModel>>()

    fun getItemName(itemId: Long, itemTypeId: String) : LiveData<String?>{
        return if(itemTypeId == Rider::class.java.simpleName){
            Transformations.map(riderRepository.getRider(itemId)){it?.fullName()}
        }else{
           Transformations.map(courseRepository.getCourse(itemId)){ it?.courseName}
        }
    }

    fun setRiderColumnFilter(riderName: String){
        resultSpreadSheet.value?.let { sheet->
           sheet.columns.firstOrNull { it.definition.javaClass == RiderNameColumn::class.java}?.copy(filterText = riderName)?.let {
               newTransform(sheet.results, sheet.updateColumnns(it))
           }
        }
    }

    fun clearAllColumnFilters(){
        resultSpreadSheet.value?.let { sheet->
                newTransform(sheet.results, sheet.columns.map { it.copy(filterText = "", isVisible = true) })
        }
    }

    fun setCourseColumnFilter(courseName: String){
        resultSpreadSheet.value?.let { sheet->
            sheet.columns.firstOrNull { it.definition.javaClass == CourseNameColumn::class.java}?.copy(filterText = courseName)?.let {
                newTransform(sheet.results, sheet.updateColumnns(it))
            }
        }
    }

    fun getResultSheet(conv: LengthConverter): LiveData<ResultListSpreadSheet>{
        if(resultSpreadSheet.value == null) {
            val newSheet = ResultListSpreadSheet(listOf(), ResultFilterViewModel.getAllColumns(conv), ::newTransform)
            resultSpreadSheet.value = newSheet
            columns.value = newSheet.columns.map { ResultFilterViewModel(it, this) }
        }
        return  resultSpreadSheet
    }


    init {
        resultSpreadSheet.addSource(allResults){res->
            resultSpreadSheet.value?.let {
                val cop = ResultListSpreadSheet(res, it.columns, it.onTransform)
                resultSpreadSheet.value = cop
            }
        }
    }

    override fun updateColumn(newColumn: ColumnData) {
        resultSpreadSheet.value?.let {
            val newCols = it.columns.map { columnData -> if(newColumn.key == columnData.key) newColumn else columnData}
                newTransform(it.results, newCols)
        }
    }

    fun newTransform(results: List<IResult>, columns: List<ColumnData>){
        viewModelScope.launch(Dispatchers.Default) {
            val newSs = ResultListSpreadSheet(results, columns, ::newTransform)
            resultSpreadSheet.postValue(newSs)
        }
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