package com.example.officialtsr.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.example.officialtsr.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
    import com.google.firebase.auth.UserInfo;

import android.app.DatePickerDialog;
import java.util.Calendar;
import java.util.UUID;

import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Locale;
import android.util.Log;

public class AccountFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    // UI Components
    private ImageView avatarImage;
    private Button editButton, saveButton, changeAvatarButton, changePasswordButton;
    private LinearLayout editForm;
    private EditText firstNameInput, lastNameInput, dateOfBirthInput, emailInput;
    private TextView displayEmail, displayFirstName, displayLastName, displayDateOfBirth;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference userDocRef;
    private StorageReference storageRef;

    private Uri avatarUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        // Initialize UI components
        initializeUI(view);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("avatars");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userDocRef = db.collection("users").document(currentUser.getUid());
            loadUserData(currentUser);
            checkIfEmailAccount(currentUser);
        }

        setupListeners();
        return view;
    }

    private void initializeUI(View view) {
        avatarImage = view.findViewById(R.id.avatar_image);
        editButton = view.findViewById(R.id.btn_edit);
        saveButton = view.findViewById(R.id.btn_save);
        changeAvatarButton = view.findViewById(R.id.btn_change_avatar);
        changePasswordButton = view.findViewById(R.id.btn_change_password);
        editForm = view.findViewById(R.id.edit_form);
        firstNameInput = view.findViewById(R.id.input_first_name);
        lastNameInput = view.findViewById(R.id.input_last_name);
        dateOfBirthInput = view.findViewById(R.id.input_date_of_birth);
        emailInput = view.findViewById(R.id.input_email);

        displayEmail = view.findViewById(R.id.display_email);
        displayFirstName = view.findViewById(R.id.display_first_name);
        displayLastName = view.findViewById(R.id.display_last_name);
        displayDateOfBirth = view.findViewById(R.id.display_date_of_birth);
    }

    private void loadUserData(FirebaseUser currentUser) {
        // Load email from FirebaseAuth
        emailInput.setText(currentUser.getEmail());
        emailInput.setInputType(InputType.TYPE_NULL); // Make email non-editable
        displayEmail.setText("Email: " + currentUser.getEmail());

        // Set default avatar first to avoid glitches
        Glide.with(this)
            .load(R.drawable.user)
            .into(avatarImage);

        // Load other user data from Firestore
        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String avatarUrl = documentSnapshot.getString("avatar");
                String firstName = documentSnapshot.getString("firstName");
                String lastName = documentSnapshot.getString("lastName");
                String dateOfBirth = documentSnapshot.getString("dateOfBirth");

                // Set data to views
                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    // If user has a custom avatar, load it
                    Glide.with(this)
                        .load(avatarUrl)
                        .placeholder(R.drawable.user)
                        .error(R.drawable.user)
                        .into(avatarImage);
                }
                
                firstNameInput.setText(firstName);
                lastNameInput.setText(lastName);
                dateOfBirthInput.setText(dateOfBirth);

                displayFirstName.setText("First Name: " + firstName);
                displayLastName.setText("Last Name: " + lastName);
                displayDateOfBirth.setText("Date of Birth: " + dateOfBirth);
            } else {
                Toast.makeText(getContext(), "User data not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to load user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void checkIfEmailAccount(FirebaseUser currentUser) {
        boolean isEmailAccount = false;

        // Check the provider ID to determine if the account is email/password
        for (UserInfo userInfo : currentUser.getProviderData()) {
            if (EmailAuthProvider.PROVIDER_ID.equals(userInfo.getProviderId())) {
                isEmailAccount = true;
                break;
            }
        }

        if (!isEmailAccount) {
            // Disable or hide the "Change Password" button for non-email accounts
            changePasswordButton.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        editButton.setOnClickListener(v -> switchToEditMode());
        saveButton.setOnClickListener(v -> saveUserData());
        changeAvatarButton.setOnClickListener(v -> openImagePicker());
        changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());

        // Show DatePickerDialog when clicking on dateOfBirthInput
        dateOfBirthInput.setOnClickListener(v -> showDatePicker());
    }

    private void switchToEditMode() {
        editForm.setVisibility(View.VISIBLE);
        editButton.setVisibility(View.GONE);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            avatarUri = data.getData();
            avatarImage.setImageURI(avatarUri);
        }
    }

    private void showDatePicker() {
        // Get current date
        Calendar currentDate = Calendar.getInstance();
        
        // Calculate minimum date (5 years ago from now)
        Calendar minAgeDate = Calendar.getInstance();
        minAgeDate.add(Calendar.YEAR, -5);
        
        int year = currentDate.get(Calendar.YEAR);
        int month = currentDate.get(Calendar.MONTH);
        int day = currentDate.get(Calendar.DAY_OF_MONTH);

        // Show DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Create a calendar with the selected date
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);
                    
                    // Check if selected date meets minimum age requirement (5 years)
                    if (selectedDate.after(minAgeDate)) {
                        Toast.makeText(getContext(), "Bạn phải đủ 5 tuổi!", Toast.LENGTH_SHORT).show();
                        
                        // Reset to minimum allowed date (exactly 5 years ago)
                        selectedYear = minAgeDate.get(Calendar.YEAR);
                        selectedMonth = minAgeDate.get(Calendar.MONTH);
                        selectedDay = minAgeDate.get(Calendar.DAY_OF_MONTH);
                    }
                    
                    // Format date as dd/MM/yyyy
                    String formattedDate = String.format("%02d/%02d/%04d", selectedDay, (selectedMonth + 1), selectedYear);
                    dateOfBirthInput.setText(formattedDate);
                }, year, month, day);
        
        // Set max date to current date (cannot select future dates)
        datePickerDialog.getDatePicker().setMaxDate(currentDate.getTimeInMillis());
        
        // Set min date to 100 years ago (reasonable age limit)
        Calendar hundredYearsAgo = Calendar.getInstance();
        hundredYearsAgo.add(Calendar.YEAR, -100);
        datePickerDialog.getDatePicker().setMinDate(hundredYearsAgo.getTimeInMillis());
        
        datePickerDialog.show();
    }

    private void saveUserData() {
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String dateOfBirth = dateOfBirthInput.getText().toString().trim();

        // Validate fields
        if (!isValidFirstName(firstName)) {
            Toast.makeText(getContext(), "Invalid first name. Only UTF-8 letters are allowed.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidLastName(lastName)) {
            Toast.makeText(getContext(), "Invalid last name. Only UTF-8 letters are allowed.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidDateOfBirth(dateOfBirth)) {
            Toast.makeText(getContext(), "Invalid date of birth. Use format dd/MM/yyyy.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validate minimum age requirement (5 years)
        if (!isUserOldEnough(dateOfBirth)) {
            Toast.makeText(getContext(), "Bạn phải đủ 5 tuổi!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (avatarUri != null) {
            // Generate a unique name for the avatar file
            String uniqueFileName = mAuth.getCurrentUser().getUid() + "_" + UUID.randomUUID().toString() + ".jpg";
            StorageReference avatarRef = storageRef.child(uniqueFileName);

            avatarRef.putFile(avatarUri).addOnSuccessListener(taskSnapshot -> avatarRef.getDownloadUrl().addOnSuccessListener(uri -> {
                updateUserData(uri.toString(), firstName, lastName, dateOfBirth);
            })).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Failed to upload avatar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            updateUserData(null, firstName, lastName, dateOfBirth);
        }
    }

    private boolean isValidFirstName(String firstName) {
        // Allow UTF-8 letters, including spaces and hyphens
        return firstName.matches("^[\\p{L} .'-]+$");
    }

    private boolean isValidLastName(String lastName) {
        // Allow UTF-8 letters, including spaces and hyphens
        return lastName.matches("^[\\p{L} .'-]+$");
    }

    private boolean isValidDateOfBirth(String dateOfBirth) {
        // Validate date format dd/MM/yyyy
        return dateOfBirth.matches("^\\d{2}/\\d{2}/\\d{4}$");
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
    }

    private boolean isValidPassword(String password) {
        // Password must be at least 8 characters, contain at least one uppercase letter, one lowercase letter, and one number
        return password.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$");
    }

    private void updateUserData(String avatarUrl, String firstName, String lastName, String dateOfBirth) {
        Map<String, Object> updates = new HashMap<>();
        if (avatarUrl != null) {
            updates.put("avatar", avatarUrl);
        }
        updates.put("firstName", firstName);
        updates.put("lastName", lastName);
        updates.put("dateOfBirth", dateOfBirth);

        userDocRef.update(updates).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Information updated successfully", Toast.LENGTH_SHORT).show();

            // Update display fields
            displayFirstName.setText("First Name: " + firstName);
            displayLastName.setText("Last Name: " + lastName);
            displayDateOfBirth.setText("Date of Birth: " + dateOfBirth);

            // Switch back to view mode
            editForm.setVisibility(View.GONE);
            editButton.setVisibility(View.VISIBLE);
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to update information: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Change Password");

        // Create a layout for the dialog
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // Add input fields
        EditText currentPasswordInput = new EditText(requireContext());
        currentPasswordInput.setHint("Current Password");
        currentPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(currentPasswordInput);

        EditText newPasswordInput = new EditText(requireContext());
        newPasswordInput.setHint("New Password");
        newPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(newPasswordInput);

        builder.setView(layout);

        // Add buttons
        builder.setPositiveButton("Change", (dialog, which) -> {
            String currentPassword = currentPasswordInput.getText().toString().trim();
            String newPassword = newPasswordInput.getText().toString().trim();

            if (!isValidPassword(newPassword)) {
                Toast.makeText(getContext(), "Invalid password. Must be at least 8 characters, include uppercase, lowercase, and a number.", Toast.LENGTH_SHORT).show();
                return;
            }

            changePassword(currentPassword, newPassword);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void changePassword(String currentPassword, String newPassword) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Re-authenticate the user
        String email = user.getEmail();
        if (email == null) {
            Toast.makeText(getContext(), "Email not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);
        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Update the password
                user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Toast.makeText(getContext(), "Password changed successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to change password: " + updateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "Re-authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Add method to check if user meets minimum age requirement
    private boolean isUserOldEnough(String dateOfBirthStr) {
        try {
            // Parse the date string in format dd/MM/yyyy
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Calendar birthDate = Calendar.getInstance();
            birthDate.setTime(sdf.parse(dateOfBirthStr));
            
            // Calculate minimum allowed birth date (5 years ago)
            Calendar minAgeDate = Calendar.getInstance();
            minAgeDate.add(Calendar.YEAR, -5);
            
            // Check if birth date is before or equal to the minimum age date
            return birthDate.before(minAgeDate) || birthDate.equals(minAgeDate);
        } catch (Exception e) {
            Log.e("AccountFragment", "Date parsing error: " + e.getMessage());
            return false;
        }
    }
}
