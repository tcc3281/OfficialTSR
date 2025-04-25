package com.example.officialtsr.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.officialtsr.utils.AuthManager;
import com.example.officialtsr.R;
import com.example.officialtsr.activities.AccountActivity;
import com.example.officialtsr.adapters.SettingsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsFragment extends Fragment {

    private RecyclerView settingsRecyclerView;

    private static final String[] settingsOptions = {
        "Account",
        "Dark Mode",
        "Logout"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        settingsRecyclerView = view.findViewById(R.id.settings_recycler_view);

        setupSettingsRecyclerView();
    }

    private void setupSettingsRecyclerView() {
        settingsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        SettingsAdapter settingsAdapter = new SettingsAdapter(settingsOptions, this::handleSettingsClick);
        settingsRecyclerView.setAdapter(settingsAdapter);
    }

    private void handleSettingsClick(int position) {
        switch (position) {
            case 0: // Account
                navigateToAccountFragment();
                break;
            case 1: // Dark Mode
                toggleDarkMode();
                break;
            case 2: // Logout
                logout();
                break;
        }
    }

    private void toggleDarkMode() {
        SharedPreferences preferences = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean isDarkMode = preferences.getBoolean("dark_mode", false);

        // Toggle dark mode
        boolean newMode = !isDarkMode;
        preferences.edit().putBoolean("dark_mode", newMode).apply();
        AppCompatDelegate.setDefaultNightMode(newMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    private void logout() {
        // Clear login state and navigate to AccountActivity
        AuthManager authManager = new AuthManager(requireContext());
        authManager.logout();

        Intent intent = new Intent(requireContext(), AccountActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void navigateToAccountFragment() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();

            firestore.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("displayName");
                        String email = documentSnapshot.getString("email");
                        String photoUrl = documentSnapshot.getString("photoURL");

                        Bundle bundle = new Bundle();
                        bundle.putString("fullName", fullName != null ? fullName : "N/A");
                        bundle.putString("email", email != null ? email : "N/A");
                        bundle.putString("photoUrl", photoUrl);

                        AccountFragment accountFragment = new AccountFragment();
                        accountFragment.setArguments(bundle);

                        getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, accountFragment)
                            .addToBackStack(null)
                            .commit();
                    } else {
                        Toast.makeText(requireContext(), "Không tìm thấy thông tin tài khoản", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Lỗi khi lấy thông tin tài khoản: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        } else {
            Toast.makeText(requireContext(), "Không có thông tin tài khoản", Toast.LENGTH_SHORT).show();
        }
    }
}
