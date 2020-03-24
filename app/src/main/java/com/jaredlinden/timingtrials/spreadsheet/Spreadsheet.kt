package com.jaredlinden.timingtrials.spreadsheet

import android.util.Log

import java.io.InputStream


class Spreadsheet {

    var workbook : Workbook = Workbook()

    // empty spreadsheet
    constructor() {
        workbook = Workbook()
    }

    // spreadsheetFormat from string to other
    constructor(inputStream: InputStream, spreadsheetFormat: String?) {

        if (spreadsheetFormat.equals("csv")) {
            workbook = CsvWorkbook(inputStream)
        }

    }


}

open class Workbook {

    var sheetList: MutableList<Sheet> = mutableListOf()

    var currentSheet : Int

    init {
        val sheet = Sheet()
        sheetList.add(sheet)
        currentSheet = 0
    }


}

open class Sheet {

    val TOO_LARGE = 99999

    var rowList : MutableList<Row> = mutableListOf()

    var columnWidths = mutableListOf<Int>()

    var name : String = ""

    fun getNumberOfColumns() : Int {
        if (rowList.size == 0) return 0
        else return rowList[0].cellList.size
    }

    fun getRow(i : Int) : Row {

        // XXX: TOO_LARGE is a big number to catch bad input
        while (i >= rowList.size && i < TOO_LARGE) {
            rowList.add(Row())
        }
        return rowList[i]
    }

}

open class Row {

    var cellList : MutableList<Cell> = mutableListOf()

    var height = 60

    fun getCell(column : Int) : Cell {
        while(cellList.size <= column) {
            cellList.add(Cell())
        }
        return cellList[column]
    }

}

open class Cell {

    var cellValue : String = ""

}