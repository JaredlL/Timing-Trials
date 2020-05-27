package com.jaredlinden.timingtrials.spreadsheet.ui

interface ITestLayoutManagerOptions {
    val data: List<List<String>>
    val headings: List<String>
    val numberOfColumns: Int
    val numberOfRows: Int
    val isEmpty: Boolean
    fun getColumnWidth(column: Int): Int
    fun getRowHeight(row: Int): Int
}

class SheetLayoutManagerOptions(override val data: List<List<String>>, override val headings: List<String>) : ITestLayoutManagerOptions {

    override val numberOfColumns: Int = headings.size
    override val numberOfRows: Int = data.size

    override val isEmpty = data.isEmpty()


    override fun getColumnWidth(column:Int):Int{
       return when(column){
           4-> 40
           6-> 80
           else -> 60
        }

    }
    override fun getRowHeight(row:Int):Int{
        return when(row){
            4-> 40
            6-> 80
            else -> 60
        }

    }




}

