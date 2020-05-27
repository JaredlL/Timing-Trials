package com.jaredlinden.timingtrials.spreadsheet.ui

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jaredlinden.timingtrials.R
import kotlinx.android.synthetic.main.fragment_spreadsheet.view.*

class SheetFragment : Fragment()  {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val view =  inflater.inflate(R.layout.fragment_spreadsheet, container, false)


        val data: List<List<String>> = (1..200).map { x-> ('a'..'z').map { y -> x.toString() + y } }

        //val gridData = GridData(data)

        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)

        val density = displayMetrics.density.toInt()
        val adapter = SheetAdapter(requireContext(), density)

        val opts = SheetLayoutManagerOptions(data, ('a'..'z').map { y-> y.toString()})

        adapter.setNewItems(opts)


        val recyclerView = view.recyclerView

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true)



        recyclerView.adapter = adapter




        //adapter.notifyDataSetChanged()

        recyclerView.layoutManager = SheetLayoutManager(opts)


        return view
    }
}