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

import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final List<Event> originalList;   // Keep all events
    private List<Event> filteredList;         // Active (filtered) list

    public EventAdapter(List<Event> events) {
        this.originalList = new ArrayList<>(events);
        this.filteredList = new ArrayList<>(events);
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
        Event event = filteredList.get(position);

        holder.title.setText(event.getTitle());
        holder.date.setText(event.getDate());
        holder.time.setText(event.getTime());
        holder.impact.setText(event.getImpact());
        holder.currency.setText(event.getCurrency());

        // --- Impact colors ---
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
        return filteredList.size();
    }

    // --- Filtering logic ---
    public void filterByImpact(String impact) {
        if (impact.equalsIgnoreCase("All")) {
            filteredList = new ArrayList<>(originalList);
        } else {
            List<Event> temp = new ArrayList<>();
            for (Event e : originalList) {
                if (e.getImpact().equalsIgnoreCase(impact)) {
                    temp.add(e);
                }
            }
            filteredList = temp;
        }
        notifyDataSetChanged();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, time, impact,currency;
        View impactDot, impactBorder;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.eventTitle);
            date = itemView.findViewById(R.id.eventDate);
            time = itemView.findViewById(R.id.eventTime);
            impact = itemView.findViewById(R.id.eventImpact);
            impactDot = itemView.findViewById(R.id.impactDot);
            impactBorder = itemView.findViewById(R.id.impactBorder);
            currency=itemView.findViewById(R.id.tv_currency);


        }
    }
}
