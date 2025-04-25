package com.example.officialtsr.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.content.res.Configuration;
import android.widget.LinearLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.officialtsr.R;
import com.example.officialtsr.adapters.TrafficSignAdapter;
import com.example.officialtsr.api.RetrofitClient;
import com.example.officialtsr.api.TrafficSignApiService;
import com.example.officialtsr.models.TrafficSign;
import com.example.officialtsr.utils.ImageCompressor;
import com.example.officialtsr.views.CameraPreview;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CameraActivity extends AppCompatActivity {

    private static final String TAG = "CameraActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int FRAME_INTERVAL = 5000; // 5 seconds
    private static final int REFRESH_INTERVAL = 5000; // 5 seconds

    private Camera camera;
    private CameraPreview cameraPreview;
    private Handler frameHandler = new Handler(Looper.getMainLooper());
    private Runnable frameCaptureRunnable;
    private Button sendButton;
    private RecyclerView resultRecyclerView;
    private TrafficSignAdapter trafficSignAdapter;
    private List<TrafficSign> trafficSigns = new ArrayList<>();
    private Handler refreshHandler = new Handler(Looper.getMainLooper());
    private Runnable refreshRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        resultRecyclerView = findViewById(R.id.result_list);
        resultRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        trafficSignAdapter = new TrafficSignAdapter(this, trafficSigns, trafficSign -> {
            // Handle item click if needed
            Toast.makeText(this, "Selected: " + trafficSign.getSignName(), Toast.LENGTH_SHORT).show();
        });
        resultRecyclerView.setAdapter(trafficSignAdapter);

        sendButton = findViewById(R.id.btn_send_frame);
        sendButton.setOnClickListener(v -> captureAndSendFrame());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return;
        }

        camera = getCameraInstance();
        if (camera != null) {
            setCameraDisplayOrientation();
            cameraPreview = new CameraPreview(this, camera);
            FrameLayout preview = findViewById(R.id.camera_preview);
            preview.addView(cameraPreview);

            startFrameCapture();
        } else {
            Toast.makeText(this, "Failed to access camera", Toast.LENGTH_SHORT).show();
            finish();
        }

        startAutoRefresh(); // Start auto-refresh
    }

    private Camera getCameraInstance() {
        try {
            return Camera.open();
        } catch (Exception e) {
            Log.e(TAG, "Camera is not available: " + e.getMessage(), e);
            return null;
        }
    }

    private void setCameraDisplayOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // Compensate for mirror effect
        } else { // Back-facing camera
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    private void startFrameCapture() {
        frameCaptureRunnable = new Runnable() {
            @Override
            public void run() {
                if (camera != null) {
                    camera.setOneShotPreviewCallback((data, camera) -> {
                        try {
                            Camera.Parameters parameters = camera.getParameters();
                            Camera.Size size = parameters.getPreviewSize();
                            YuvImage yuvImage = new YuvImage(data, parameters.getPreviewFormat(), size.width, size.height, null);
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            yuvImage.compressToJpeg(new Rect(0, 0, size.width, size.height), 100, out);
                            byte[] jpegData = out.toByteArray();

                            // Write data to a temporary file
                            File tempFile = File.createTempFile("frame", ".jpg", getCacheDir());
                            FileOutputStream fos = new FileOutputStream(tempFile);
                            fos.write(jpegData);
                            fos.close();

                            Log.d(TAG, "Temp file created: " + tempFile.exists() + " at " + tempFile.getAbsolutePath());

                            // Compress the image
                            File compressedImage = ImageCompressor.compressImage(CameraActivity.this, Uri.fromFile(tempFile), 0.8f, 800);

                            Log.d(TAG, "Compressed file exists: " + (compressedImage != null && compressedImage.exists()));

                            // Send the image to the server
                            sendFrameToServer(compressedImage);
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing frame: " + e.getMessage(), e);
                        }
                    });
                } else {
                    Log.e(TAG, "Camera is null. Unable to capture frame.");
                }
                frameHandler.postDelayed(this, FRAME_INTERVAL);
            }
        };
        frameHandler.post(frameCaptureRunnable);
    }

    private void captureAndSendFrame() {
        if (camera != null) {
            camera.setOneShotPreviewCallback((data, camera) -> {
                try {
                    Log.d(TAG, "Frame captured from camera (manual capture)."); // Log for manual capture
                    Camera.Parameters parameters = camera.getParameters();
                    Camera.Size size = parameters.getPreviewSize();
                    YuvImage yuvImage = new YuvImage(data, parameters.getPreviewFormat(), size.width, size.height, null);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    yuvImage.compressToJpeg(new Rect(0, 0, size.width, size.height), 100, out);
                    byte[] jpegData = out.toByteArray();

                    // Create a temporary file for the frame
                    File tempFile = File.createTempFile("frame", ".jpg", getCacheDir());
                    FileOutputStream fos = new FileOutputStream(tempFile);
                    fos.write(jpegData);
                    fos.close();

                    // Compress the image
                    File compressedImage = ImageCompressor.compressImage(CameraActivity.this, Uri.fromFile(tempFile), 0.8f, 800);

                    // Send the image to the server
                    sendFrameToServer(compressedImage);
                } catch (Exception e) {
                    Log.e(TAG, "Error capturing and sending frame: " + e.getMessage(), e);
                    Toast.makeText(CameraActivity.this, "Failed to process frame: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e(TAG, "Camera is null. Unable to capture frame (manual capture)."); // Log if camera is null
        }
    }

    private void sendFrameToServer(File imageFile) {
        TrafficSignApiService apiService = RetrofitClient.getInstance().create(TrafficSignApiService.class);

        RequestBody fileBody = RequestBody.create(imageFile, MediaType.parse("image/jpeg"));
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", imageFile.getName(), fileBody);

        apiService.detectAndClassifySimple(part).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        Log.d(TAG, "Frame sent successfully: " + responseBody);
                        handleApiResponse(responseBody);
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading API response: " + e.getMessage(), e);
                    }
                } else {
                    Log.e(TAG, "Failed to send frame: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Error sending frame: " + t.getMessage(), t);
            }
        });
    }

    private void handleApiResponse(String responseBody) {
        try {
            JSONObject jsonResponse = new JSONObject(responseBody);
            JSONArray classificationResults = jsonResponse.optJSONArray("classification_results");
            List<String> detectedLabels = new ArrayList<>();
            if (classificationResults != null && classificationResults.length() > 0) {
                
                for (int i = 0; i < classificationResults.length(); i++) {
                    detectedLabels.add(classificationResults.getString(i)); // Collect all detected labels
                }
                fetchTrafficSignDetails(detectedLabels); // Pass all labels to fetchTrafficSignDetails
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, "No matching traffic signs found", Toast.LENGTH_SHORT).show();
                });
                detectedLabels.clear(); // Clear the list if no labels are detected
                fetchTrafficSignDetails(detectedLabels);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing API response: " + e.getMessage(), e);
        }
    }

    private void fetchTrafficSignDetails(List<String> labels) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        trafficSigns.clear(); // Always clear the list to ensure it refreshes
        trafficSignAdapter.notifyDataSetChanged(); // Notify adapter immediately to reflect the empty state

        if (labels.isEmpty()) {
            Log.d(TAG, "No labels detected by the API.");
            return; // No labels to query, exit early
        }

        db.collection("TrafficSign")
            .whereIn("LABEL", labels) // Query for all matching labels
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String name = document.getString("SIGN_NAME");
                        String description = document.getString("DESCRIPTION");
                        String imageLink = document.getString("IMAGE_LINK");
                        String lawId = document.getString("LAW_ID");
                        String label = document.getString("LABEL");

                        trafficSigns.add(new TrafficSign(
                            name,
                            description,
                            imageLink,
                            lawId,
                            name,
                            null,
                            label
                        ));
                    }
                } else {
                    Log.d(TAG, "No matching traffic signs found in Firestore.");
                }
                runOnUiThread(() -> trafficSignAdapter.notifyDataSetChanged()); // Refresh the adapter
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error fetching traffic sign details: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    trafficSigns.clear(); // Clear the list on failure
                    trafficSignAdapter.notifyDataSetChanged(); // Refresh the adapter
                });
            })
            .addOnCompleteListener(task -> {
                // Ensure the list is refreshed every 3 seconds regardless of success or failure
                refreshHandler.postDelayed(() -> captureAndSendFrame(), REFRESH_INTERVAL);
            });
    }

    private void adjustLayoutForOrientation(int orientation) {
        LinearLayout rootLayout = findViewById(R.id.main_layout);
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            rootLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams previewParams = (LinearLayout.LayoutParams) findViewById(R.id.camera_preview).getLayoutParams();
            previewParams.weight = 1;
            LinearLayout.LayoutParams resultParams = (LinearLayout.LayoutParams) findViewById(R.id.result_list).getLayoutParams();
            resultParams.weight = 1;
        } else {
            rootLayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams previewParams = (LinearLayout.LayoutParams) findViewById(R.id.camera_preview).getLayoutParams();
            previewParams.weight = 1;
            LinearLayout.LayoutParams resultParams = (LinearLayout.LayoutParams) findViewById(R.id.result_list).getLayoutParams();
            resultParams.weight = 1;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        adjustLayoutForOrientation(newConfig.orientation);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camera != null) {
            camera.release();
            camera = null;
        }
        frameHandler.removeCallbacks(frameCaptureRunnable);
        stopAutoRefresh(); // Stop auto-refresh when the activity is paused
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutoRefresh(); // Ensure auto-refresh is stopped when the activity is destroyed
    }

    private void startAutoRefresh() {
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                captureAndSendFrame(); // Capture and send frame to the server
                refreshHandler.postDelayed(this, REFRESH_INTERVAL); // Schedule the next refresh
            }
        };
        refreshHandler.post(refreshRunnable);
    }

    private void stopAutoRefresh() {
        refreshHandler.removeCallbacks(refreshRunnable); // Stop the refresh handler
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                camera = getCameraInstance();
                if (camera != null) {
                    setCameraDisplayOrientation();
                    cameraPreview = new CameraPreview(this, camera);
                    FrameLayout preview = findViewById(R.id.camera_preview);
                    preview.addView(cameraPreview);

                    startFrameCapture();
                } else {
                    Toast.makeText(this, "Failed to access camera", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
