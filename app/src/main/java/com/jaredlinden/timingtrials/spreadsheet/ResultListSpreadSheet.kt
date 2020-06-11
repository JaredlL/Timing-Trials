package com.jaredlinden.timingtrials.spreadsheet

import com.jaredlinden.timingtrials.data.IResult
import com.jaredlinden.timingtrials.domain.ColumnData
import com.jaredlinden.timingtrials.domain.SortType


class  ResultListSpreadSheet(val results: List<IResult>,
                             val columns: List<ColumnData>,
                             val onTransform: (x:List<IResult>, y: List<ColumnData>, z: ResultListSpreadSheet) -> Unit,
                             val measureString: (s:String) -> Float
)
    :ISheetLayoutManagerOptions {

    private val visibleCols = columns.filter { it.isVisible }

    override val data: List<List<String>> = results.filter { res-> columns.all { it.passesFilter(res) } }.map { res -> visibleCols.map { it.getValue(res) } }

    //private val colWidths: List<Int> = data.fold(visibleCols.map { it.description.length }, {currentLengths,strings -> currentLengths.zip(strings).map { if(it.first >= it.second.length ) it.first else it.second.length } })
    private val colWidths: List<Float> = data.fold(visibleCols.map { measureString(it.description) }, {currentLongest,strings -> currentLongest.zip(strings).map { if (it.first >= measureString(it.second) ) it.first else measureString(it.second)} })

    override val sheetColumns: List<ISheetColumn> = visibleCols.zip(colWidths) { colData,w-> object : ISheetColumn{

        override val headingText: String = colData.description
        override val headingTextWidth: Float = measureString(headingText)
        override val width: Float = w
        override val sortType: SortType = colData.sortType
        override fun onClick() {

            val (newSortType, multiplier) = when(colData.sortType){
                SortType.DESCENDING -> Pair(SortType.ASCENDING, -1)
                else -> Pair(SortType.DESCENDING, 1)
            }

            val newData = results.sortedWith (Comparator{ r1,r2 -> multiplier * colData.compare(r1,r2) })

            onTransform(newData, columns.map { columnData ->
                if(colData.key == columnData.key)
                    columnData.copy(sortType =  newSortType)
                else columnData.copy(sortType = SortType.NONE)
            }, this@ResultListSpreadSheet)
        }

    }
    }

//    override fun getColumnWidth(column: Int): Float {
//       return sheetColumns[column].width
//    }

    override val numberOfRows: Int = data.size

    override val isEmpty: Boolean = data.isEmpty()

    override val focusedColumn: Int = 1


    override fun getRowHeight(row: Int): Int {
        return 1
    }

//    fun onColumnClick(columnPosition: Int) {
//        val clickedCol = columns.filter { it.isVisible }[columnPosition]
//        val multiplier = if (clickedCol.compareAscending) 1 else -1
//        val newData = results.sortedWith (Comparator{ r1,r2 -> multiplier * clickedCol.compare(r1,r2) })
//        onTransform(newData, columns.map { columnData -> if(clickedCol.key == columnData.key) columnData.copy(compareAscending = !columnData.compareAscending) else columnData  })
//        //onTransform(ResultListSpreadSheet(newData,columns.map { columnData -> if(clickedCol.key == columnData.key) columnData.copy(compareAscending = !columnData.compareAscending) else columnData  },onTransform))
//    }

//    fun updateColumn(newColumnData: ColumnData): ResultListSpreadSheet{
//        return ResultListSpreadSheet(results,columns.map { columnData -> if(newColumnData.key == columnData.key) newColumnData else columnData  },onTransform);
//    }

    override fun onCellClick(row: Int, col: Int) {
        val cellString =data[row][col]
        val currentColFilter = visibleCols[col].filterText
        val newFilter = if(cellString != currentColFilter) cellString else ""
        if(visibleCols[col].filterText != newFilter){
            val newCol = visibleCols[col].copy(filterText = newFilter)
            onTransform(results, updateColumnns(newCol), this)
        }

        //onTransform(ResultListSpreadSheet(results, updateColumnns(newCol), onTransform))
    }

    override fun onCellLongPress(row: Int, col: Int) {

    }

    fun updateColumnns(newColumnData: ColumnData): List<ColumnData>{
        return columns.map { if(it.key == newColumnData.key) newColumnData else it  }
    }

    fun copy(results: List<IResult> = this.results, columns: List<ColumnData> = this.columns): ResultListSpreadSheet{
        return ResultListSpreadSheet(results, columns, onTransform, measureString)
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