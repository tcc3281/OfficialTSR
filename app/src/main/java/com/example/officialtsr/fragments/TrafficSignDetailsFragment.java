package com.example.officialtsr.fragments;

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
import com.example.officialtsr.R;

public class TrafficSignDetailsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_traffic_sign_details, container, false);

        ImageView imageView = view.findViewById(R.id.details_image);
        TextView lawIdTextView = view.findViewById(R.id.details_law_id);
        TextView signNameTextView = view.findViewById(R.id.details_sign_name);
        TextView typeTextView = view.findViewById(R.id.details_type);
        TextView descriptionTextView = view.findViewById(R.id.details_description);

        if (getArguments() != null) {
            String imageLink = getArguments().getString("imageLink");
            String lawId = getArguments().getString("lawId");
            String signName = getArguments().getString("signName");
            String type = getArguments().getString("type");
            String description = getArguments().getString("description");

            Glide.with(requireContext()).load(imageLink).into(imageView);
            lawIdTextView.setText(lawId);
            signNameTextView.setText(signName);
            typeTextView.setText(type);
            descriptionTextView.setText(description);
        }

        return view;
    }
}
