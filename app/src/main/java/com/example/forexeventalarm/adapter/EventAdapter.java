package com.example.forexeventalarm.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.forexeventalarm.R;
import com.example.forexeventalarm.model.Event;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final List<Event> events;

    public EventAdapter(List<Event> events) {
        this.events = events;
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
        Event event = events.get(position);
        holder.title.setText(event.getTitle());
        holder.date.setText(event.getDate());
        holder.time.setText(event.getTime());
        holder.impact.setText(event.getImpact());


        // Impact colors
        switch (event.getImpact().toLowerCase()) {
            case "high":
                holder.impactDot.getBackground().setTint(Color.RED);
                holder.impactBorder.setBackgroundColor(Color.RED);
                break;

            case "medium":
                holder.impactDot.getBackground().setTint(Color.parseColor("#FFA500")); // Orange
                holder.impactBorder.setBackgroundColor(Color.parseColor("#FFA500"));
                break;

            case "low":
                holder.impactDot.getBackground().setTint(Color.YELLOW);
                holder.impactBorder.setBackgroundColor(Color.YELLOW);
                break;

            case "holiday":
                holder.impactDot.getBackground().setTint(Color.GRAY);
                holder.impactBorder.setBackgroundColor(Color.GRAY);
                break;

            default:
                holder.impactDot.getBackground().setTint(Color.parseColor("#FFA500")); // Orange fallback
                holder.impactBorder.setBackgroundColor(Color.parseColor("#FFA500"));
                break;
        }

    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, time, impact;
        View impactDot,impactBorder;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.eventTitle);
            date = itemView.findViewById(R.id.eventDate);
            time = itemView.findViewById(R.id.eventTime);
            impact = itemView.findViewById(R.id.eventImpact);
            impactDot = itemView.findViewById(R.id.impactDot);
            impactBorder = itemView.findViewById(R.id.impactBorder);
        }
    }
}
