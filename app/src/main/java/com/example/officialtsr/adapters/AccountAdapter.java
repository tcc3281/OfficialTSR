package com.example.officialtsr.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position == 0) { // Account section
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_account, parent, false);
            }

            ImageView accountImage = convertView.findViewById(R.id.account_image);
            TextView accountId = convertView.findViewById(R.id.account_id);

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                accountId.setText(user.getEmail()); // Display user email as ID
                // Load profile picture if available
                // Use a library like Glide or Picasso for loading images
                // Glide.with(context).load(user.getPhotoUrl()).into(accountImage);
            } else {
                accountId.setText("Guest");
            }

            // Dynamically set text color based on dark mode
            SharedPreferences preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
            boolean isDarkMode = preferences.getBoolean("dark_mode", false);
            accountId.setTextColor(context.getResources().getColor(isDarkMode ? android.R.color.white : android.R.color.black));
        } else { // Other sections
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
            }

            TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
            textView.setText(options[position]);
        }

        return convertView;
    }
}
