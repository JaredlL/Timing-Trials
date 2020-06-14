package com.jaredlinden.timingtrials.spreadsheet

import android.R.attr.bottom
import android.R.attr.top
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.domain.SortType


class SheetAdapter internal constructor(val context: Context, val displayMetrics: DisplayMetrics, val paint: Paint, val snackBarCallback: () -> Unit): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    companion object{
        private const val CELL = 3
        private const val COLUMN_MARKER = 2
        private const val ROW_MARKER = 1

        private const val ROW_MARKER_WIDTH = 3
        private const val COLUMN_MARKER_HEIGHT = 30
        private const val ROW_HEIGHT = 60

        private const val WIDTH_MULTIPLIER = 12
    }

    val density = displayMetrics.density.toInt()


    inner class CellViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        var text = ""
        val textView: TextView = view.findViewById(R.id.spreadSheetTextView)
        fun bind(resultText: String){
            textView.text = resultText
        }
    }

    inner class ColumnHeaderViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        var text = ""
        val textView: TextView = view.findViewById(R.id.spreadSheetHeaderTextView)
        //val imageView: ImageView = view.findViewById(R.id.headerImageView)
        val layout: LinearLayout = view.findViewById(R.id.headerLayout)
        fun bind(resultText: String){
            textView.text = resultText
        }
    }


    val dummy = listOf(listOf("",""))
    var mOptions: ISheetLayoutManagerOptions = SheetLayoutManagerOptions(dummy,  listOf())

    fun setNewItems(options: ISheetLayoutManagerOptions){
        //mItems = newItems
        mOptions = options
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (getItemViewType(position) == CELL) {
            makeCell(holder as CellViewHolder, position)
        } else if (getItemViewType(position) == COLUMN_MARKER) {
            makeColumnMarker(holder as ColumnHeaderViewHolder, position)
        } else {
            makeRowMarker(holder as CellViewHolder, position)
        }

    }

    fun totalColumns(): Int = mOptions.sheetColumns.size + 1
    fun totalRows(): Int = mOptions.numberOfRows + 1




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {



       return when(viewType){
           CELL -> createCellVh(parent)
           ROW_MARKER -> createCellVh(parent)
           COLUMN_MARKER -> createColumnVh(parent)
           else -> createCellVh(parent)
        }


    }

    fun createCellVh(parent: ViewGroup): CellViewHolder{
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_spreadsheet, parent, false)
        return CellViewHolder(view)
    }

    fun createColumnVh(parent: ViewGroup): ColumnHeaderViewHolder{
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_spreadsheet_header, parent, false)
        return ColumnHeaderViewHolder(view)
    }

    override fun getItemCount(): Int {
        return totalColumns() * totalRows()
    }

    override fun getItemViewType(position: Int): Int {

        val coordinate = posToMarkers(position)

        return when {
            position == 0 -> {
                ROW_MARKER
            }
            coordinate.r == 0 -> {
                COLUMN_MARKER
            }
            coordinate.c == 0 -> {
                ROW_MARKER
            }
            else -> {
                CELL
            }
        }

    }

    private fun getColumnWidthInPixels(column:Int):Int{

        val baseWidth = mOptions.sheetColumns[column].width.toInt()
        val headingTextWidth = mOptions.sheetColumns[column].headingTextWidth.toInt()

        return if(mOptions.sheetColumns[column].sortType == SortType.NONE){
            baseWidth + sidePadding
        }else{
            Math.max(baseWidth, headingTextWidth + (widthOfImage * 2)) + sidePadding
        }
    }

    private val sidePadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8F, displayMetrics).toInt()
    private val widthOfImage = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 26F, displayMetrics).toInt()
    private val oneDpInPixel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1F, displayMetrics).toInt()

    val widthOfALetter: Int = paint.measureText("0").toInt() + oneDpInPixel * 2
    val hieghtOfALetter: Int = android.graphics.Rect().also {r ->  paint.getTextBounds("000",0,2,r) }.height() * 2 //+ oneDpInPixel

    private fun getRowHeight(row:Int):Int{

        return if(row == 0){
            (widthOfALetter * 3.5).toInt()
        }else{
            widthOfALetter * 3
        }
    }


    private fun makeCell(viewHolder: CellViewHolder, position: Int) {


        val numColumns = totalColumns()

        val coordinate = posToMarkers(position)
        var r = coordinate.r
        var c = coordinate.c

        r--
        c--

        val cellsValue: String?

        if (numColumns == 0) return

        if (numColumns < 1) {
            cellsValue = ""
        } else {

            cellsValue = mOptions.data[r][c]
        }

        viewHolder.textView.text = cellsValue


        viewHolder.textView.width = getColumnWidthInPixels(c)



        viewHolder.textView.height = getRowHeight(r+2)
        viewHolder.textView.setGravity(Gravity.CENTER)

        val alpha = 255


        viewHolder.textView.setOnClickListener {
            //Toast.makeText(context, "$r,$c", Toast.LENGTH_SHORT).show()
            mOptions.onCellClick(r,c)
            snackBarCallback()

        }
        viewHolder.textView.setOnLongClickListener {
            //Toast.makeText(context, "$r,$c", Toast.LENGTH_SHORT).show()
            mOptions.onCellLongPress(r,c)
            true

        }
    }


    val drawable = context.getDrawable(R.drawable.background_spreadsheet_heading)
    private fun makeRowMarker(viewHolder: CellViewHolder, position: Int) {

        var (row,_) = posToMarkers(position)
        row--





        val gray = 193

        val sdk = android.os.Build.VERSION.SDK_INT

                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            //setBackgroundDrawable();
            viewHolder.textView.setBackgroundDrawable(drawable)

        } else {
            viewHolder.textView.setBackground(drawable)

        }


        if(row+1 <= 0){

            viewHolder.textView.text = ""
            viewHolder.textView.height = getRowHeight(0)

        }else{
            viewHolder.textView.height = getRowHeight(8)
            val rm = (row + 1).toString()
            viewHolder.textView.text = rm
        }



        val width = ROW_MARKER_WIDTH *(widthOfALetter?:10)

        viewHolder.textView.width = width
        viewHolder.textView.gravity = Gravity.CENTER
    }

    // must be wide enough for 2-3 letter column markers
    private fun makeColumnMarker(viewHolder: ColumnHeaderViewHolder, position: Int) {

        val (_, c) = posToMarkers(position)

        val mc = c -1

        val colData = mOptions.sheetColumns[mc]

        val widthOfText = colData.headingTextWidth.toInt()
        val fullColWidth = getColumnWidthInPixels(mc)
        val leftPadding = ((fullColWidth - widthOfText) / 2)
        val rightPadding = if ((fullColWidth - widthOfText).rem(2) == 1) leftPadding + 1 else leftPadding

        viewHolder.textView.height = getRowHeight(0)
        when(colData.sortType){
            SortType.ASCENDING->{
                viewHolder.textView.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.ic_baseline_arrow_drop_down_24, 0)
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(leftPadding, 0, (rightPadding - widthOfImage), 0)
                viewHolder.textView.setLayoutParams(params)
                viewHolder.textView.width = (widthOfText + widthOfImage)
            }
            SortType.DESCENDING->{
                viewHolder.textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_arrow_drop_up_24, 0)
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(leftPadding, 0, (rightPadding - widthOfImage), 0)
                viewHolder.textView.setLayoutParams(params)
                viewHolder.textView.width = (widthOfText + widthOfImage)
            }
            else->{
                viewHolder.textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, 0, 0, 0)
                viewHolder.textView.setLayoutParams(params)
                viewHolder.textView.width = fullColWidth
            }

        }


        viewHolder.textView.text = colData.headingText



            viewHolder.layout.setOnClickListener {
                colData.onClick()
            }

        viewHolder.textView.setGravity(Gravity.CENTER)



        //setBackground(viewHolder)


    }


    // translate position to column and row
    private fun posToMarkers(pos: Int): Coordinate {
        val col =  pos.rem(totalColumns())
        val row = pos / (totalColumns())
        return Coordinate(row, col)
    }

    private data class Coordinate(val r : Int, val c : Int)
}