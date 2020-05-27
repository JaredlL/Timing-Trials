package com.jaredlinden.timingtrials.spreadsheet.test

class TestLayoutManagerOptions(val data: List<List<String>>, val headings: List<String>){

    val numberOfColumns: Int = data.first().size
    val numberOfRows: Int = data.size
    

    fun getRowHeight(row:Int):Int{
        return 60
    }

    fun getColumnWidth(column:Int):Int{
       return when(column){
           4-> 40
           6-> 80
           else -> 60
        }

    }




}

