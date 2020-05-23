package com.jaredlinden.timingtrials.spreadsheet.test

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.spreadsheet.GridData
import com.jaredlinden.timingtrials.spreadsheet.ui.SheetLayoutManager
import com.jaredlinden.timingtrials.timetrialresults.ResultCell
import com.jaredlinden.timingtrials.timetrialresults.ResultListAdapter
import com.jaredlinden.timingtrials.timetrialresults.ResultViewModel
import kotlinx.android.synthetic.main.fragment_spreadsheet.*
import kotlinx.android.synthetic.main.fragment_spreadsheet.view.*
import kotlinx.android.synthetic.main.fragment_spreadsheet.view.button4

class TestSheetFragment : Fragment()  {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val view =  inflater.inflate(R.layout.fragment_spreadsheet, container, false)


        val data = (1..20).map { x-> ('a'..'t').map {y-> x.toString() + y  } }

        //val gridData = GridData(data)


        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)

        val density = displayMetrics.density.toInt()
        val adapter = TestAdapter(requireContext(), density)

        val opts = TestLayoutManagerOptions(data)

        adapter.setNewItems(opts)


        val recyclerView = view.recyclerView

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true)


        val sheetLayoutManager = SheetLayoutManager()
        //LinearLayoutManager(requireContext())

        recyclerView.adapter = adapter




        //adapter.notifyDataSetChanged()

        recyclerView.layoutManager = TestLayoutManager(opts)

        view?.button4?.setOnClickListener {
            adapter.notifyDataSetChanged()
        }

        return view
    }
}