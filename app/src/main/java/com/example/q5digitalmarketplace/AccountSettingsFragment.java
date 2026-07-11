package com.example.q5digitalmarketplace;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

public class AccountSettingsFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private String loggedInEmail;

    // 🛠️ UI Toggles matching layout preferences
    private SwitchCompat switchPush, switchEmail, switchVolume, switchShowEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_settings, container, false);

        dbHelper = new DatabaseHelper(getContext());

        // Bind UI Toggle preferences elements cleanly
        switchPush = view.findViewById(R.id.switch_push);
        switchEmail = view.findViewById(R.id.switch_email);
        switchVolume = view.findViewById(R.id.switch_volume);
        switchShowEmail = view.findViewById(R.id.switch_show_email);

        Button btnSave = view.findViewById(R.id.btn_save_settings);
        View btnBack = view.findViewById(R.id.btn_back);

        // Extract session identification keys
        if (getActivity() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            loggedInEmail = prefs.getString("user_email", null);
        }

        // Load saved configuration settings states
        loadSettingsPreferences();

        // Wire up back button navigation actions
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        // Wire up settings configuration commit click handler
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveSettingsPreferences());
        }

        return view;
    }

    /**
     * Reads state positions out of persistent space unique to each profile account context
     */
    private void loadSettingsPreferences() {
        if (getActivity() == null || loggedInEmail == null || loggedInEmail.isEmpty()) return;

        // Open custom preferences file separate from baseline login sessions
        SharedPreferences settingsPrefs = getActivity().getSharedPreferences("AccountSettings_" + loggedInEmail, Context.MODE_PRIVATE);

        if (switchPush != null) {
            switchPush.setChecked(settingsPrefs.getBoolean("push_enabled", true)); // Defaults ON
        }
        if (switchEmail != null) {
            switchEmail.setChecked(settingsPrefs.getBoolean("email_enabled", false));
        }
        if (switchVolume != null) {
            switchVolume.setChecked(settingsPrefs.getBoolean("volume_enabled", true)); // Defaults ON
        }
        if (switchShowEmail != null) {
            switchShowEmail.setChecked(settingsPrefs.getBoolean("show_email_enabled", false));
        }
    }

    /**
     * Commits layout preference toggle values cleanly to account preference profiles
     */
    private void saveSettingsPreferences() {
        if (getActivity() == null || loggedInEmail == null || loggedInEmail.isEmpty()) return;

        SharedPreferences settingsPrefs = getActivity().getSharedPreferences("AccountSettings_" + loggedInEmail, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settingsPrefs.edit();

        if (switchPush != null) editor.putBoolean("push_enabled", switchPush.isChecked());
        if (switchEmail != null) editor.putBoolean("email_enabled", switchEmail.isChecked());
        if (switchVolume != null) editor.putBoolean("volume_enabled", switchVolume.isChecked());
        if (switchShowEmail != null) editor.putBoolean("show_email_enabled", switchShowEmail.isChecked());

        editor.apply();

        Toast.makeText(getContext(), "Settings preferences updated completely!", Toast.LENGTH_SHORT).show();
        getParentFragmentManager().popBackStack(); // Retract screen layer safely
    }
}