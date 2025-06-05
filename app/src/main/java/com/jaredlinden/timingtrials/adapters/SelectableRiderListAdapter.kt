package com.jaredlinden.timingtrials.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.data.Rider
import com.jaredlinden.timingtrials.databinding.ListItemSelectableRiderBinding
import com.jaredlinden.timingtrials.setup.SelectedRidersInformation

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
                riderLayout.setOnLongClickListener { longPress(riderVm.id)
                    true
                }

                executePendingBindings()

                checkBox.setOnClickListener {
                    if(checkBox.isChecked != mSelected.asSequence().map { it.id }.contains(riderVm.id)){
                        if(checkBox.isChecked){
                            val nSelected = mSelected + listOf(riderVm)
                            mSelected = nSelected
                            addRiderToSelection(riderVm)
                        }else{
                            val nSelected = mSelected.filterNot { it.id == riderVm.id }
                            mSelected = nSelected
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
        mRiders[position].let { rider ->
            with(holder){
                itemView.tag = rider.id
                holder.longPress = editRider
                bind(rider)
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return mRiders[position].id
    }

    var editRider = {riderId: Long -> Unit}
    var removeRiderFromSelection = {rider:Rider -> Unit}
    var addRiderToSelection = {rider:Rider -> Unit}

    fun setRiders(newInfo: SelectedRidersInformation){
        val newSelected = newInfo.allRiderList.filter { newInfo.selectedIds.contains(it.id) }

        if(mRiders != newInfo.allRiderList || mSelected != newSelected){
            mRiders = newInfo.allRiderList
            mSelected = newSelected
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int{ return mRiders.count() }

}