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
import com.android.jared.linden.timingtrials.setup.SelectedRidersInformation
import kotlinx.android.synthetic.main.list_item_selectable_rider.view.*
import java.util.HashSet


/**
 * Adapter for the [RecyclerView] in [RiderListFragment].
 */



class SelectableRiderListAdapter internal constructor(val context: Context): RecyclerView.Adapter<SelectableRiderListAdapter.SelectableRiderViewHolder>() {


    inner class SelectableRiderViewHolder(val binding: ListItemSelectableRiderBinding): RecyclerView.ViewHolder(binding.root) {

        var longPress = {_: Long -> Unit}

        fun bind(riderVm: Rider){

            binding.apply{
                rider = riderVm
                checkBox.isChecked = mSelected.asSequence().map { it.id }.contains(riderVm.id)
                riderLayout.setOnLongClickListener { longPress(riderVm.id?:0)
                    true
                }

                executePendingBindings()

                checkBox.setOnClickListener {
                    if(riderVm.id != null && checkBox.isChecked != mSelected.asSequence().map { it.id }.contains(riderVm.id)){
                        if(checkBox.isChecked){
                            val nSelected = mSelected + listOf(riderVm)
                            mSelected = nSelected
                            //riderSelectionChanged(mSelected)
                            addRiderToSelection(riderVm)
                        }else{
                            val nSelected = mSelected.filterNot { it.id == riderVm.id }
                            mSelected = nSelected
                            //riderSelectionChanged(mSelected)
                            removeRiderFromSelection(riderVm)
                        }

                    }
                }
            }
        }
    }

    var mRiders: List<Rider> = listOf()
    var mSelected: List<Rider> = listOf()
    val layoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectableRiderViewHolder {

        val binding = DataBindingUtil.inflate<ListItemSelectableRiderBinding>(layoutInflater, R.layout.list_item_selectable_rider, parent, false)
        return SelectableRiderViewHolder(binding)

    }

    override fun onBindViewHolder(holder: SelectableRiderViewHolder, position: Int) {
        mRiders.get(position).let { rider ->
            with(holder){
                itemView.tag = rider.id
                holder.longPress = editRider
                bind(rider)
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return mRiders[position].id?:0
    }

    var editRider = {_: Long -> Unit}
    var riderSelectionChanged = {_:List<Rider> -> Unit}

    var removeRiderFromSelection = {rider:Rider -> Unit}

    var addRiderToSelection = {rider:Rider -> Unit}

    fun setRiders(newInfo: SelectedRidersInformation){
        val newSelected = newInfo.timeTrial.riderList.map { it.riderData }

        if(mRiders != newInfo.allRiderList || mSelected != newSelected){
            mRiders = newInfo.allRiderList
            mSelected = newSelected
            System.out.println("JAREDMSG -> NOTIFY Select Riders Changed")
            notifyDataSetChanged()
        }

    }

    override fun getItemCount(): Int{ return mRiders.count() }

}