package com.example.officialtsr.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.officialtsr.utils.AuthManager;
import com.example.officialtsr.fragments.ListFragment;
import com.example.officialtsr.fragments.MainFragment;
import com.example.officialtsr.R;
import com.example.officialtsr.fragments.SettingsFragment;
import com.example.officialtsr.models.TrafficSign;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private GoogleSignInClient mGoogleSignInClient;
    private AuthManager authManager;
    private List<TrafficSign> cachedTrafficSigns = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MainActivity", "onCreate called"); // Log để kiểm tra

        // Apply Dark Mode based on saved preferences
        SharedPreferences preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean isDarkMode = preferences.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
            isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        authManager = new AuthManager(this);
        mGoogleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId(); // Explicitly get the item ID
            if (itemId == R.id.nav_main) {
                selectedFragment = new MainFragment();
            } else if (itemId == R.id.nav_list) {
                selectedFragment = new ListFragment();
            } else if (itemId == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
            }
            return true;
        });

        // Set default fragment
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_main);
        }

        loadTrafficSigns(); // Load traffic signs once
    }

    private void loadTrafficSigns() {
        if (!cachedTrafficSigns.isEmpty()) return; // Skip if already loaded

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("TrafficSign")
            .orderBy("SIGN_NAME", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                cachedTrafficSigns.clear();
                queryDocumentSnapshots.forEach(document -> {
                    String imageLink = document.getString("IMAGE_LINK");
                    String lawId = document.getString("LAW_ID");
                    String signName = document.getString("SIGN_NAME");
                    String type = document.getString("TYPE");
                    String description = document.getString("DESCRIPTION");
                    String label = document.getString("LABEL"); // Fetch label field

                    if (imageLink != null && lawId != null && signName != null && type != null) {
                        cachedTrafficSigns.add(new TrafficSign(
                            null,
                            description != null ? description : "No description available",
                            imageLink,
                            lawId,
                            signName,
                            type,
                            label // Pass label field
                        ));
                    }
                });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to load traffic signs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    public List<TrafficSign> getCachedTrafficSigns() {
        return cachedTrafficSigns;
    }

    public void saveTrafficSigns(List<TrafficSign> trafficSigns) {
        cachedTrafficSigns.clear();
        cachedTrafficSigns.addAll(trafficSigns);
    }

    private void logout() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            authManager.logout(); // Clear saved login state
            FirebaseAuth.getInstance().signOut(); // Sign out from Firebase
            Toast.makeText(this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, AccountActivity.class)); // Redirect to login screen
            finish();
        });
    }
}