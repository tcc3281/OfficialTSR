package com.example.officialtsr;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class TrafficSignAdapter extends RecyclerView.Adapter<TrafficSignAdapter.TrafficSignViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(TrafficSign trafficSign);
    }

    private final Context context;
    private final List<TrafficSign> trafficSigns;
    private final OnItemClickListener onItemClickListener;

    public TrafficSignAdapter(Context context, List<TrafficSign> trafficSigns, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.trafficSigns = trafficSigns;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public TrafficSignViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_traffic_sign, parent, false);
        return new TrafficSignViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrafficSignViewHolder holder, int position) {
        TrafficSign trafficSign = trafficSigns.get(position);
        holder.lawIdTextView.setText(trafficSign.getLawId());
        holder.signNameTextView.setText(trafficSign.getSignName());
        Glide.with(context).load(trafficSign.getImageLink()).into(holder.imageView);

        // Dynamically set text color for LAW_ID based on dark mode
        int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
        holder.lawIdTextView.setTextColor(isDarkMode ? Color.WHITE : Color.BLACK);

        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(trafficSign));
    }

    @Override
    public int getItemCount() {
        return trafficSigns.size();
    }

    static class TrafficSignViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView lawIdTextView;
        TextView signNameTextView;

        public TrafficSignViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.traffic_sign_image);
            lawIdTextView = itemView.findViewById(R.id.traffic_sign_law_id);
            signNameTextView = itemView.findViewById(R.id.traffic_sign_name);
        }
    }
}
