package com.jaredlinden.timingtrials.resultexplorer

import android.graphics.Paint
import androidx.lifecycle.*
import com.jaredlinden.timingtrials.data.*
import com.jaredlinden.timingtrials.data.roomrepo.*
import com.jaredlinden.timingtrials.domain.*
import com.jaredlinden.timingtrials.spreadsheet.ResultListSpreadSheet
import com.jaredlinden.timingtrials.util.Event
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

data class GlobalResultViewModelData(val itemId: Long, val  itemType: String, val converter: LengthConverter)


//TODO Refactor spreadsheet so its driven by the columns

class GlobalResultViewModel @Inject constructor(private val timeTrialRepository: ITimeTrialRepository, private val riderRepository: IRiderRepository,private val courseRepository: ICourseRepository, private val timeTrialRiderRepository: TimeTrialRiderRepository) : ViewModel(), ISheetViewModel {



    private var itemId: Long = 0
    private var itemTypeId: String = ""

    private val columnsContext: MutableLiveData<GlobalResultViewModelData> = MutableLiveData()
    private val allResults = timeTrialRiderRepository.getAllResults()

    val columnViewModels = MutableLiveData<List<ResultFilterViewModel>>()

    override val resultSpreadSheet: MediatorLiveData<ResultListSpreadSheet> = MediatorLiveData()

    //val rss: LiveData<ResultListSpreadSheet> = Transformations.switchMap(

    var m_paint: Paint? = null

    //var originalColContex:  GlobalResultViewModelData? = null
    fun setColumnsContext(newData: GlobalResultViewModelData, paint: Paint){

        m_paint = paint
        val c =columnViewModels.value
        if(c == null){
            columnViewModels.value = ResultFilterViewModel.getAllColumns((newData.converter)).map { ResultFilterViewModel(it, this) }
        }else{
            //c.forEach { it.mutableColumn.value = it.mutableColumn.value?.copy(isFocused = false)}
        }

        if(columnsContext.value != newData){
            //originalColContex = newData
            resultSpreadSheet.value?.let {
                resultSpreadSheet.value = ResultListSpreadSheet(it.results, columnViewModels.value?.mapNotNull { it.mutableColumn.value }?: listOf() , ::newTransform, ::navigateToTt){ s -> m_paint?.measureText(s)?:s.length.toFloat()}
            }
            columnsContext.value = newData
        }
    }

    val navigateToTTId: MutableLiveData<Event<Long>> = MutableLiveData()

    private fun navigateToTt(ttId: Long){
        navigateToTTId.value = Event(ttId)
    }

    fun getItemName(itemId: Long, itemTypeId: String) : LiveData<ITimingTrialsEntity?>{
        return if(itemTypeId == Rider::class.java.simpleName){
            Transformations.map(riderRepository.getRider(itemId)){it}
        }else{
            Transformations.map(courseRepository.getCourse(itemId)){ it}
        }
    }

    private val orig = ResultListSpreadSheet(listOf(), ResultFilterViewModel.getAllColumns(LengthConverter.default), ::newTransform, ::navigateToTt) {s -> s.length.toFloat()}
    init {
        resultSpreadSheet.value = orig
        columnViewModels.value = orig.columns.map { ResultFilterViewModel(it, this) }

        resultSpreadSheet.addSource(Transformations.switchMap(columnsContext){ it?.let { getItemName(it.itemId, it.itemType)} }){res->

            res?.let {
                when(it){
                    is Rider -> setRiderColumnFilter(it.fullName())
                    is Course -> setCourseColumnFilter(it.courseName)
                }
            }
        }

        resultSpreadSheet.addSource(allResults){res->
            res?.let {
                resultSpreadSheet.value?.let {
                    newTransform(res, it.columns, it)
                }
            }
        }

    }


    override fun updateColumn(newColumn: ColumnData) {
        resultSpreadSheet.value?.let {
            val newCols = columnViewModels.value?.mapNotNull { it.mutableColumn.value }?:it.columns
            newTransform(it.results, newCols, it)
        }
    }


    fun setRiderColumnFilter(riderName: String){
        resultSpreadSheet.value?.let { sheet->
            val newCols = sheet.columns.map { if(it.definition.javaClass == RiderNameColumn::class.java) it.copy(filterText = riderName) else it }
            newTransform(sheet.results, newCols, sheet)
        }
    }

    fun clearAllColumnFilters(){
        resultSpreadSheet.value?.let { sheet->
                newTransform(sheet.results, sheet.columns.map { it.copy(filterText = "", isVisible = true, isFocused = false, sortType = SortType.NONE) }, sheet)
        }
    }

    fun setCourseColumnFilter(courseName: String){
        resultSpreadSheet.value?.let { sheet->
            val newCols = sheet.columns.map { if(it.definition.javaClass == CourseNameColumn::class.java) it.copy(filterText = courseName) else it }
            newTransform(sheet.results, newCols, sheet)
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
                   queue.poll()?.let { qss->
                       val n = mnew
                       mnew = n?.copy(qss.first, qss.second) ?: qss.third.copy(qss.first, qss.second)
                   }

                }
                mnew?.let { resultSpreadSheet.postValue(it) }
                isCarolineAlive.set(false)
            }
        }else{
            queue.add(Triple(results, columns, prev))
        }

    }



//    fun setTypeIdData(next: GenericListItemNext){
//        resMed.value = GlobalResultData("", dataSource.getHeading(next), listOf())
//        typeIdLiveData.value = next
//    }
//
//    val resMed = MediatorLiveData<GlobalResultData>().apply {
//
//    }
//
//    init {
//        resMed.addSource( Transformations.switchMap(typeIdLiveData){dataSource.getResultList(it)}){
//           resMed.value = resMed.value?.copy(resultsList =  it)
//        }
//        resMed.addSource( Transformations.switchMap(typeIdLiveData){dataSource.getResutTitle(it)}){
//            resMed.value = resMed.value?.copy(title =  it)
//        }
//    }
}