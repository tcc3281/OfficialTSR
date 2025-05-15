package com.example.officialtsr.adapters;

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
import com.example.officialtsr.R;
import com.example.officialtsr.models.TrafficSign;

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
    }    @Override
    public void onBindViewHolder(@NonNull TrafficSignViewHolder holder, int position) {
        TrafficSign trafficSign = trafficSigns.get(position);
        
        // Hiển thị mã luật và tên biển báo
        String lawId = trafficSign.getLawId();
        if (lawId != null) {
            holder.lawIdTextView.setText(lawId);
            holder.lawIdTextView.setVisibility(View.VISIBLE);
        } else {
            holder.lawIdTextView.setText("");
            holder.lawIdTextView.setVisibility(View.GONE);
        }
        
        String signName = trafficSign.getSignName();
        if (signName != null) {
            holder.signNameTextView.setText(signName);
        } else {
            holder.signNameTextView.setText(trafficSign.getLabel() != null ? 
                trafficSign.getLabel() : "Biển báo không xác định");
        }
        
        // Set color based on category
        String category = trafficSign.getType();
        int categoryColor = getCategoryColor(category);  // Phương thức đã được sửa để xử lý null
        holder.categoryIndicator.setBackgroundColor(categoryColor);
        holder.categoryIndicator.setVisibility(View.VISIBLE);
        
        // Load image with rounded corners using Glide
        String imageUrl = trafficSign.getImageLink();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.user) // Hình placeholder
                .error(R.drawable.user) // Hình lỗi
                .centerCrop()
                .into(holder.imageView);
        } else {
            // Nếu không có URL hình, dùng placeholder
            holder.imageView.setImageResource(R.drawable.user);
        }

        // Apply ripple effect when clicked
        holder.itemView.setOnClickListener(v -> {
            onItemClickListener.onItemClick(trafficSign);
        });
    }
    
    /**
     * Get color based on traffic sign category
     */    private int getCategoryColor(String category) {
        // Check if category is null to avoid NullPointerException
        if (category == null) {
            return context.getResources().getColor(R.color.accent); // Default color when type is null
        }
        
        // Return different colors based on category
        switch (category.toLowerCase()) {
            case "warning":
                return context.getResources().getColor(R.color.warning);
            case "prohibition":
                return context.getResources().getColor(R.color.error);
            case "mandatory":
                return context.getResources().getColor(R.color.primary);
            case "information":
                return context.getResources().getColor(R.color.info);
            default:
                return context.getResources().getColor(R.color.accent);
        }
    }

    @Override
    public int getItemCount() {
        return trafficSigns.size();
    }    static class TrafficSignViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView lawIdTextView;
        TextView signNameTextView;
        View categoryIndicator;

        public TrafficSignViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.traffic_sign_image);
            lawIdTextView = itemView.findViewById(R.id.traffic_sign_law_id);
            signNameTextView = itemView.findViewById(R.id.traffic_sign_name);
            categoryIndicator = itemView.findViewById(R.id.category_indicator);
        }
    }
}
