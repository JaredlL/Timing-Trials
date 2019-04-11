package com.android.jared.linden.timingtrials.viewdata

import android.content.Context
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView

class GenericListAdapter internal constructor(val context: Context): RecyclerView.Adapter<GenericViewHolder>() {


    inner class GenericViewHolder(binding: ListItemRiderBinding): RecyclerView.ViewHolder(binding.root) {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder {

        val binding = DataBindingUtil.inflate(layoutInflater, R.layout.list_item_rider, parent, false)
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