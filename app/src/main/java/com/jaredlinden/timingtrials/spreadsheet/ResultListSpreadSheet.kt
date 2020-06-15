package com.jaredlinden.timingtrials.spreadsheet

import com.jaredlinden.timingtrials.data.IResult
import com.jaredlinden.timingtrials.domain.ColumnData
import com.jaredlinden.timingtrials.domain.SortType


class ResultListSpreadSheet(val results: List<IResult>,
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



    override val data: List<List<String>> =
            if (comp != null) {
                results.sortedWith(comp).filter { res -> columns.all { it.passesFilter(res) } }.map { res -> visibleCols.map { it.getValue(res) } }
            } else {
                results.filter { res -> columns.all { it.passesFilter(res) } }.map { res -> visibleCols.map { it.getValue(res) } }
            }


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
        results[row].timeTrial?.id?.let {
            onNavigateToTt(it)
        }

    }

    fun copy(results: List<IResult> = this.results, columns: List<ColumnData> = this.columns): ResultListSpreadSheet {
        return ResultListSpreadSheet(results, columns, setNewColumns, onNavigateToTt, measureString)
    }


}


//class RiderResultListSpreadSheet(val results: List<IResult>, val distConverter: LengthConverter):ISheetLayoutManagerOptions{
//
//    override val data: List<List<String>> = results.map { listFromResult(it) }
//
//    private fun listFromResult(res:IResult) : List<String>{
//        val dateString = res.dateSet?.let { ConverterUtils.dateToDisplay(it) }?:""
//        val timeString = res.resultTime?.let { ConverterUtils.toSecondsDisplayString(it) }?:""
//        val distDisplayString = res.course.length.let { distConverter.lengthToDisplay(it * res.laps) }
//        val averageSpeedMetersPerMilisecond = averageSpeed(res)
//        return listOf(res.course.courseName, timeString, dateString,  distDisplayString, res.laps.toString(), averageSpeedMetersPerMilisecond)
//    }
//
//    fun averageSpeed(res:IResult) : String{
//        val rt = res.resultTime
//       return if(rt != null && rt != 0L){
//           val averageSpeedMetersPerMilisecond = distConverter.convert(res.course.length * res.laps) * (3600000.0 / rt.toDouble())
//        return "%2.2f".format(averageSpeedMetersPerMilisecond)
//        }else{
//            ""
//        }
//    }
//
//    override val headings: List<String> = listOf("Course", "Time", "Date", "Distance (${distConverter.unitDef.miniString})", "Laps", "Avg Speed (${distConverter.unitDef.miniString}/h)")
//
//
//    override val numberOfColumns: Int = headings.count()
//
//
//    override val numberOfRows: Int  = results.count()
//
//    override val isEmpty: Boolean = results.isEmpty()
//
//
//    val headingWidths = headings.map { it.length }
//    val colWidths: List<Int> = data.fold(headings.map { it.length }, {currentLengths,strings -> currentLengths.zip(strings).map { if(it.first >= it.second.length ) it.first else it.second.length } })
//
//    override fun getColumnWidth(column: Int): Int {
//        return if(headingWidths[column] != colWidths[column]) colWidths[column] * 8 + 10 else colWidths[column] * 8
//    }
//
//    override fun getRowHeight(row: Int): Int {
//        return 30
//    }
//
//}

//class CourseResultListSpreadSheet(val results: List<IResult>, val distConverter: LengthConverter):ISheetLayoutManagerOptions{
//
//    override val data: List<List<String>> = results.map { listFromResult(it) }
//
//    private fun listFromResult(res:IResult) : List<String>{
//        val dateString = res.dateSet?.let { ConverterUtils.dateToDisplay(it) }?:""
//        val timeString = res.resultTime?.let { ConverterUtils.toSecondsDisplayString(it) }?:""
//        //val distDisplayString = res.course.length.let { distConverter.lengthToDisplay(it * res.laps) }
//        val averageSpeedMetersPerMilisecond = averageSpeed(res)
//        return listOf(res.rider.fullName(), timeString, dateString,  res.riderClub,  res.gender.smallString(),res.category,  averageSpeedMetersPerMilisecond, res.laps.toString())
//    }
//
//    fun averageSpeed(res:IResult) : String{
//        val rt = res.resultTime
//        return if(rt != null && rt != 0L){
//            val averageSpeedMetersPerMilisecond = distConverter.convert(res.course.length * res.laps) * (3600000.0 / rt.toDouble())
//            return "%2.2f".format(averageSpeedMetersPerMilisecond)
//        }else{
//            ""
//        }
//    }
//
//    override val headings: List<String> = listOf("Rider", "Time", "Date", "Club", "Gender", "Category","Avg Speed ${distConverter.unitDef.miniString}/h", "Laps" )
//
//
//    override val numberOfColumns: Int = headings.count()
//
//
//    override val numberOfRows: Int  = results.count()
//
//    override val isEmpty: Boolean = results.isEmpty()
//
//
//    val headingWidths = headings.map { it.length }
//    val colWidths: List<Int> = data.fold(headings.map { it.length }, {currentLengths,strings -> currentLengths.zip(strings).map { if(it.first >= it.second.length ) it.first else it.second.length } })
//
//    override fun getColumnWidth(column: Int): Int {
//        return if(headingWidths[column] != colWidths[column]) colWidths[column] * 8 + 10 else colWidths[column] * 8
//    }
//
//    override fun getRowHeight(row: Int): Int {
//        return 30
//    }
//
//}