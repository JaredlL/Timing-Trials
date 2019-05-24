package com.android.jared.linden.timingtrials.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.data.Rider
import com.android.jared.linden.timingtrials.data.RiderLight
import com.android.jared.linden.timingtrials.databinding.ListItemOrderableRiderBinding
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder


class OrderableRiderListAdapter(context: Context) : RecyclerView.Adapter<OrderableRiderListAdapter.OrderableRiderViewHolder>(),
        DraggableItemAdapter<OrderableRiderListAdapter.OrderableRiderViewHolder> {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        // requires static value, it means need to keep the same value
        // even if the item position has been changed.
        return mRiders[position].id ?: 0L
    }

    inner class OrderableRiderViewHolder(val binding: ListItemOrderableRiderBinding): AbstractDraggableItemViewHolder(binding.root){

        fun bind(rd:RiderLight, position: Int){
            binding.apply {
                rider = rd
                pos = position + 1
            }
        }
    }

    private val layoutInflater = LayoutInflater.from(context)
    private var mRiders: List<RiderLight> = listOf()
    var onMove: (from:Int, to:Int) -> Unit = {_,_ -> Unit}

    fun setRiders(newRiders: List<RiderLight>){
        mRiders = newRiders
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
        return mRiders.count()
    }

    override fun onBindViewHolder(holder: OrderableRiderViewHolder, position: Int) {
        mRiders.get(position).let { rider ->
            with(holder){
                itemView.tag = rider
                bind(rider, position)
            }
        }
    }

    override fun onGetItemDraggableRange(holder: OrderableRiderViewHolder, position: Int): ItemDraggableRange? {
        return null
    }

    override fun onCheckCanStartDrag(holder: OrderableRiderViewHolder, position: Int, x: Int, y: Int): Boolean {
        return true
    }

    override fun onItemDragStarted(position: Int) {

    }

    override fun onMoveItem(fromPosition: Int, toPosition: Int) {
        onMove(fromPosition, toPosition)
    }

    override fun onCheckCanDrop(draggingPosition: Int, dropPosition: Int): Boolean {
        return true
    }

    override fun onItemDragFinished(fromPosition: Int, toPosition: Int, result: Boolean) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderableRiderViewHolder {

        val binding = DataBindingUtil.inflate<ListItemOrderableRiderBinding>(layoutInflater, R.layout.list_item_orderable_rider, parent, false)
        return OrderableRiderViewHolder(binding)
    }





}