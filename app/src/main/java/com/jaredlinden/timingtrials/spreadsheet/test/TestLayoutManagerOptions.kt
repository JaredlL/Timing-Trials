package com.jaredlinden.timingtrials.spreadsheet.test

class TestLayoutManagerOptions(val data: List<List<String>>, val headings: List<String>){

    val numberOfColumns: Int = data.first().size
    val numberOfRows: Int = data.size

    val totalItems = numberOfColumns * numberOfRows

    fun getRowHeight(row:Int):Int{
        return 100
    }

    fun getColumnWidth(column:Int):Int{
        return 100
    }




}

