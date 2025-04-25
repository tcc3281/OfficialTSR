package com.example.officialtsr.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.officialtsr.R;
import com.example.officialtsr.utils.AuthManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.GoogleAuthProvider;

public class AccountActivity extends AppCompatActivity {
    private static final String TAG = "AccountActivity";

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private AuthManager authManager;
    private ProgressDialog progressDialog;

    private EditText emailInput;
    private EditText passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        mAuth = FirebaseAuth.getInstance();
        authManager = new AuthManager(this);

        if (authManager.isLoggedIn()) { // Check login state
            navigateToMainActivity();
            return;
        }

        setupGoogleSignIn();
        setupUI();
    }

    private void setupGoogleSignIn() {
        String clientId = getString(R.string.default_web_client_id);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientId)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupUI() {
        emailInput = findViewById(R.id.input_email);
        passwordInput = findViewById(R.id.input_password);
        com.google.android.gms.common.SignInButton googleSignInButton = findViewById(R.id.btn_google_sign_in);
        Button emailSignInButton = findViewById(R.id.btn_email_sign_in);
        Button emailRegisterButton = findViewById(R.id.btn_email_register);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang xử lý...");
        progressDialog.setCancelable(false);

        googleSignInButton.setOnClickListener(v -> signInWithGoogle());
        emailSignInButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            signInWithEmail(email, password);
        });
        emailRegisterButton.setOnClickListener(v -> navigateToRegistration());
    }

    private void signInWithGoogle() {
        progressDialog.show();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 100);
    }

    public void signInWithEmail(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        authManager.saveLoginState(true, null); // Save login state
                        navigateToMainActivity();
                    } else {
                        Exception exception = task.getException();
                        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                            showError("Thông tin đăng nhập không hợp lệ.");
                        } else {
                            showError("Đăng nhập thất bại: " + exception.getMessage());
                        }
                    }
                });
    }

    private void navigateToRegistration() {
        Toast.makeText(this, "Chức năng đăng ký chưa được triển khai", Toast.LENGTH_SHORT).show();
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (Exception e) {
                progressDialog.dismiss();
                showError("Đăng nhập Google thất bại: " + e.getMessage());
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        authManager.saveLoginState(true, null); // Save login state
                        navigateToMainActivity();
                    } else {
                        showError("Đăng nhập Google thất bại.");
                    }
                });
    }
}