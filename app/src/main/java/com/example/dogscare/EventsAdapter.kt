package com.example.dogscare

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventAdapter(
    private var events: List<EventDisplayModel>
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener){
        this.listener = listener
    }

    fun updateList(newList: List<EventDisplayModel>) {
        events = newList
        notifyDataSetChanged()
    }

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventTitle: TextView = itemView.findViewById(R.id.eventTitle)
        val eventDateRange: TextView = itemView.findViewById(R.id.eventDateRange)

        fun bind(event: EventDisplayModel){
            eventTitle.text = event.header
            val start = timestampToString(event.startDate)
            val end = timestampToString(event.endDate)
            if(event.startDate == event.endDate) eventDateRange.text = "$start"
            else eventDateRange.text = "$start - $end"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event_list, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.bind(event)

        holder.itemView.setOnClickListener {
            listener?.onItemClick(event)
        }
    }


    override fun getItemCount(): Int = events.size

    interface OnItemClickListener{
        fun onItemClick(event : EventDisplayModel)
    }
}
