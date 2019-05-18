package com.android.jared.linden.timingtrials.result

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.android.jared.linden.timingtrials.ui.ResultViewWrapper
import com.android.jared.linden.timingtrials.util.ITEM_ID_EXTRA
import com.android.jared.linden.timingtrials.util.argument
import com.android.jared.linden.timingtrials.util.getViewModel
import com.android.jared.linden.timingtrials.util.injector
import kotlinx.android.synthetic.main.result_activity.*
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Rect
import android.view.View
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.LinearLayoutManager
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable


class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.android.jared.linden.timingtrials.R.layout.result_activity)

        val timeTrialId by argument<Long>(ITEM_ID_EXTRA)
        val resultViewModel = getViewModel { injector.resultViewModel() }.apply { initialise(timeTrialId) }

        val viewManager = GridLayoutManager(this, 2)


        val adapter = ResultListAdapter(this)

        resultViewModel.timeTrial.observe(this, Observer {res->
            res?.let {tt->
                val newRes = tt.helper.results.asSequence().map { res -> ResultViewWrapper(res) }.sortedBy { it.result.totalTime }.toList()
                if(newRes.isNotEmpty()){
                    val rowLength = newRes.first().resultsRow.size

                    viewManager.spanCount = rowLength + 2
                    viewManager.spanSizeLookup = (object : GridLayoutManager.SpanSizeLookup(){
                        override fun getSpanSize(position: Int): Int {
                           return if (position.rem(rowLength) == 0 || position.rem(rowLength) == 1) {
                                2
                            }else {
                                1
                            }

                        }
                    })
                    adapter.setResults(newRes)
                }

            }
        })

        resultRecyclerView.layoutManager = viewManager
        resultRecyclerView.adapter = adapter
        resultRecyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))




        }




    }



class SpacesItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {

   override fun getItemOffsets(outRect: Rect, view: View,
                       parent: RecyclerView, state: RecyclerView.State) {
        //outRect.left = space
        //outRect.right = space
        outRect.bottom = space
        view.background = ColorDrawable(Color.BLACK)

        // Add top margin only for the first item to avoid double space between items
//        if (parent.getChildLayoutPosition(view) == 0) {
//          //  outRect.top = space
//        } else {
//          //  outRect.top = 0
//        }
    }
}

class DividerItemDecoration(context: Context, orientation: Int) : RecyclerView.ItemDecoration() {

    private val mDivider: Drawable?

    private var mOrientation: Int = 0

    init {
        val a = context.obtainStyledAttributes(ATTRS)
        mDivider = a.getDrawable(0)
        a.recycle()
        setOrientation(orientation)
    }

    fun setOrientation(orientation: Int) {
        if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
            throw IllegalArgumentException("invalid orientation")
        }
        mOrientation = orientation
    }

    override fun onDraw(c: Canvas, parent: RecyclerView) {
        if (mOrientation == VERTICAL_LIST) {
            drawVertical(c, parent)
        } else {
            drawHorizontal(c, parent)
        }
    }

    fun drawVertical(c: Canvas, parent: RecyclerView) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child
                    .layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = top + mDivider!!.intrinsicHeight
            mDivider.setBounds(left, top, right, bottom)
            mDivider.draw(c)
        }
    }

    fun drawHorizontal(c: Canvas, parent: RecyclerView) {
        val top = parent.paddingTop
        val bottom = parent.height - parent.paddingBottom

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child
                    .layoutParams as RecyclerView.LayoutParams
            val left = child.right + params.rightMargin
            val right = left + mDivider!!.intrinsicHeight
            mDivider.setBounds(left, top, right, bottom)
            mDivider.draw(c)
        }
    }

    override fun getItemOffsets(outRect: Rect, itemPosition: Int, parent: RecyclerView) {
        if (mOrientation == VERTICAL_LIST) {
            outRect.set(0, 0, 0, mDivider!!.intrinsicHeight)
        } else {
            outRect.set(0, 0, mDivider!!.intrinsicWidth, 0)
        }
    }

    companion object {

        private val ATTRS = intArrayOf(android.R.attr.listDivider)

        val HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL

        val VERTICAL_LIST = LinearLayoutManager.VERTICAL
    }
}
