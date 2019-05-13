package com.android.jared.linden.timingtrials.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.databinding.ListItemRiderBinding
import com.android.jared.linden.timingtrials.databinding.ListItemSelectableRiderBinding
import com.android.jared.linden.timingtrials.setup.SelectableRiderViewWrapper


/**
 * Adapter for the [RecyclerView] in [RiderListFragment].
 */



class SelectableRiderListAdapter internal constructor(val context: Context): RecyclerView.Adapter<SelectableRiderListAdapter.SelectableRiderViewHolder>() {


    inner class SelectableRiderViewHolder(val binding: ListItemSelectableRiderBinding): RecyclerView.ViewHolder(binding.root) {

        var longPress = {(rider):Rider -> Unit}

        fun bind(riderVm: SelectableRiderViewWrapper){

            binding.apply{
                selectableRider = riderVm
                riderLayout.setOnLongClickListener { longPress(riderVm.rider)
                    true
                }

                executePendingBindings()
            }
        }
    }

    var mRiders: List<SelectableRiderViewWrapper> = listOf()
    //var mSelected: ArrayList<Long> = arrayListOf()
    val layoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectableRiderViewHolder {

        val binding = DataBindingUtil.inflate<ListItemSelectableRiderBinding>(layoutInflater, R.layout.list_item_selectable_rider, parent, false)
        return SelectableRiderViewHolder(binding)

    }

    override fun onBindViewHolder(holder: SelectableRiderViewHolder, position: Int) {
        mRiders.get(position).let { rider ->
            with(holder){
                itemView.tag = rider
                holder.longPress = editRider
                bind(rider)
            }
        }
    }

    var editRider = {(SelectableRiderViewWrapper):Rider -> Unit}

    fun setRiders(newRiders: List<SelectableRiderViewWrapper>){
        mRiders = newRiders
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int{ return mRiders.count() }

}