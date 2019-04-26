package com.android.jared.linden.timingtrials.timing

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.databinding.ListItemEventBinding
import com.android.jared.linden.timingtrials.ui.EventViewWrapper

class EventListAdapter internal constructor(val context:Context): RecyclerView.Adapter<EventListAdapter.EventViewHolder>(){

    inner class EventViewHolder(binding: ListItemEventBinding): RecyclerView.ViewHolder(binding.root){
        private val _binding = binding

        fun bind(eventWrapper: EventViewWrapper){
            _binding.apply {
                event = eventWrapper
                executePendingBindings()
            }

        }
    }

    var mEvents: List<EventViewWrapper> = listOf()
    val layoutInflater = LayoutInflater.from(context)

    fun setEvents(newEvents: List<EventViewWrapper>){
        mEvents = newEvents
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        mEvents[position].let {
            with(holder){
                itemView.tag = it
                bind(it)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding: ListItemEventBinding = DataBindingUtil.inflate(layoutInflater, R.layout.list_item_event, parent, false)
        return EventViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return mEvents.count()
    }
}