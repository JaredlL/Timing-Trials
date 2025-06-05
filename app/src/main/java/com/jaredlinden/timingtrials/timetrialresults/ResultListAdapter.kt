package com.jaredlinden.timingtrials.timetrialresults

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jaredlinden.timingtrials.databinding.ListItemResultBinding

class ResultListAdapter internal constructor(val context: Context, val editResult: (Long?) -> Unit): RecyclerView.Adapter<ResultListAdapter.ResultViewHolder>(){

    inner class ResultViewHolder(binding: ListItemResultBinding): RecyclerView.ViewHolder(binding.root){
        private val _binding = binding

        fun bind(result: ResultCell, position: Int){
            _binding.apply {
                viewModel = result

                if(position.rem(rowLength) == 0 || position < rowLength){
                    resultTextView.typeface = Typeface.DEFAULT_BOLD
                }else{
                    resultTextView.typeface = Typeface.DEFAULT
                }

                resultTextView.setOnLongClickListener {
                    editResult(result.resultId)
                    true
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
        val binding: ListItemResultBinding = ListItemResultBinding.inflate(layoutInflater, parent, false)
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


    fun setResults(newResults: List<ResultRowViewModel>){

        rowLength = newResults.first().row.size
        mResults = newResults.flatMap { it.row }
        rowCount = newResults.size
        notifyDataSetChanged()
    }
}

