package com.jaredlinden.timingtrials.resultexplorer

import android.graphics.Paint
import androidx.lifecycle.*
import com.jaredlinden.timingtrials.data.*
import com.jaredlinden.timingtrials.data.roomrepo.*
import com.jaredlinden.timingtrials.domain.*
import com.jaredlinden.timingtrials.util.Event
import com.jaredlinden.timingtrials.util.LengthConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject


interface ISheetViewModel{
    val columns: MutableLiveData<List<ColumnData>>
    fun updateColumn(newColumn: ColumnData)
}

data class GlobalResultViewModelData(val itemId: Long, val  itemType: String, val converter: LengthConverter)


class ResultExplorerViewModel @Inject constructor(private val timeTrialRepository: ITimeTrialRepository, private val riderRepository: IRiderRepository, private val courseRepository: ICourseRepository, private val timeTrialRiderRepository: TimeTrialRiderRepository) : ViewModel(), ISheetViewModel {



    private var itemId: Long = 0
    private var itemTypeId: String = ""

    private val columnsContext: MutableLiveData<GlobalResultViewModelData> = MutableLiveData()
    private val allResults = timeTrialRiderRepository.getAllResults()


    private val cols = ColumnData.getAllColumns(LengthConverter.default)

    //This drives the spreadsheet data display
    override val columns: MutableLiveData<List<ColumnData>> = MutableLiveData(cols)

    val columnViewModels = cols.map { ResultFilterViewModel(it, this) }

    val resultSpreadSheet: MediatorLiveData<ResultExplorerSpreadSheet> = MediatorLiveData()


    var m_paint: Paint? = null

    fun setColumnsContext(newData: GlobalResultViewModelData, paint: Paint){

        m_paint = paint



        val currentCols = columns.value

        if(currentCols != null){
            val newCols = ColumnData.updateConverter(currentCols,newData.converter)

            if(currentCols != newCols){
                columns.value = newCols
            }


        }else{
            columns.value = ColumnData.getAllColumns(newData.converter)
        }
        if(columnsContext.value != newData){
            columnsContext.value = newData
        }
    }

    val navigateToTTId: MutableLiveData<Event<Long>> = MutableLiveData()

    private fun navigateToTt(ttId: Long){
        navigateToTTId.value = Event(ttId)
    }

    private fun getItemName(itemId: Long, itemTypeId: String) : LiveData<ITimingTrialsEntity?>{
        return if(itemTypeId == Rider::class.java.simpleName){
            Transformations.map(riderRepository.getRider(itemId)){it}
        }else{
            Transformations.map(courseRepository.getCourse(itemId)){ it}
        }
    }

    private val orig = ResultExplorerSpreadSheet(listOf(), cols, ::setNewColumns, ::navigateToTt) { s ->
        m_paint?.measureText(s) ?: s.length * 16F
    }
    init {
        resultSpreadSheet.value = orig

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
                columns.value?.let { cols->
                    resultSpreadSheet.value?.let {
                        newTransform(res, cols, it)
                    }
                }

            }
        }
        resultSpreadSheet.addSource(columns){res->
            res?.let {cols->
                resultSpreadSheet.value?.let {
                    newTransform(it.results, cols, it)
                }

            }
        }

    }


    override fun updateColumn(newColumn: ColumnData) {
        columns.value?.let { currentCols->
            val newCols = currentCols.map { if(it.key == newColumn.key) newColumn else it }
            setNewColumns(newCols)
        }
    }


    fun setRiderColumnFilter(riderName: String){
        columns.value?.let { currentCols->
            val newCols = currentCols.map { if(it.definition.javaClass == RiderNameColumn::class.java) it.copy(filterText = riderName) else it.copy(filterText = "") }
            setNewColumns(newCols)
        }
    }

    fun clearAllColumnFilters(){
        columns.value?.let { currentCols->
            val newCols = currentCols.map { it.copy(filterText = "", isVisible = true, isFocused = false, sortType = SortType.NONE) }
            setNewColumns(newCols)
        }
    }

    fun setCourseColumnFilter(courseName: String){
        columns.value?.let { currentCols->
            val newCols = currentCols.map { if(it.definition.javaClass == CourseNameColumn::class.java) it.copy(filterText = courseName) else it.copy(filterText = "") }
            setNewColumns(newCols)
        }
    }

    private fun updateColsIfNotEqual(newCols: List<ColumnData>){
        val current = columns.value
        if(current == null){
            columns.value = newCols
        }
        else if(current != newCols){
            columns.value = newCols
        }
    }


    fun setNewColumns(columns: List<ColumnData>){
        updateColsIfNotEqual(columns)
    }


    private val queue = ConcurrentLinkedQueue<Triple<List<IResult>, List<ColumnData>, ResultExplorerSpreadSheet>>()
    private var isCarolineAlive = AtomicBoolean()

    fun newTransform(results: List<IResult>, columns: List<ColumnData>, prev: ResultExplorerSpreadSheet){
        if(!isCarolineAlive.get()){
            queue.add(Triple(results, columns, prev))
            viewModelScope.launch(Dispatchers.Default) {
                isCarolineAlive.set(true)
                var mnew: ResultExplorerSpreadSheet? = null
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

}