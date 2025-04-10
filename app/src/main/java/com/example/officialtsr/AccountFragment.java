package com.example.officialtsr;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

public class AccountFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        ImageView profileImage = view.findViewById(R.id.account_profile_image);
        TextView fullNameTextView = view.findViewById(R.id.account_full_name);
        TextView emailTextView = view.findViewById(R.id.account_email);

        if (getArguments() != null) {
            String fullName = getArguments().getString("fullName", "N/A");
            String email = getArguments().getString("email", "N/A");
            String photoUrl = getArguments().getString("photoUrl");

            fullNameTextView.setText(fullName);
            emailTextView.setText(email);

            if (photoUrl != null) {
                Glide.with(requireContext()).load(photoUrl).into(profileImage);
            }
        }

        return view;
    }
}
