package com.example.officialtsr.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.officialtsr.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationCameraFragment extends Fragment {

    private static final String TAG = "NotificationCamera";
    private static final int DISPLAY_DURATION = 3000; // 3 seconds
    private CameraDevice cameraDevice;
    private ImageView signImageView;
    private TextView signNameTextView;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private List<QueryDocumentSnapshot> trafficSigns = new ArrayList<>();
    private int currentIndex = 0;
    private String testString; // Variable to store the test string

    public void setTestString(String testString) {
        this.testString = testString;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification_camera, container, false);

        TextView textView = view.findViewById(R.id.notification_camera_text);
        textView.setText("CAMERA ĐANG BẬT");
        textView.setTextSize(24); // Set large text size for visibility
        textView.setTextColor(requireContext().getResources().getColor(android.R.color.holo_red_dark));

        signImageView = view.findViewById(R.id.notification_sign_image);
        signNameTextView = view.findViewById(R.id.notification_sign_name);

        openCamera();
        // check is camera open
        if (cameraDevice != null) {
            Log.d(TAG, "Camera is open - 59");
        } else {
            Log.d(TAG, "Camera is not open - 61");
        }

        if (testString != null) {
            Toast.makeText(requireContext(), "Test API Result: " + testString, Toast.LENGTH_LONG).show();
        }

        return view;
    }

    private void openCamera() {
        CameraManager cameraManager = (CameraManager) requireContext().getSystemService(getContext().CAMERA_SERVICE);
        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Camera permission not granted - 72");
                return;
            }
            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    Log.d(TAG, "Camera opened - 79");
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    camera.close();
                    cameraDevice = null;
                    Log.d(TAG, "Camera disconnected - 86");
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    camera.close();
                    cameraDevice = null;
                    Log.e(TAG, "93 - Camera error: " + error);
                }
            }, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "97 - Failed to access camera", e);
        }
    }

    private void fetchTrafficSignDetails() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("TrafficSign")
            .orderBy("SIGN_NAME", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    trafficSigns.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        if (document instanceof QueryDocumentSnapshot) {
                            trafficSigns.add((QueryDocumentSnapshot) document);
                        }
                    }
                    displayNextTrafficSign();
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to fetch traffic sign details: " + e.getMessage(), e);
            });
    }

    private void displayNextTrafficSign() {
        if (trafficSigns.isEmpty()) return;

        QueryDocumentSnapshot document = trafficSigns.get(currentIndex);
        String imageLink = document.getString("IMAGE_LINK");
        String signName = document.getString("SIGN_NAME");

        // Update UI with the current traffic sign
        Glide.with(requireContext()).load(imageLink).into(signImageView);
        signNameTextView.setText(signName);

        // Schedule the next traffic sign to be displayed
        currentIndex = (currentIndex + 1) % trafficSigns.size();
        handler.postDelayed(this::displayNextTrafficSign, DISPLAY_DURATION);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null); // Stop the handler when the view is destroyed
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
            Log.d(TAG, "Camera closed - 145");
        }
    }
}
