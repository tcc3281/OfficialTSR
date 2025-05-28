package com.example.officialtsr.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.officialtsr.R;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder> {

    private final String[] options;
    private final OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }    public SettingsAdapter(String[] options, OnItemClickListener onItemClickListener) {
        this.options = options;
        this.onItemClickListener = onItemClickListener;
    }
    
    @NonNull
    @Override    public SettingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_setting, parent, false);
        return new SettingsViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull SettingsViewHolder holder, int position) {
        String option = options[position];
        holder.titleTextView.setText(option);
        
        // Set appropriate icon and description based on setting option
        switch (position) {            case 0: // Account
                holder.iconView.setImageResource(android.R.drawable.ic_menu_myplaces);
                holder.descriptionTextView.setText("Quản lý tài khoản người dùng");
                break;
            case 1: // Dark mode
                holder.iconView.setImageResource(android.R.drawable.ic_menu_view);
                holder.descriptionTextView.setText("Chuyển đổi chế độ tối");
                break;
            case 2: // Logout
                holder.iconView.setImageResource(android.R.drawable.ic_lock_power_off);
                holder.descriptionTextView.setText("Đăng xuất khỏi tài khoản");
                break;
            default:
                holder.iconView.setImageResource(android.R.drawable.ic_menu_manage);
                holder.descriptionTextView.setText("Cài đặt hệ thống");
        }
        
        // Add ripple effect
        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(position));
    }

    @Override
    public int getItemCount() {
        return options.length;
    }

    static class SettingsViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView descriptionTextView;
        ImageView iconView;
        ImageView actionView;

        public SettingsViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.setting_title);
            descriptionTextView = itemView.findViewById(R.id.setting_description);
            iconView = itemView.findViewById(R.id.setting_icon);
            actionView = itemView.findViewById(R.id.setting_action);
        }
    }
}
