package com.example.officialtsr.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.officialtsr.R;
import com.example.officialtsr.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;

import android.app.DatePickerDialog;
import android.widget.DatePicker;
import android.content.Intent;
import com.example.officialtsr.activities.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

public class UserInfoFragment extends Fragment {

    private EditText displayNameInput;
    private EditText dateOfBirthInput;
    private EditText emailInput;
    private EditText passwordInput;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private final Calendar calendar = Calendar.getInstance();
    private boolean isRegistration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_info, container, false);

        displayNameInput = view.findViewById(R.id.input_display_name);
        dateOfBirthInput = view.findViewById(R.id.input_date_of_birth);
        emailInput = view.findViewById(R.id.input_email);
        passwordInput = view.findViewById(R.id.input_password);
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Nhận trạng thái "isRegistration" từ Bundle
        if (getArguments() != null) {
            isRegistration = getArguments().getBoolean("isRegistration", false);
        }

        dateOfBirthInput.setOnClickListener(v -> showDatePickerDialog());

        view.findViewById(R.id.btn_save_user_info).setOnClickListener(v -> {
            if (isRegistration) {
                registerUser();
            } else {
                saveUserInfo();
            }
        });

        return view;
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            requireContext(),
            (DatePicker view, int year, int month, int dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateOfBirthField();
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateOfBirthField() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        dateOfBirthInput.setText(dateFormat.format(calendar.getTime()));
    }

    private void registerUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String displayName = displayNameInput.getText().toString().trim();
        String dateOfBirth = dateOfBirthInput.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(displayName)) {
            Toast.makeText(getContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(getContext(), "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String uid = auth.getCurrentUser().getUid();
                    saveUserToFirestore(uid, email, displayName, dateOfBirth);
                } else {
                    Toast.makeText(getContext(), "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void saveUserToFirestore(String uid, String email, String displayName, String dateOfBirth) {
        User user = new User(
            uid,
            email,
            displayName,
            null, // Photo URL (optional)
            dateOfBirth,
            Collections.singletonList("email"),
            true,
            System.currentTimeMillis(),
            System.currentTimeMillis()
        );

        firestore.collection("users").document(uid).set(user)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                navigateToMainActivity();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Lỗi khi lưu thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void saveUserInfo() {
        String displayName = displayNameInput.getText().toString().trim();
        String dateOfBirth = dateOfBirthInput.getText().toString().trim();

        if (TextUtils.isEmpty(displayName)) {
            Toast.makeText(getContext(), "Tên hiển thị không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();
            String email = auth.getCurrentUser().getEmail();

            User user = new User(
                uid,
                email,
                displayName,
                null, // Photo URL (optional)
                dateOfBirth,
                Collections.singletonList("email"),
                true,
                System.currentTimeMillis(),
                System.currentTimeMillis()
            );

            firestore.collection("users").document(uid).set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Thông tin đã được lưu", Toast.LENGTH_SHORT).show();
                    navigateToMainActivity();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi khi lưu thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        }
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
