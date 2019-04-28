package com.android.jared.linden.timingtrials.timing

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.databinding.ListItemRiderStatusBinding
import com.android.jared.linden.timingtrials.ui.RiderStatusViewWrapper

class RiderStatusAdapter internal constructor(val context: Context): RecyclerView.Adapter<RiderStatusAdapter.RiderStatusViewHolder>(){

    inner class RiderStatusViewHolder(binding: ListItemRiderStatusBinding): RecyclerView.ViewHolder(binding.root){
        private val _binding = binding

        fun bind(status: RiderStatusViewWrapper){
            _binding.apply {
                viewModel = status
                executePendingBindings()
            }

        }
    }

    var mStatus: List<RiderStatusViewWrapper> = listOf()
    val layoutInflater = LayoutInflater.from(context)

    fun setEvents(newStatus: List<RiderStatusViewWrapper>){
        mStatus = newStatus
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RiderStatusViewHolder, position: Int) {
        mStatus[position].let {
            with(holder){
                itemView.tag = it
                bind(it)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RiderStatusViewHolder {
        val binding: ListItemRiderStatusBinding = DataBindingUtil.inflate(layoutInflater, R.layout.list_item_rider_status, parent, false)
        return RiderStatusViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return mStatus.count()
    }
}