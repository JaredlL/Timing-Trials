package com.jaredlinden.timingtrials.spreadsheet

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
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginEnd
import androidx.recyclerview.widget.RecyclerView
import com.jaredlinden.timingtrials.R


class SheetAdapter internal constructor(val context: Context, val displayMetrics: DisplayMetrics, val snackBarCallback: () -> Unit): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

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
        val imageView: ImageView = view.findViewById(R.id.headerImageView)
        val layout: ConstraintLayout = view.findViewById(R.id.headerLayout)
        fun bind(resultText: String){
            textView.text = resultText
        }
    }

    val layoutInflater = LayoutInflater.from(context)

    val dummy = listOf(listOf("",""))
    var mOptions: ISheetLayoutManagerOptions = SheetLayoutManagerOptions(dummy,  listOf(""))

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

    fun totalColumns(): Int = mOptions.numberOfColumns + 1
    fun totalRows(): Int = mOptions.numberOfRows + 1

    var widthOfALetter: Int? = null
    var hieghtOfALetter: Int? = null
    var widthOfImage: Int? = null


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
        if(widthOfALetter == null){
            val tv: TextView = view.findViewById(R.id.spreadSheetTextView)
            tv.text = "Z"


            val widthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            val heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            tv.measure(widthMeasureSpec, heightMeasureSpec)
            widthOfALetter = tv.measuredWidth
            hieghtOfALetter = tv.measuredHeight + 2

        }

        return CellViewHolder(view)
    }

    fun createColumnVh(parent: ViewGroup): ColumnHeaderViewHolder{
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_spreadsheet_header, parent, false)
        if(widthOfALetter == null){
            val tv: TextView = view.findViewById(R.id.spreadSheetHeaderTextView)
            tv.text = "Z"


            val widthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            val heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            tv.measure(widthMeasureSpec, heightMeasureSpec)
            widthOfALetter = tv.measuredWidth
            hieghtOfALetter = tv.measuredHeight

        }
        return ColumnHeaderViewHolder(view)
    }

    override fun getItemCount(): Int {
        return totalColumns() * totalRows()
    }

    override fun getItemViewType(position: Int): Int {

        val coordinate = posToMarkers(position)

        return if (coordinate.r == 0) {
            COLUMN_MARKER
        } else if (coordinate.c == 0) {
            ROW_MARKER
        } else {
            CELL
        }

    }

    val cellAdd = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24F, displayMetrics).toInt()
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
            cellsValue = " "
        } else {
            //cellsValue = sheet.getRow(r).getCell(c).cellValue
            cellsValue = mOptions.data[r][c]
        }

        viewHolder.textView.text = cellsValue


        val baseWidth = mOptions.getColumnWidth(c)
        val calcWidth = (baseWidth *  (widthOfALetter?:0)) + (cellAdd * 2).toInt()

        viewHolder.textView.width = calcWidth //+ (widthOfImage?:0)


        val denseHeight = (hieghtOfALetter?:1) * mOptions.getRowHeight(r) * 1.2

        viewHolder.textView.height = denseHeight.toInt()
        viewHolder.textView.setGravity(Gravity.CENTER)

        //val alpha = 0;
        val alpha = 255

//        val sdk = android.os.Build.VERSION.SDK_INT
//        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
//            viewHolder.textView.setBackgroundDrawable(ColorDrawable(Color.argb(alpha, 255, 255, 255)))
//
//        } else {
//            viewHolder.textView.setBackground(ColorDrawable(Color.argb(alpha, 255, 255, 255)))
//        }

        viewHolder.textView.setOnClickListener {
            //Toast.makeText(context, "$r,$c", Toast.LENGTH_SHORT).show()
            mOptions.onCellClick(r,c)
            snackBarCallback()

        }
    }

    private fun makeRowMarker(viewHolder: CellViewHolder, position: Int) {

        var (row,_) = posToMarkers(position)
        row--




        val gray = 193

        val sdk = android.os.Build.VERSION.SDK_INT
        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            //setBackgroundDrawable();
            viewHolder.textView.setBackgroundDrawable(ColorDrawable(Color.argb(255, gray,gray,gray)))

        } else {
            viewHolder.textView.setBackground(ColorDrawable(Color.argb(255, gray,gray,gray)))

        }

        val rm = (row + 1).toString()

        val width = ROW_MARKER_WIDTH *(widthOfALetter?:10)
        val height = (hieghtOfALetter?:1) * mOptions.getRowHeight(row) * 1.2

        viewHolder.textView.height = height.toInt()
        viewHolder.textView.width = width
        viewHolder.textView.text = rm
        viewHolder.textView.gravity = Gravity.CENTER
    }

    // must be wide enough for 2-3 letter column markers
    private fun makeColumnMarker(viewHolder: ColumnHeaderViewHolder, position: Int) {

        val (_, c) = posToMarkers(position)



        if (position == 0) {
            viewHolder.imageView.visibility = View.GONE
            viewHolder.textView.width = (ROW_MARKER_WIDTH * (widthOfALetter?:10)) -  cellAdd.toInt()
            viewHolder.textView.text = ""


        } else {

            //val baseWidth = mOptions.getColumnWidth(c-1)
            viewHolder.imageView.visibility = View.VISIBLE
            viewHolder.textView.width = mOptions.getColumnWidth(c-1) * (widthOfALetter?:1)

            var text = mOptions.headings[c-1]
            viewHolder.textView.text = text
        }
        viewHolder.textView.height = COLUMN_MARKER_HEIGHT * density

        if(c > 0){
            viewHolder.layout.setOnClickListener {
                mOptions.onColumnClick(c - 1)
            }
        }else{
            viewHolder.layout.setOnClickListener {
            }
        }

        viewHolder.textView.setGravity(Gravity.CENTER)



        setBackground(viewHolder)


    }

    private fun setBackground(viewHolder: ColumnHeaderViewHolder) {

        //val gray = 84
        val gray = 193

        val sdk = android.os.Build.VERSION.SDK_INT
        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            //setBackgroundDrawable();
            viewHolder.layout.setBackgroundDrawable(ColorDrawable(Color.argb(255, gray,gray,gray)))

        } else {
            viewHolder.layout.setBackground(ColorDrawable(Color.argb(255, gray,gray,gray)))

        }
    }

    // translate position to column and row
    private fun posToMarkers(pos: Int): Coordinate {
        val col =  pos.rem(totalColumns())
        val row = pos / (totalColumns())
        return Coordinate(row, col)
    }

    private data class Coordinate(val r : Int, val c : Int)
}