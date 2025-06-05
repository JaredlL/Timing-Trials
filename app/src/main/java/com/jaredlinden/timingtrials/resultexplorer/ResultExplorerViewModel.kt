package com.jaredlinden.timingtrials.resultexplorer

import android.graphics.Paint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.jaredlinden.timingtrials.data.Course
import com.jaredlinden.timingtrials.data.ITimingTrialsEntity
import com.jaredlinden.timingtrials.data.Rider
import com.jaredlinden.timingtrials.data.roomrepo.ICourseRepository
import com.jaredlinden.timingtrials.data.roomrepo.IRiderRepository
import com.jaredlinden.timingtrials.data.roomrepo.TimeTrialRiderRepository
import com.jaredlinden.timingtrials.domain.ColumnData
import com.jaredlinden.timingtrials.domain.CourseNameColumn
import com.jaredlinden.timingtrials.domain.RiderNameColumn
import com.jaredlinden.timingtrials.util.Event
import com.jaredlinden.timingtrials.util.LengthConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

interface ISheetViewModel{
    val columns: MutableLiveData<List<ColumnData>>
    fun updateColumn(newColumn: ColumnData)
}

data class GlobalResultViewModelData(val itemId: Long, val  itemType: String, val converter: LengthConverter)

@HiltViewModel
class ResultExplorerViewModel @Inject constructor(
    private val riderRepository: IRiderRepository,
    private val courseRepository: ICourseRepository,
    timeTrialRiderRepository: TimeTrialRiderRepository) : ViewModel(), ISheetViewModel {

    private val columnsContext: MutableLiveData<GlobalResultViewModelData> = MutableLiveData()
    private val allResults = timeTrialRiderRepository.getAllResults()

    var hasShownSnackBar = false

    private val cols = ColumnData.getAllColumns(LengthConverter.default)

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
            riderRepository.getRider(itemId).map{it}
        }else{
            courseRepository.getCourse(itemId).map{ it}
        }
    }

    private val orig = ResultExplorerSpreadSheet(listOf(), cols, ::setNewColumns, ::navigateToTt) { s ->
        m_paint?.measureText(s) ?: (s.length * 16F)
    }
    init {
        resultSpreadSheet.value = orig

        resultSpreadSheet.addSource(columnsContext.switchMap{ it?.let { getItemName(it.itemId, it.itemType)} }){res->

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
                        resultSpreadSheet.value = it.copy(res)
                    }
            }
        }
        resultSpreadSheet.addSource(columns){res->
            res?.let {cols->
                resultSpreadSheet.value?.let {
                    resultSpreadSheet.value = it.copy(columns = cols)
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
            val conv = columnsContext.value?.converter?: LengthConverter.default
            setNewColumns(ColumnData.getAllColumns(conv))
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
}