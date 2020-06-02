package com.jaredlinden.timingtrials.resultexplorer

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jaredlinden.timingtrials.domain.IResultColumn
import com.jaredlinden.timingtrials.util.setIfNotEqual

class ResultFilterViewModel(val column: IResultColumn) : ViewModel() {

    val mutableColumn = MediatorLiveData<IResultColumn>().apply { value = column }

    val isVisible = MutableLiveData(column.isVisible)
    val filterText = MutableLiveData(column.filterText)
    val sortIndex= MutableLiveData(column.sortOrder)

    init {
        mutableColumn.addSource(mutableColumn){
            it?.let { col->
                isVisible.setIfNotEqual(col.isVisible)
                filterText.setIfNotEqual(col.filterText)
                sortIndex.setIfNotEqual(col.sortOrder)
            }
        }

        mutableColumn.addSource(isVisible){res->
            res?.let {str->
                mutableColumn.value?.let { col->
                    if(col.isVisible != str){
                        mutableColumn.value = col.copy(isVisible = str)
                    }
                }
            }
        }
    }

}