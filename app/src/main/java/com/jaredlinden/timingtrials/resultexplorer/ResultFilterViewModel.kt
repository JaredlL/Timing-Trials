package com.jaredlinden.timingtrials.resultexplorer

import androidx.databinding.BindingAdapter
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.jaredlinden.timingtrials.domain.*
import com.jaredlinden.timingtrials.util.LengthConverter
import com.jaredlinden.timingtrials.util.changeValIfNotEqual
import com.jaredlinden.timingtrials.util.setIfNotEqual




class ResultFilterViewModel(column:ColumnData, val sheetVm:ISheetViewModel) {

    private val columnKey = column.key

    private fun currentCol(): ColumnData?{
       return sheetVm.resultSpreadSheet.value?.columns?.firstOrNull{it.key == columnKey}
    }

    private val mutableColumn = MediatorLiveData<ColumnData>().apply { value = currentCol() }


    val imageRes = Transformations.map(mutableColumn){
        it?.imageRes
    }

    val isVisible = MutableLiveData(column.isVisible)
    val filterText = MutableLiveData(column.filterText)
    val sortIndex= MutableLiveData(column.sortOrder)
    val description = Transformations.map(mutableColumn){
        it?.description

    }

    fun clearText(){
        filterText.value  =""
    }

    init {
        mutableColumn.addSource(Transformations.map(sheetVm.resultSpreadSheet){it?.columns?.firstOrNull{it.key == columnKey}}){res->
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

    }

}