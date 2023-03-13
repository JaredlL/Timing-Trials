package com.jaredlinden.timingtrials.timing

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.databinding.ListItemRiderStatusBinding
import com.jaredlinden.timingtrials.ui.RiderStatus
import com.jaredlinden.timingtrials.ui.RiderStatusViewWrapper

class RiderStatusAdapter internal constructor(val context: Context): RecyclerView.Adapter<RiderStatusAdapter.RiderStatusViewHolder>(){

    inner class RiderStatusViewHolder(binding: ListItemRiderStatusBinding): RecyclerView.ViewHolder(binding.root){
        private val _binding = binding

        fun bind(status: RiderStatusViewWrapper){
            _binding.apply {
                viewModel = status

                riderStatusTextView.background = when(status.status){
                    RiderStatus.NOT_STARTED -> AppCompatResources.getDrawable(context, R.drawable.background_rider_status_inactive)
                    RiderStatus.RIDING -> AppCompatResources.getDrawable(context, R.drawable.background_rider_status_active)
                    RiderStatus.DNS -> AppCompatResources.getDrawable(context, R.drawable.background_rider_status_dnf)
                    RiderStatus.DNF -> AppCompatResources.getDrawable(context, R.drawable.background_rider_status_dnf)
                    else-> AppCompatResources.getDrawable(context, R.drawable.background_rider_status_inactive)
                }

                riderStatusTextView.setOnLongClickListener {
                    onLongClick(status)
                    true
                }


                executePendingBindings()
            }

        }
    }

    var mStatus: List<RiderStatusViewWrapper> = listOf()
    val layoutInflater = LayoutInflater.from(context)

    var onLongClick = {rider: RiderStatusViewWrapper -> Unit}

    fun setRiderStatus(newStatus: List<RiderStatusViewWrapper>){
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

    override fun getItemId(position: Int): Long {
        return mStatus[position].timeTrialRider.riderId
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RiderStatusViewHolder {
        val binding: ListItemRiderStatusBinding = DataBindingUtil.inflate(layoutInflater, R.layout.list_item_rider_status, parent, false)
        return RiderStatusViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return mStatus.count()
    }
}