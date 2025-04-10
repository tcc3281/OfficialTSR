package com.example.officialtsr;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class AccountActivity extends AppCompatActivity {
    private static final String TAG = "GoogleSignIn";
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private AuthManager authManager;
    private ProgressDialog progressDialog;
    private AnalyticsHelper analyticsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        // Kiểm tra đăng nhập tự động
        authManager = new AuthManager(this);
        if (authManager.isLoggedIn()) {
            navigateToMainActivity();
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        setupGoogleSignIn();
        setupUI();
        analyticsHelper = new AnalyticsHelper(FirebaseAnalytics.getInstance(this));
    }

    private void setupGoogleSignIn() {
        String clientId;
        try {
            clientId = getString(R.string.default_web_client_id);
        } catch (Exception e) {
            clientId = null; // Fallback if the resource is missing
        }

        if (clientId == null || clientId.isEmpty()) {
            Log.w(TAG, "default_web_client_id is missing. Using default configuration for first login.");
            clientId = "DEFAULT_CLIENT_ID"; // Replace with a placeholder or default value
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientId)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupUI() {
        SignInButton btnSignIn = findViewById(R.id.btn_google_sign_in);
        btnSignIn.setOnClickListener(v -> signInWithGoogle());



        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang đăng nhập...");
        progressDialog.setCancelable(false);
    }

    private void logout() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            authManager.logout(); // Clear saved login state
            Toast.makeText(this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
            logLoginEvent(false, "Google");
        });
    }

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    handleSignInResult(result.getData());
                } else {
                    handleSignInCancellation(result.getResultCode());
                }
            });

    private void handleSignInResult(Intent data) {
        progressDialog.show();
        try {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null) {
                Log.d(TAG, "Google Sign-In successful. Account: " + account.getEmail());
                firebaseAuthWithGoogle(account.getIdToken());
            } else {
                Log.e(TAG, "Google Sign-In failed: Account is null.");
                showError("Lỗi đăng nhập: Tài khoản không hợp lệ", null);
            }
        } catch (ApiException e) {
            progressDialog.dismiss();
            Log.e(TAG, "Google Sign-In error: " + e.getStatusCode(), e);
            showError("Lỗi đăng nhập: " + e.getStatusCode(), e);
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        if (idToken == null || idToken.isEmpty()) {
            Log.e(TAG, "ID Token is null or empty.");
            showError("Lỗi xác thực: ID Token không hợp lệ", null);
            return;
        }

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Firebase Authentication successful.");
                        handleLoginSuccess(idToken);
                    } else {
                        Log.e(TAG, "Firebase Authentication failed.", task.getException());
                        handleLoginFailure(task.getException());
                    }
                });
    }

    private void handleLoginSuccess(String idToken) {
        // Save the default_web_client_id after successful login
        String clientId = getString(R.string.default_web_client_id);
        if (clientId == null || clientId.isEmpty()) {
            Log.d(TAG, "Saving default_web_client_id after first login.");
            saveDefaultWebClientId(idToken); // Save the client ID dynamically
        }

        authManager.saveLoginState(true, idToken);
        Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
        logLoginEvent(true, "Google");
        navigateToMainActivity();
    }

    private void saveDefaultWebClientId(String idToken) {
        // Logic to save the client ID dynamically (if applicable)
        // This could involve saving it to SharedPreferences or another secure location
        Log.d(TAG, "Default Web Client ID saved: " + idToken);
    }

    private void navigateToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void handleLoginFailure(Exception exception) {
        String errorMsg = exception != null ? exception.getMessage() : "Lỗi không xác định";
        showError("Đăng nhập thất bại: " + errorMsg, exception);
    }

    private void handleSignInCancellation(int resultCode) {
        if (resultCode == Activity.RESULT_CANCELED) {
            Log.w(TAG, "Đăng nhập bị hủy bởi người dùng. Kiểm tra xem intent có được xử lý đúng không.");
            Toast.makeText(this, "Đăng nhập bị hủy. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
        } else {
            Log.e(TAG, "Đăng nhập thất bại với mã: " + resultCode);
            showError("Đăng nhập thất bại với mã: " + resultCode, null);
        }
    }

    private void showError(String message, Exception e) {
        if (e != null) {
            Log.e(TAG, message, e);
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        super.onDestroy();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void signInWithGoogle() {
        if (!isNetworkAvailable()) {
            Log.e(TAG, "No network connection.");
            showError("Không có kết nối mạng. Vui lòng kiểm tra lại", null);
            return;
        }
        Log.d(TAG, "Starting Google Sign-In intent.");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void logLoginEvent(boolean success, String method) {
        analyticsHelper.logLoginEvent(success, method);
    }
}