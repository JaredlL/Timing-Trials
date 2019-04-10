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

class RiderListAdapter internal constructor(val context: Context): RecyclerView.Adapter<RiderListAdapter.RiderViewHolder>() {


    inner class RiderViewHolder(binding: ListItemRiderBinding): RecyclerView.ViewHolder(binding.root) {
        private val _binding = binding

        var longPress = {(rider):Rider -> Unit}

        fun bind(riderVm: Rider){

            _binding.apply{
                rider = riderVm
                riderLayout.setOnLongClickListener { longPress(riderVm)
                    true
                }

                executePendingBindings()
            }
        }
    }

    var mRiders: List<Rider> = listOf()
    val layoutInflater = LayoutInflater.from(context)
    var selectable: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RiderListAdapter.RiderViewHolder {

        val binding = DataBindingUtil.inflate<ListItemRiderBinding>(layoutInflater, R.layout.list_item_rider, parent, false)
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

    fun setRiders(newRiders: List<Rider>){
        mRiders = newRiders
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int{ return mRiders.count() }

}

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectableRiderListAdapter.SelectableRiderViewHolder {

        val binding = DataBindingUtil.inflate<ListItemSelectableRiderBinding>(layoutInflater, R.layout.list_item_selectable_rider, parent, false)
        return SelectableRiderViewHolder(binding)

    }

    override fun onBindViewHolder(holder: SelectableRiderListAdapter.SelectableRiderViewHolder, position: Int) {
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