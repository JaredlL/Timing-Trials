package com.android.jared.linden.timingtrials.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.databinding.ListItemRiderBinding
import com.android.jared.linden.timingtrials.viewmodels.RiderViewModel

/**
 * Adapter for the [RecyclerView] in [RiderListFragment].
 */

class RiderListAdapter internal constructor(val context: Context): RecyclerView.Adapter<RiderListAdapter.RiderViewHolder>() {


    inner class RiderViewHolder(binding: ListItemRiderBinding): RecyclerView.ViewHolder(binding.root) {
        private val _binding = binding

        var longPress = {(rider):Rider -> Unit}

        fun bind(riderVm: Rider){

            _binding.apply{
                rider = riderVm
                checkBox.isChecked = mSelected.contains(riderVm.Id)
                checkBox.setOnClickListener {
                   if(checkBox.isChecked) {
                       mSelected.add(riderVm.Id ?: 0)
                   } else {
                       (mSelected.remove(riderVm.Id))
                   }
                }
                relLay.setOnLongClickListener { longPress(riderVm)
                    true
                }

                executePendingBindings()
            }
        }
    }

    var mRiders: List<Rider> = listOf()
    var mSelected: ArrayList<Long> = arrayListOf()
    val layoutInflater = LayoutInflater.from(context)
    var selectable: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RiderViewHolder {

        val binding: ListItemRiderBinding = DataBindingUtil.inflate(layoutInflater, R.layout.list_item_rider, parent, false)
        binding.checkBox.visibility = if(selectable) View.VISIBLE else View.GONE
        return RiderViewHolder(binding)

    }

    override fun onBindViewHolder(holder: RiderViewHolder, position: Int) {
        mRiders.get(position).let { rider ->
            with(holder){
                itemView.tag = rider
                holder.longPress = editRider
                bind(rider)
            }
        }
    }

    var editRider = {(rider):Rider -> Unit}

    fun setRiders(newRiders: List<Rider>, selectedIds: ArrayList<Long>){
        mRiders = newRiders
        mSelected = selectedIds
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int{ return mRiders.count() }

}