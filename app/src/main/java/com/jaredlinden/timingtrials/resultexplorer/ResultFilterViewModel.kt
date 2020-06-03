package com.jaredlinden.timingtrials.resultexplorer

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.jaredlinden.timingtrials.domain.*
import com.jaredlinden.timingtrials.util.LengthConverter
import com.jaredlinden.timingtrials.util.changeValIfNotEqual
import com.jaredlinden.timingtrials.util.setIfNotEqual



class ResultFilterViewModel(column: ColumnData) {

    val mutableColumn = MediatorLiveData<ColumnData>().apply { value = column }

    val isVisible = MutableLiveData(column.isVisible)
    val filterText = MutableLiveData(column.filterText)
    val sortIndex= MutableLiveData(column.sortOrder)
    val description = column.description

    fun clearText(){
        filterText.value  =""
       // mutableColumn.value = mutableColumn.value?.copy(filterText = "")
    }

    init {
        mutableColumn.addSource(mutableColumn){
            it?.let { col->
                isVisible.setIfNotEqual(col.isVisible)
                filterText.setIfNotEqual(col.filterText)
                sortIndex.setIfNotEqual(col.sortOrder)
            }
        }

        mutableColumn.changeValIfNotEqual(isVisible, {x -> x.isVisible}, {x,y -> y.copy(isVisible = x)})
        mutableColumn.changeValIfNotEqual(filterText, {x -> x.filterText}, {x,y -> y.copy(filterText = x)})
        mutableColumn.changeValIfNotEqual(sortIndex, {x -> x.sortOrder}, {x,y -> y.copy(sortOrder = x)})

    }

    companion object{
        fun getAllColumns(distConverter: LengthConverter):List<ColumnData>{
            return listOf(
                    ColumnData(RiderNameColumn()),
                    ColumnData(CourseNameColumn()),
                    ColumnData(TimeColumn()),
                    ColumnData(ClubColumn()),
                    ColumnData(GenderColumn()),
                    ColumnData(CategoryColumn()),
                    ColumnData(LapsColumn()),
                    ColumnData(DateColumn()),
                    ColumnData(DistanceColumn(distConverter)),
                    ColumnData(SpeedColumn(distConverter)))

        }

        fun getAllColumnViewModels(distConverter: LengthConverter):List<ResultFilterViewModel>{
            return getAllColumns(distConverter).map { ResultFilterViewModel(it) }
        }



    }

}