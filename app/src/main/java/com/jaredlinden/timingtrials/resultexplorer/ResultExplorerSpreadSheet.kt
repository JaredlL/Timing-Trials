package com.jaredlinden.timingtrials.resultexplorer

import com.jaredlinden.timingtrials.data.IResult
import com.jaredlinden.timingtrials.domain.ColumnData
import com.jaredlinden.timingtrials.domain.SortType
import com.jaredlinden.timingtrials.spreadsheet.ISheetColumn
import com.jaredlinden.timingtrials.spreadsheet.ISheetLayoutManagerOptions


class ResultExplorerSpreadSheet(val results: List<IResult>,
                                val columns: List<ColumnData>,
                                val setNewColumns: (columns: List<ColumnData>) -> Unit,
                                val onNavigateToTt: (Long) -> Unit,
                                val measureString: (s: String) -> Float
) : ISheetLayoutManagerOptions {

    private val visibleCols = columns.filter { it.isVisible }

    private val comp: Comparator<IResult>? = columns.firstOrNull { it.sortType != SortType.NONE}?.let {
        when (it.sortType){
            SortType.ASCENDING -> Comparator { r1, r2 -> -1 * it.compare(r1, r2)}
            else -> Comparator { r1, r2 -> it.compare(r1, r2)}
        }
    }

    private val sortedVisibleResults = if (comp != null) {
        results.sortedWith(comp).filter { res -> columns.all { it.passesFilter(res) } }
    } else {
        results.filter { res -> columns.all { it.passesFilter(res) } }
    }

    override val data: List<List<String>> = sortedVisibleResults.map { res -> visibleCols.map { it.getValue(res) } }


    private val colWidths: List<Float> = data.fold(visibleCols.map { measureString(it.description) }, { currentLongest, strings -> currentLongest.zip(strings).map { if (it.first >= measureString(it.second)) it.first else measureString(it.second) } })

    override val sheetColumns: List<ISheetColumn> = visibleCols.zip(colWidths) { colData, width ->
        object : ISheetColumn {

            override val headingText: String = colData.description
            override val headingTextWidth: Float = measureString(headingText)
            override val width: Float = width
            override val focused: Boolean = colData.isFocused
            override val sortType: SortType = colData.sortType
            override fun onClick() {

                val newSortType = when (colData.sortType) {
                    SortType.DESCENDING -> SortType.ASCENDING
                    else -> SortType.DESCENDING
                }
                val newCols = columns.map { columnData -> if (colData.key == columnData.key) columnData.copy(sortType = newSortType, isFocused = true) else columnData.copy(sortType = SortType.NONE, isFocused = false) }
                setNewColumns(newCols)
            }
        }
    }


    override val numberOfRows: Int = data.size

    override val isEmpty: Boolean = data.isEmpty()

    override val focusedColumn: Int = sheetColumns.indexOfFirst { it.focused }

    override fun getRowHeight(row: Int): Int {
        return 1
    }

    override fun onCellClick(row: Int, col: Int) {
        val cellString = data[row][col]
        val currentColFilter = visibleCols[col].filterText
        val newFilter = if (cellString != currentColFilter) cellString else ""
        if (visibleCols[col].filterText != newFilter) {
            val newCol = visibleCols[col].copy(filterText = newFilter, isFocused = true)
            setNewColumns(columns.map { if (it.key == newCol.key) newCol else it.copy(isFocused = false) })
        }
    }

    override fun onCellLongPress(row: Int, col: Int) {

        sortedVisibleResults[row].timeTrial?.id?.let {
            onNavigateToTt(it)
        }
    }

    fun copy(results: List<IResult> = this.results, columns: List<ColumnData> = this.columns): ResultExplorerSpreadSheet {
        return ResultExplorerSpreadSheet(results, columns, setNewColumns, onNavigateToTt, measureString)
    }
}
