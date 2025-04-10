package com.example.officialtsr;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MainFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        Button btnCamera = view.findViewById(R.id.btn_camera);
        Button btnNotification = view.findViewById(R.id.btn_notification);

        btnCamera.setOnClickListener(v -> openCamera());
        btnNotification.setOnClickListener(v -> openNotification());

        return view;
    }

    private void openCamera() {
        Intent intent = new Intent(requireContext(), CameraActivity.class);
        startActivity(intent);
    }

    private void openNotification() {
        // Placeholder for notification functionality
        Toast.makeText(requireContext(), "Notification button clicked", Toast.LENGTH_SHORT).show();
        // Add intent to open notification activity if needed
    }
}
