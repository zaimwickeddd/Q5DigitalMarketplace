package com.example.q5digitalmarketplace;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.app.Activity;

public class EditProfileFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private EditText etUsername, etPassword;
    private TextView tvInitial;
    private ImageView imgProfile;
    private String userEmail;
    private String selectedImageUri = null;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        selectedImageUri = imageUri.toString();
                        imgProfile.setImageURI(imageUri);
                        imgProfile.setVisibility(View.VISIBLE);
                        tvInitial.setVisibility(View.GONE);
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        dbHelper = new DatabaseHelper(getContext());
        etUsername = view.findViewById(R.id.et_edit_username);
        etPassword = view.findViewById(R.id.et_edit_password);
        tvInitial = view.findViewById(R.id.tv_edit_initial);
        imgProfile = view.findViewById(R.id.img_edit_profile);

        SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userEmail = prefs.getString("user_email", null);

        loadCurrentData();

        view.findViewById(R.id.btn_back).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        view.findViewById(R.id.btn_change_photo).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });

        view.findViewById(R.id.btn_save_profile).setOnClickListener(v -> saveChanges());

        return view;
    }

    private void loadCurrentData() {
        if (userEmail != null) {
            Cursor cursor = dbHelper.getStudentProfileByEmail(userEmail);
            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(1); // Name
                String imagePath = cursor.getString(5); // ProfileImage is index 5

                etUsername.setText(name);

                // 🛠️ FIXED: UPGRADED DYNAMIC SMART IMAGE PARSER FOR EDIT VIEWPORT
                if (imagePath != null && !imagePath.trim().isEmpty()) {
                    Context context = getContext();
                    int resId = 0;
                    if (context != null) {
                        resId = context.getResources().getIdentifier(imagePath.trim(), "drawable", context.getPackageName());
                    }

                    if (resId != 0) {
                        // Successfully matched a local mock asset name string from drawables
                        imgProfile.setImageResource(resId);
                    } else {
                        // Fallback: Parse string path structure as a local device content URI
                        imgProfile.setImageURI(Uri.parse(imagePath.trim()));
                    }
                    imgProfile.setVisibility(View.VISIBLE);
                    tvInitial.setVisibility(View.GONE);
                } else if (name != null && !name.isEmpty()) {
                    // Standard text initial fallback mode if image path string data is completely empty
                    tvInitial.setText(String.valueOf(name.charAt(0)).toUpperCase());
                    imgProfile.setVisibility(View.GONE);
                    tvInitial.setVisibility(View.VISIBLE);
                }
                cursor.close();
            }
        }
    }

    private void saveChanges() {
        String newName = etUsername.getText().toString().trim();
        String newPassword = etPassword.getText().toString().trim();

        if (newName.isEmpty()) {
            Toast.makeText(getContext(), "Username cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.isEmpty() && newPassword.length() < 6) {
            Toast.makeText(getContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update Name and Image
        String currentPhone = "";
        Cursor cursor = dbHelper.getStudentProfileByEmail(userEmail);
        if (cursor != null && cursor.moveToFirst()) {
            currentPhone = cursor.getString(3); // PhoneNum
            cursor.close();
        }

        boolean profileUpdated = dbHelper.updateStudentProfile(userEmail, newName, currentPhone);
        if (selectedImageUri != null) {
            dbHelper.updateStudentProfileImage(userEmail, selectedImageUri);
        }

        boolean passwordUpdated = true;
        if (!newPassword.isEmpty()) {
            passwordUpdated = dbHelper.updateStudentPassword(userEmail, newPassword);
        }

        if (profileUpdated && passwordUpdated) {
            Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
        } else {
            Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
        }
    }
}