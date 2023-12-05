package com.jaredlinden.timingtrials.timing

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.jaredlinden.timingtrials.R
import com.jaredlinden.timingtrials.databinding.ListItemEventButtonBinding
import com.jaredlinden.timingtrials.databinding.ListItemEventTextBinding
import com.jaredlinden.timingtrials.domain.ITimelineEvent
import com.jaredlinden.timingtrials.domain.TimelineEventType
import com.jaredlinden.timingtrials.ui.EventViewWrapper

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

                when (eventWrapper.event.eventType) {
                    TimelineEventType.RIDER_PASSED -> {
                        text1.setTextColor(ContextCompat.getColor(context,R.color.secondaryColor))
                    }
                    TimelineEventType.RIDER_FINISHED -> {
                        text1.setTextColor(ContextCompat.getColor(context,R.color.primaryColor))
                    }
                    else -> {
                        text1.setTextColor(ContextCompat.getColor(context,R.color.material_on_surface_emphasis_medium))
                    }
                }
                val event = eventWrapper.event
                if(event.rider != null && event.eventType != TimelineEventType.RIDER_STARTED){
                    listItemEventTextContraint.setOnLongClickListener {
                        longClick(event)
                        true
                    }
                }else{
                    listItemEventTextContraint.setOnLongClickListener { false }
                }

                executePendingBindings()
            }

        }
    }

    var longClick = {_: ITimelineEvent -> }

    private var mEvents: List<EventViewWrapper> = listOf()
    private val layoutInflater = LayoutInflater.from(context)

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

    override fun getItemId(position: Int): Long {
        return mEvents[position].event.timeStamp
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
        return if (ev.eventType == TimelineEventType.RIDER_PASSED && ev.rider == null)
            R.layout.list_item_event_button
        else
            R.layout.list_item_event_text
    }

    override fun getItemCount(): Int {
        return mEvents.size
    }
}