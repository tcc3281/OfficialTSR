package com.example.officialtsr;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class AuthManager {
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_GOOGLE_ID_TOKEN = "googleIdToken";

    private final SharedPreferences securePrefs;

    public AuthManager(@NonNull Context context) {
        try {
            // Correctly get or create the master key for encryption
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);

            // Initialize EncryptedSharedPreferences
            securePrefs = EncryptedSharedPreferences.create(
                    "secure_auth_prefs",
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Failed to initialize secure preferences", e);
        }
    }

    public void saveLoginState(boolean isLoggedIn, String idToken) {
        securePrefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
                .putString(KEY_GOOGLE_ID_TOKEN, idToken)
                .apply();
    }

    public boolean isLoggedIn() {
        return securePrefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getIdToken() {
        return securePrefs.getString(KEY_GOOGLE_ID_TOKEN, null);
    }

    public void logout() {
        securePrefs.edit()
                .clear()
                .apply();
    }
}