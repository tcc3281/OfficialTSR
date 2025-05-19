package com.example.officialtsr.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import com.example.officialtsr.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountAdapter extends BaseAdapter {

    private final Context context;
    private final String[] options;

    public AccountAdapter(Context context, String[] options) {
        this.context = context;
        this.options = options;
    }

    @Override
    public int getCount() {
        return options.length;
    }

    @Override
    public Object getItem(int position) {
        return options[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position == 0) { // Account section
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_account, parent, false);
            }

            ImageView accountImage = convertView.findViewById(R.id.account_image);
            TextView accountId = convertView.findViewById(R.id.account_id);
            TextView accountName = convertView.findViewById(R.id.account_name);
            
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                // Set email as account ID
                accountId.setText(user.getEmail());
                
                // Set display name or default to email username part
                String displayName = user.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    accountName.setText(displayName);
                } else {
                    // Extract username from email (before @)
                    String email = user.getEmail();
                    if (email != null && email.contains("@")) {
                        accountName.setText(email.substring(0, email.indexOf('@')));
                    } else {
                        accountName.setText("User");
                    }
                }
                  // Load profile picture if available using Glide
                if (user.getPhotoUrl() != null) {
                    Glide.with(context)
                        .load(user.getPhotoUrl())
                        .circleCrop()
                        .placeholder(R.drawable.user)
                        .into(accountImage);
                }
            } else {
                accountId.setText("Guest");
                accountName.setText("Guest User");
                // Set default avatar
                accountImage.setImageResource(R.drawable.user);
            }
            
        } else { // Other sections - now using Material Card with icon and description
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_setting, parent, false);
            }

            TextView titleTextView = convertView.findViewById(R.id.setting_title);
            TextView descriptionTextView = convertView.findViewById(R.id.setting_description);
            ImageView iconView = convertView.findViewById(R.id.setting_icon);
            
            titleTextView.setText(options[position]);
            
            // Set icon and description based on the option
            switch (position) {
                case 1: // Edit Profile
                    iconView.setImageResource(android.R.drawable.ic_menu_edit);
                    descriptionTextView.setText("Chỉnh sửa thông tin cá nhân");
                    break;
                case 2: // Change Password
                    iconView.setImageResource(android.R.drawable.ic_lock_lock);
                    descriptionTextView.setText("Thay đổi mật khẩu đăng nhập");
                    break;
                case 3: // Account Settings
                    iconView.setImageResource(android.R.drawable.ic_menu_preferences);
                    descriptionTextView.setText("Cài đặt tài khoản");
                    break;
                default:
                    iconView.setImageResource(android.R.drawable.ic_menu_manage);
                    descriptionTextView.setText("Tùy chọn người dùng");
            }
        }

        return convertView;
    }
}
