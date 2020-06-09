package com.jaredlinden.timingtrials.spreadsheet

import com.jaredlinden.timingtrials.domain.SortType

interface ISheetLayoutManagerOptions {
    val data: List<List<String>>
    //val headings: List<String>
    //val numberOfColumns: Int
    val sheetColumns: List<ISheetColumn>
    val numberOfRows: Int
    val isEmpty: Boolean
    fun getColumnWidth(column: Int): Int
    fun getRowHeight(row: Int): Int
    //fun onColumnClick(columnPosition: Int)
    fun onCellClick(row: Int, col: Int)
    fun onCellLongPress(row: Int, col: Int)

}

interface ISheetColumn{
    val headingText:String
    val width: Int
    val sortType: SortType
    fun onClick()
}

class SheetLayoutManagerOptions(override val data: List<List<String>>, override val sheetColumns: List<ISheetColumn>) : ISheetLayoutManagerOptions {

    //val numberOfColumns: Int = sheetColumns.size
    override val numberOfRows: Int = data.size

    override val isEmpty = data.isEmpty()


    override fun getColumnWidth(column:Int):Int{
        return  3
    }
    override fun getRowHeight(row:Int):Int{
        return  3
    }


    override fun onCellClick(row: Int, col: Int) {

    }

    override fun onCellLongPress(row: Int, col: Int) {

    }
}

