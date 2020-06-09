package com.jaredlinden.timingtrials.resultexplorer

import androidx.lifecycle.*
import com.jaredlinden.timingtrials.data.IResult
import com.jaredlinden.timingtrials.data.Rider
import com.jaredlinden.timingtrials.data.TimeTrial
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
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
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
               newTransform(sheet.results, sheet.updateColumnns(it), sheet)
           }
        }
    }

    fun clearAllColumnFilters(){
        resultSpreadSheet.value?.let { sheet->
                newTransform(sheet.results, sheet.columns.map { it.copy(filterText = "", isVisible = true) }, sheet)
        }
    }

    fun setCourseColumnFilter(courseName: String){
        resultSpreadSheet.value?.let { sheet->
            sheet.columns.firstOrNull { it.definition.javaClass == CourseNameColumn::class.java}?.copy(filterText = courseName)?.let {
                newTransform(sheet.results, sheet.updateColumnns(it), sheet)
            }
        }
    }

    fun getResultSheet(conv: LengthConverter, stringMeasure: (s:String) -> Float): LiveData<ResultListSpreadSheet>{
        val current = resultSpreadSheet.value
        if(current == null) {
            resultSpreadSheet.addSource(allResults) { res ->
                val cols = ResultFilterViewModel.getAllColumns(conv)
                columns.value = cols.map { ResultFilterViewModel(it, this) }
                val sheet = ResultListSpreadSheet(listOf(), listOf(), ::newTransform, stringMeasure)
                newTransform(res, cols, sheet)
            }
            }else{
            val cols = ResultFilterViewModel.getAllColumns(conv)
            columns.value = cols.map { ResultFilterViewModel(it, this) }
            val newSsheet = ResultListSpreadSheet(current.results, cols, ::newTransform, stringMeasure)
            newTransform(newSsheet.results, newSsheet.columns, newSsheet)
            }

        return  resultSpreadSheet
    }



    override fun updateColumn(newColumn: ColumnData) {
        resultSpreadSheet.value?.let {
            val newCols = it.columns.map { columnData -> if(newColumn.key == columnData.key) newColumn else columnData}
                newTransform(it.results, newCols, it)
        }
    }

    private val queue = ConcurrentLinkedQueue<Triple<List<IResult>, List<ColumnData>, ResultListSpreadSheet>>()
    private var isCarolineAlive = AtomicBoolean()

    fun newTransform(results: List<IResult>, columns: List<ColumnData>, prev: ResultListSpreadSheet){
        if(!isCarolineAlive.get()){
            queue.add(Triple(results, columns, prev))
            viewModelScope.launch(Dispatchers.Default) {
                isCarolineAlive.set(true)
                var mnew:ResultListSpreadSheet? = null
                while (queue.peek() != null){
                   queue.poll()?.let {
                       mnew = it.third.copy(it.first, it.second)
                   }
                }
                mnew?.let { resultSpreadSheet.postValue(it) }
                isCarolineAlive.set(false)
            }
        }else{
            queue.add(Triple(results, columns, prev))
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