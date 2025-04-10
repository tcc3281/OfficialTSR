package com.example.officialtsr.utils;

import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class AnalyticsHelper {
    private final FirebaseAnalytics firebaseAnalytics;

    public AnalyticsHelper(FirebaseAnalytics firebaseAnalytics) {
        this.firebaseAnalytics = firebaseAnalytics;
    }

    public void logLoginEvent(boolean success, String method) {
        Bundle bundle = new Bundle();
        bundle.putString("login_method", method);
        bundle.putBoolean("success", success);
        bundle.putLong("timestamp", System.currentTimeMillis());
        firebaseAnalytics.logEvent("google_login", bundle);
    }
}