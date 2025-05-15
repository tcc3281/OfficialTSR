package com.example.officialtsr.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.officialtsr.R;
import com.example.officialtsr.activities.CameraActivity;
import com.example.officialtsr.api.RetrofitClient;
import com.example.officialtsr.api.TrafficSignApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainFragment extends Fragment {

    @Nullable    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        
        try {
            Button btnCamera = view.findViewById(R.id.btn_camera);
            btnCamera.setOnClickListener(v -> openCamera());
        } catch (Exception e) {
            Log.e("MainFragment", "Error setting up camera button: " + e.getMessage());
            Toast.makeText(requireContext(), "Error loading main screen", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void openCamera() {
        Intent intent = new Intent(requireContext(), CameraActivity.class);
        startActivity(intent);
    }
}
