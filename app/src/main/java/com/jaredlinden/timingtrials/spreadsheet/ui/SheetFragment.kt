package com.jaredlinden.timingtrials.spreadsheet.ui


import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.adapters.GenericListItemAdapter
import com.jaredlinden.timingtrials.spreadsheet.GridData
import com.jaredlinden.timingtrials.spreadsheet.test.TestAdapter
import com.jaredlinden.timingtrials.spreadsheet.test.TestLayoutManager
import com.jaredlinden.timingtrials.spreadsheet.test.TestLayoutManagerOptions

import kotlinx.android.synthetic.main.fragment_spreadsheet.view.*


/*

TODO: when screen rotates in 2nd, 3rd, 4th etc. sheet, have the
left top cell remain the same (do not reset to A1)


 */

class SheetFragment : Fragment() {

    lateinit var fragmentRecyclerView : RecyclerView

    companion object {
        fun newInstance() = SheetFragment()
    }

    private var viewModel: SheetViewModel? = null

    fun startSearch() {
        val sheetLayoutManager = fragmentRecyclerView.layoutManager as SheetLayoutManager
        //sheetLayoutManager.startSearch()
    }

//    override fun onResume() {
//        super.onResume()
//        val sheetLayoutManager = fragmentRecyclerView.layoutManager as SheetLayoutManager
//        sheetLayoutManager.removeAllViews()
//        fragmentRecyclerView.adapter?.notifyDataSetChanged()
//        //sheetLayoutManager.resetLayoutManagerSearch()
//    }

    fun processSearchJump() {
        //jumpToNewCoordinates().resetLayoutManagerSearch()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view =  inflater.inflate(R.layout.fragment_spreadsheet, container, false)







        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = activity?.run {
            ViewModelProviders.of(this).get(SheetViewModel::class.java)
        }

        val topRow = viewModel?.topRow
        val leftColumn = viewModel?.leftColumn

        if (topRow != null) {
            SheetLayoutManager.topRow = topRow
        }

        if (leftColumn != null) {
            SheetLayoutManager.leftColumn = leftColumn
        }

    }


//    fun jumpToNewCoordinates() : SheetLayoutManager {
//        val tr = viewModel?.topRow
//        val lc = viewModel?.leftColumn
//
//        if (tr != null && lc != null) {
//            SheetLayoutManager.topRow = tr
//            SheetLayoutManager.leftColumn = lc
//        }
//
//        val sheetAdapter = fragmentRecyclerView.adapter as SheetAdapter
//        sheetAdapter.notifyDataSetChanged()
//
//        val sheetLayoutManager = fragmentRecyclerView.layoutManager as SheetLayoutManager
//        sheetLayoutManager.removeAllViews()
//        return sheetLayoutManager
//
//    }

    fun processUri() {
        val sheetLayoutManager = fragmentRecyclerView.layoutManager as SheetLayoutManager
        sheetLayoutManager.resetToTopLeft()
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel?.topRow = SheetLayoutManager.topRow
        viewModel?.leftColumn = SheetLayoutManager.leftColumn
    }


}
