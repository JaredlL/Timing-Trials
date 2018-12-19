package com.android.jared.linden.timingtrials.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.databinding.ListItemRiderBinding
import com.android.jared.linden.timingtrials.viewmodels.RiderViewModel

/**
 * Adapter for the [RecyclerView] in [RiderListFragment].
 */

class RiderListAdapter internal constructor(context: Context): RecyclerView.Adapter<RiderListAdapter.RiderViewHolder>() {


    inner class RiderViewHolder(binding: ListItemRiderBinding): RecyclerView.ViewHolder(binding.root) {
        private val _binding = binding

        fun bind(riderVm: RiderViewModel){
            _binding.apply{
                rider = riderVm
                executePendingBindings()
            }
        }
    }

    var mRiders: List<RiderViewModel> = listOf()
    val layoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RiderViewHolder {

        val binding: ListItemRiderBinding = DataBindingUtil.inflate(layoutInflater, R.layout.list_item_rider, parent, false)
        return RiderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RiderViewHolder, position: Int) {
        mRiders.get(position).let { rider ->
            with(holder){
                itemView.tag = rider
                bind(rider)
            }
        }
    }


    fun setRiders(newRiders: List<Rider>){
        mRiders = newRiders.map { r -> RiderViewModel(r) }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int{ return mRiders.count() }

}