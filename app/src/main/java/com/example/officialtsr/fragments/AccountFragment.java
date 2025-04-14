package com.example.officialtsr.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.officialtsr.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AccountFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView profileImage;
    private TextView fullNameTextView;
    private TextView emailTextView;
    private TextView dateOfBirthTextView;
    private Uri selectedImageUri;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private String uid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        profileImage = view.findViewById(R.id.account_profile_image);
        fullNameTextView = view.findViewById(R.id.account_full_name);
        emailTextView = view.findViewById(R.id.account_email);
        dateOfBirthTextView = view.findViewById(R.id.account_date_of_birth);
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        if (getArguments() != null) {
            String fullName = getArguments().getString("fullName", "N/A");
            String email = getArguments().getString("email", "N/A");
            String photoUrl = getArguments().getString("photoUrl");
            String dateOfBirth = getArguments().getString("dateOfBirth", "N/A");

            fullNameTextView.setText(fullName);
            emailTextView.setText(email);
            dateOfBirthTextView.setText(dateOfBirth);

            if (photoUrl != null) {
                Glide.with(requireContext()).load(photoUrl).into(profileImage);
            }
        }

        profileImage.setOnClickListener(v -> openImagePicker());
        dateOfBirthTextView.setOnClickListener(v -> showDatePickerDialog());

        view.findViewById(R.id.btn_edit_info).setOnClickListener(v -> saveUpdatedInfo());

        return view;
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            requireContext(),
            (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                dateOfBirthTextView.setText(dateFormat.format(calendar.getTime()));
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void saveUpdatedInfo() {
        String dateOfBirth = dateOfBirthTextView.getText().toString().trim();

        if (TextUtils.isEmpty(dateOfBirth)) {
            Toast.makeText(getContext(), "Ngày sinh không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri != null) {
            uploadProfileImage(dateOfBirth);
        } else {
            updateFirestoreUserInfo(null, dateOfBirth);
        }
    }

    private void uploadProfileImage(String dateOfBirth) {
        StorageReference avatarRef = storage.getReference().child("avatar/" + uid + ".jpg");
        avatarRef.putFile(selectedImageUri)
            .addOnSuccessListener(taskSnapshot -> avatarRef.getDownloadUrl()
                .addOnSuccessListener(uri -> updateFirestoreUserInfo(uri.toString(), dateOfBirth))
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi khi lấy URL ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show()))
            .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi khi tải ảnh lên: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateFirestoreUserInfo(String photoUrl, String dateOfBirth) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            firestore.collection("users").document(uid)
                .update(
                    "photoURL", photoUrl,
                    "dateOfBirth", dateOfBirth
                )
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Thông tin đã được cập nhật", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi khi cập nhật thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            profileImage.setImageURI(selectedImageUri);
        }
    }
}
