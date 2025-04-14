package com.example.officialtsr.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthManager {
    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_ID_TOKEN = "id_token";

    private final SharedPreferences sharedPreferences;

    public AuthManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void saveLoginState(boolean isLoggedIn, String idToken) {
        sharedPreferences.edit()
                .putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
                .putString(KEY_ID_TOKEN, idToken != null ? idToken : "") // Lưu token nếu có
                .apply();
    }

    public String getIdToken() {
        return sharedPreferences.getString(KEY_ID_TOKEN, null);
    }

    public void logout() {
        sharedPreferences.edit()
                .clear()
                .apply();
    }
}