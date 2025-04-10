package com.example.officialtsr;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private GoogleSignInClient mGoogleSignInClient;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply Dark Mode based on saved preferences
        SharedPreferences preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean isDarkMode = preferences.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
            isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Update the bell icon based on the current theme
        updateNotificationIcon(isDarkMode);

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
    }

    public void updateNotificationIcon(boolean isDarkMode) {
        ImageView notificationIcon = findViewById(R.id.notification_icon);
        if (notificationIcon != null) {
            // Reverse the logic: use black for dark mode and white for light mode
            notificationIcon.setImageResource(isDarkMode ? R.drawable.ic_bell_black : R.drawable.ic_bell_white);
        }
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