package com.example.officialtsr.fragments;

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
        TrafficSignApiService apiService = RetrofitClient.getInstance().create(TrafficSignApiService.class);
        apiService.getTestString().enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String testString = response.body();

                    NotificationCameraFragment fragment = new NotificationCameraFragment();
                    fragment.setTestString(testString);

                    requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch test string", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(requireContext(), "API call failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
