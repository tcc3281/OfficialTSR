package com.example.officialtsr.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.officialtsr.R;
import com.example.officialtsr.models.TrafficSign;

import java.util.List;

public class BottomListAdapter extends RecyclerView.Adapter<BottomListAdapter.BottomListViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(TrafficSign trafficSign);
    }

    private final Context context;
    private final List<TrafficSign> trafficSigns;
    private final OnItemClickListener onItemClickListener;

    public BottomListAdapter(Context context, List<TrafficSign> trafficSigns, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.trafficSigns = trafficSigns;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public BottomListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_traffic_sign, parent, false);
        return new BottomListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BottomListViewHolder holder, int position) {
        TrafficSign trafficSign = trafficSigns.get(position);
        holder.lawIdTextView.setText(trafficSign.getLawId());
        holder.signNameTextView.setText(trafficSign.getSignName());
        Glide.with(context).load(trafficSign.getImageLink()).into(holder.imageView);

        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(trafficSign));
    }

    @Override
    public int getItemCount() {
        return trafficSigns.size();
    }

    static class BottomListViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView lawIdTextView;
        TextView signNameTextView;

        public BottomListViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.traffic_sign_image);
            lawIdTextView = itemView.findViewById(R.id.traffic_sign_law_id);
            signNameTextView = itemView.findViewById(R.id.traffic_sign_name);
        }
    }
}
