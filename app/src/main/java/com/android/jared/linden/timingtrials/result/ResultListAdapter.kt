package com.android.jared.linden.timingtrials.result

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.databinding.ListItemResultBinding
import com.android.jared.linden.timingtrials.databinding.ListItemRiderStatusBinding
import com.android.jared.linden.timingtrials.ui.ResultCell
import com.android.jared.linden.timingtrials.ui.ResultViewWrapper
import com.android.jared.linden.timingtrials.ui.RiderStatusViewWrapper

class ResultListAdapter internal constructor(val context: Context): RecyclerView.Adapter<ResultListAdapter.ResultViewHolder>(){


    inner class ResultViewHolder(binding: ListItemResultBinding): RecyclerView.ViewHolder(binding.root){
        private val _binding = binding

        fun bind(status: String, position: Int){
            _binding.apply {
                content = status

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val binding: ListItemResultBinding = DataBindingUtil.inflate(layoutInflater, R.layout.list_item_result, parent, false)
        return ResultViewHolder(binding)
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    val TYPE_CELL = 0
    val TYPE_ROWHEADING = 1

    override fun getItemCount(): Int {
        return mResults.size
    }

    private var mResults: List<String> = listOf()
    private var rowLength = 0
    val layoutInflater = LayoutInflater.from(context)

    fun setResults(newResults: List<ResultViewWrapper>){
        val first = newResults.first()
        rowLength = first.resultsRow.size
        val headRow = mutableListOf<String>()
        headRow.add("Rider")
        headRow.add("Category")
        headRow.add("Club")
        headRow.add("Total Time")

        if(first.result.splits.size > 1){
            first.result.splits.forEachIndexed { index, _ -> if(index - 1 <first.result.splits.size) headRow.add("Split ${index + 1}") }
        }

        headRow.addAll(newResults.flatMap { it.resultsRow.map { res -> res.contents } })
        mResults = headRow
        notifyDataSetChanged()
    }





}