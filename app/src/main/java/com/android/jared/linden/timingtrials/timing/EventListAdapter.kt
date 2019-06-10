package com.android.jared.linden.timingtrials.timing

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.jared.linden.timingtrials.R
import com.android.jared.linden.timingtrials.databinding.ListItemEventButtonBinding
import com.android.jared.linden.timingtrials.databinding.ListItemEventTextBinding
import com.android.jared.linden.timingtrials.domain.TimelineEventType
import com.android.jared.linden.timingtrials.ui.EventViewWrapper

class EventListAdapter internal constructor(val context:Context): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    inner class ButtonEventViewHolder(binding: ListItemEventButtonBinding): RecyclerView.ViewHolder(binding.root){
        private val _binding = binding

        fun bind(eventWrapper: EventViewWrapper){
            _binding.apply {
                event = eventWrapper
                executePendingBindings()
            }

        }
    }
    inner class TextEventViewHolder(binding: ListItemEventTextBinding): RecyclerView.ViewHolder(binding.root){
        private val _binding = binding

        fun bind(eventWrapper: EventViewWrapper){
            _binding.apply {
                event = eventWrapper

                if(eventWrapper.event.eventType == TimelineEventType.RIDER_PASSED){
                    text1.setTextColor(Color.BLUE)
                }
                else if(eventWrapper.event.eventType == TimelineEventType.RIDER_FINISHED){
                    text1.setTextColor(Color.MAGENTA)
                }else{
                    text1.setTextColor(Color.BLACK)
                }
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when(getItemViewType(position)){
            R.layout.list_item_event_button ->{
                mEvents[position].let {
                    with(holder as ButtonEventViewHolder){
                        itemView.tag = it
                        bind(it)
                    }
                }
            }
            else ->{
                mEvents[position].let {
                    with(holder as TextEventViewHolder){
                        itemView.tag = it
                        bind(it)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            R.layout.list_item_event_button ->{
                val binding: ListItemEventButtonBinding = DataBindingUtil.inflate(layoutInflater, R.layout.list_item_event_button, parent, false)
                ButtonEventViewHolder(binding)
            }
            else ->{
                val binding: ListItemEventTextBinding = DataBindingUtil.inflate(layoutInflater, R.layout.list_item_event_text, parent, false)
                TextEventViewHolder(binding)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val ev = mEvents[position].event
        return if (ev.eventType == TimelineEventType.RIDER_PASSED && ev.riderId == null) R.layout.list_item_event_button else R.layout.list_item_event_text
        //return mEvents[position].event.eventType.ordinal
    }

    override fun getItemCount(): Int {
        return mEvents.count()
    }
}