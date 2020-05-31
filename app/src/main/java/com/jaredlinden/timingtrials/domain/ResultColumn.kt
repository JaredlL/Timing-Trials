package com.jaredlinden.timingtrials.domain

import com.jaredlinden.timingtrials.data.IResult

interface IResultColumn{

    val key: String
    val description : String
    fun getValue(result: IResult): String
    val isVisible : Boolean
    val index: Int
    val sortOrder: Int
    val filterText: String

}

class ResultColumn {


}