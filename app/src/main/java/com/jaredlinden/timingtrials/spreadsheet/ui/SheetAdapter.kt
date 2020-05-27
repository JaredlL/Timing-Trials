package com.jaredlinden.timingtrials.spreadsheet.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.jaredlinden.timingtrials.R


class SheetAdapter internal constructor(val context: Context, val density: Int): RecyclerView.Adapter<SheetAdapter.ViewHolder>(){

    companion object{
        private const val CELL = 3
        private const val COLUMN_MARKER = 2
        private const val ROW_MARKER = 1

        private const val ROW_MARKER_WIDTH = 30
        private const val ROW_HEIGHT = 60
    }


    inner  class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        var text = ""
        val textView: TextView = view.findViewById(R.id.spreadSheetTextView)
        fun bind(resultText: String){
            textView.text = resultText
        }
    }

    val layoutInflater = LayoutInflater.from(context)

    val dummy = listOf(listOf("",""))
    var mOptions: ITestLayoutManagerOptions = SheetLayoutManagerOptions(dummy,  listOf(""))

    fun setNewItems(options: ITestLayoutManagerOptions){
        //mItems = newItems
        mOptions = options
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (getItemViewType(position) == CELL) {
            makeCell(holder, position)
        } else if (getItemViewType(position) == COLUMN_MARKER) {
            makeColumnMarker(holder, position)
        } else {
            makeRowMarker(holder, position)
        }

    }

    fun totalColumns(): Int = mOptions.numberOfColumns + 1
    fun totalRows(): Int = mOptions.numberOfRows + 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_spreadsheet, parent, false)
        return ViewHolder(view)
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

    private fun makeCell(viewHolder: ViewHolder, position: Int) {


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
            val denseWidth = baseWidth * density
            viewHolder.textView.width = denseWidth


        val denseHeight = density * mOptions.getRowHeight(r)

        viewHolder.textView.height = denseHeight

        //val alpha = 0;
        val alpha = 255

        val sdk = android.os.Build.VERSION.SDK_INT
        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            viewHolder.textView.setBackgroundDrawable(ColorDrawable(Color.argb(alpha, 255, 255, 255)))

        } else {
            viewHolder.textView.setBackground(ColorDrawable(Color.argb(alpha, 255, 255, 255)))
        }

        viewHolder.textView.setOnClickListener {
            Toast.makeText(context, cellsValue, Toast.LENGTH_SHORT).show()
        }
    }

    private fun makeRowMarker(viewHolder: ViewHolder, position: Int) {

        var (row,_) = posToMarkers(position)

        row--
        //al workbook = spreadsheet?.workbook
        //val sheet = workbook?.sheetList?.get(workbook.currentSheet)
        val height = mOptions.getRowHeight(row)
        val denseHeight = density * height

        viewHolder.textView.height = denseHeight
        setBackground(viewHolder)

        val rm = Integer.toString(row+1)

        val width = ROW_MARKER_WIDTH * density

        viewHolder.textView.height = density * height
        viewHolder.textView.setWidth(width)
        viewHolder.textView.setText(rm)
        viewHolder.textView.setGravity(Gravity.CENTER)
    }

    // must be wide enough for 2-3 letter column markers
    private fun makeColumnMarker(viewHolder: ViewHolder, position: Int) {

        val (_, c) = posToMarkers(position)


        val size =  mOptions.numberOfColumns

        if (position == 0) {
            viewHolder.textView.width = ROW_MARKER_WIDTH * density

        } else {
            if (size != null) {
                if (size >= c) {
                    val baseWidth = mOptions.getColumnWidth(c-1)
                    val denseWidth = baseWidth * density
                    viewHolder.textView.width = denseWidth
                } else {
                    viewHolder.textView.width = ROW_MARKER_WIDTH * density * 2
                }
            } else {
                viewHolder.textView.width = ROW_MARKER_WIDTH * density * 2
            }

            var text = mOptions.headings[position-1]
            viewHolder.textView.setText(text)
        }

        setBackground(viewHolder)

        viewHolder.textView.setGravity(Gravity.CENTER)
    }

    private fun setBackground(viewHolder: ViewHolder) {

        //val gray = 84
        val gray = 193

        val sdk = android.os.Build.VERSION.SDK_INT
        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            //setBackgroundDrawable();
            viewHolder.textView.setBackgroundDrawable(ColorDrawable(Color.argb(255, gray,gray,gray)))

        } else {
            viewHolder.textView.setBackground(ColorDrawable(Color.argb(255, gray,gray,gray)))

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