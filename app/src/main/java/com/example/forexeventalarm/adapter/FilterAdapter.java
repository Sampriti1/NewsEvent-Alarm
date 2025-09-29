package com.example.forexeventalarm.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.forexeventalarm.R;

import java.util.List;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterViewHolder> {

    public interface OnFilterSelectedListener {
        void onFilterSelected(String filter);
    }

    private final Context context;
    private final List<String> filterOptions;
    private final OnFilterSelectedListener listener;

    public FilterAdapter(Context context, List<String> filterOptions, OnFilterSelectedListener listener) {
        this.context = context;
        this.filterOptions = filterOptions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_filter_option, parent, false);
        return new FilterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilterViewHolder holder, int position) {
        String option = filterOptions.get(position);
        holder.filterText.setText(option);

        // Dot color based on impact
        switch (option.toLowerCase()) {
            case "high":
                holder.dot.setBackgroundColor(Color.RED);
                break;
            case "medium":
                holder.dot.setBackgroundColor(Color.parseColor("#FFA500")); // Orange
                break;
            case "low":
                holder.dot.setBackgroundColor(Color.YELLOW);
                break;
            case "holiday":
                holder.dot.setBackgroundColor(Color.GRAY);
                break;
            default:
                holder.dot.setBackgroundColor(Color.WHITE);
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFilterSelected(option);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filterOptions.size();
    }

    static class FilterViewHolder extends RecyclerView.ViewHolder {
        TextView filterText;
        View dot;

        public FilterViewHolder(@NonNull View itemView) {
            super(itemView);
            filterText = itemView.findViewById(R.id.filterText);
            dot = itemView.findViewById(R.id.filterDot);
        }
    }
}
