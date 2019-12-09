package com.android.jared.linden.timingtrials.timetrialresults

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.databinding.ListItemResultBinding

class ResultListAdapter internal constructor(val context: Context): RecyclerView.Adapter<ResultListAdapter.ResultViewHolder>(){


    inner class ResultViewHolder(binding: ListItemResultBinding): RecyclerView.ViewHolder(binding.root){
        private val _binding = binding

        fun bind(result: ResultCell, position: Int){
            _binding.apply {
                //lifecycleOwner = activity
                viewModel = result

                if(position.rem(rowLength) == 0 || position < rowLength){
                    resultTextView.typeface = Typeface.DEFAULT_BOLD
                }else{
                    resultTextView.typeface = Typeface.DEFAULT
                }
                executePendingBindings()
            }

        }
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        mResults[position].let {
            with(holder){
                itemView.tag = it
                bind(it, position)
            }
        }

    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val binding: ListItemResultBinding = DataBindingUtil.inflate(layoutInflater, R.layout.list_item_result, parent, false)
        return ResultViewHolder(binding)
    }

    override fun getItemViewType(position: Int): Int {
        return 1
    }

    override fun getItemCount(): Int {
        return mResults.size
    }

    private var mResults: List<ResultCell> = listOf()
    private var rowLength = 0

    val layoutInflater = LayoutInflater.from(context)
    var rowCount = 0
    //var itemHeight = 0

    fun setResults(newResults: List<ResultRowViewModel>){

        rowLength = newResults.first().row.size
        mResults = newResults.flatMap { it.row }
        rowCount = newResults.size
        notifyDataSetChanged()
    }

}

