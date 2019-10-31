package com.android.jared.linden.timingtrials.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.databinding.ListItemGenericBinding
import com.android.jared.linden.timingtrials.databinding.ListItemSelectableRiderBinding
import com.android.jared.linden.timingtrials.setup.SelectableRiderViewWrapper
import com.android.jared.linden.timingtrials.ui.IGenericListItem

class GenericListItemAdapter internal constructor(val context: Context): RecyclerView.Adapter<GenericListItemAdapter.GenericListItemViewHolder>() {


    inner class GenericListItemViewHolder(val binding: ListItemGenericBinding): RecyclerView.ViewHolder(binding.root) {

        //var longPress = {(rider): IGenericListItem -> Unit}

        fun bind(genericItem: IGenericListItem){

            binding.apply{
                item = genericItem

                executePendingBindings()
            }
        }
    }

    private var mItems: List<IGenericListItem> = listOf()
    //var mSelected: ArrayList<Long> = arrayListOf()
    val layoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericListItemViewHolder {

        val binding = DataBindingUtil.inflate<ListItemGenericBinding>(layoutInflater, R.layout.list_item_generic, parent, false)
        return GenericListItemViewHolder(binding)

    }

    override fun onBindViewHolder(holder: GenericListItemViewHolder, position: Int) {
        mItems.get(position).let { gitem ->
            with(holder){
                itemView.tag = gitem
                bind(gitem)
            }
        }
    }


    fun setItems(newItems: List<IGenericListItem>){
        mItems = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int{ return mItems.count() }

}