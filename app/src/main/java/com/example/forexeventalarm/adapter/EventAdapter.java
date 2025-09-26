package com.example.forexeventalarm.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.forexeventalarm.R;
import com.example.forexeventalarm.model.NewsEvents;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<NewsEvents> eventList;

    public EventAdapter(List<NewsEvents> eventList) {
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        NewsEvents event = eventList.get(position);
        holder.eventTitle.setText(event.getTitle());
        holder.eventDate.setText(event.getDate());
        holder.eventTime.setText(event.getTime());
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventTitle, eventDate, eventTime;

        public EventViewHolder(View itemView) {
            super(itemView);
            eventTitle = itemView.findViewById(R.id.eventTitle);
            eventDate = itemView.findViewById(R.id.eventDate);
            eventTime = itemView.findViewById(R.id.eventTime);
        }
    }

}


