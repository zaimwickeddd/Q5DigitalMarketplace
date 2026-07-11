package com.example.q5digitalmarketplace;

import android.content.Context;
import android.content.Intent; // 🛠️ FIXED: Added missing import to prevent compilation crash
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AccountSettingsFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private EditText etUsername;
    private ImageView ivEditAvatar;
    private Uri selectedImageUri = null;
    private String loggedInEmail;

    // Gallery Picker System Activity Result Contract mapping logic
    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivEditAvatar.setImageURI(uri);
                    // Persist permanent access permission tokens for the local file image system structure framework
                    if (getActivity() != null) {
                        getActivity().getContentResolver().takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_settings, container, false);

        dbHelper = new DatabaseHelper(getContext());
        etUsername = view.findViewById(R.id.et_settings_username);
        ivEditAvatar = view.findViewById(R.id.iv_settings_avatar);
        Button btnSave = view.findViewById(R.id.btn_save_settings);

        // Fetch User Identity Session tokens
        if (getActivity() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            loggedInEmail = prefs.getString("user_email", null);
        }

        loadExistingUserData();

        // 🛠️ ADDED: Wire up the toolbar back navigation button arrow
        View btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        // Photo Gallery Picker Trigger mapping on profile avatar element tap view area bounds
        ivEditAvatar.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        // Save Modifications Click Handler Action Action Router logic
        btnSave.setOnClickListener(v -> {
            String newName = etUsername.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(getContext(), "Username cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            String pathStr = (selectedImageUri != null) ? selectedImageUri.toString() : "";

            boolean isUpdated = dbHelper.updateStudentProfileData(loggedInEmail, newName, pathStr);
            if (isUpdated) {
                Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack(); // Retract operations window back onto dashboard workspace list
            } else {
                Toast.makeText(getContext(), "Failed to save data changes.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void loadExistingUserData() {
        if (loggedInEmail != null) {
            Cursor cursor = dbHelper.getStudentProfileByEmail(loggedInEmail);
            if (cursor != null && cursor.moveToFirst()) {
                etUsername.setText(cursor.getString(1));
                String savedImg = cursor.getString(5);
                if (savedImg != null && !savedImg.trim().isEmpty()) {
                    selectedImageUri = Uri.parse(savedImg);
                    ivEditAvatar.setImageURI(selectedImageUri);
                }
                cursor.close();
            }
        }
    }
}