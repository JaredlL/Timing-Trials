package com.jaredlinden.timingtrials.spreadsheet

import com.jaredlinden.timingtrials.domain.SortType

interface ISheetLayoutManagerOptions {
    val data: List<List<String>>
    val sheetColumns: List<ISheetColumn>
    val numberOfRows: Int
    val isEmpty: Boolean
    val focusedColumn: Int
    fun getRowHeight(row: Int): Int
    fun onCellClick(row: Int, col: Int)
    fun onCellLongPress(row: Int, col: Int)
}

interface ISheetColumn{
    val headingText:String
    val headingTextWidth: Float
    val width: Float
    val focused: Boolean
    val sortType: SortType
    fun onClick()
}

class SheetLayoutManagerOptions(override val data: List<List<String>>, override val sheetColumns: List<ISheetColumn>) : ISheetLayoutManagerOptions {


    override val numberOfRows: Int = data.size

    override val isEmpty = data.isEmpty()

    override val focusedColumn: Int = 0

    override fun getRowHeight(row:Int):Int{
        return  3
    }

    override fun onCellClick(row: Int, col: Int) {
    }

    override fun onCellLongPress(row: Int, col: Int) {

    }
}

