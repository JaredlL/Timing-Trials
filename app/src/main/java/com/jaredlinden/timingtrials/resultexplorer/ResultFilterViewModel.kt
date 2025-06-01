package com.jaredlinden.timingtrials.resultexplorer

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.jaredlinden.timingtrials.domain.*
import com.jaredlinden.timingtrials.util.changeValIfNotEqual
import com.jaredlinden.timingtrials.util.setIfNotEqual

class ResultFilterViewModel(column: ColumnData, val sheetVm:ISheetViewModel) {

    private val columnKey = column.key

    private fun currentCol(): ColumnData?{
       return sheetVm.columns.value?.firstOrNull{it.key == columnKey}
    }

    val mutableColumn = MediatorLiveData<ColumnData>().apply { value = currentCol() }

    val imageRes = mutableColumn.map{
        it?.imageRes
    }

    val isVisible = MutableLiveData(true)
    val filterText = MutableLiveData("")
    val sortIndex= MutableLiveData(0)
    val description = mutableColumn.map{
        it?.description
    }

    init {
        mutableColumn.addSource(sheetVm.columns.map{ it.firstOrNull{it.key == columnKey}}){res->
            res?.let { col->
                isVisible.setIfNotEqual(col.isVisible)
                filterText.setIfNotEqual(col.filterText)
                sortIndex.setIfNotEqual(col.sortOrder)
                mutableColumn.value = col
            }
        }

        mutableColumn.changeValIfNotEqual(isVisible, {x -> x.isVisible}, {x,y -> y.copy(isVisible = x)})
        mutableColumn.changeValIfNotEqual(filterText, {x -> x.filterText}, {x,y -> y.copy(filterText = x)})
        mutableColumn.changeValIfNotEqual(sortIndex, {x -> x.sortOrder}, {x,y ->y.copy(sortOrder = x)})

        mutableColumn.addSource(mutableColumn){
            it?.let { cd->
               if (cd != currentCol()){
                    sheetVm.updateColumn(cd)
                }
            }
        }
    }
}