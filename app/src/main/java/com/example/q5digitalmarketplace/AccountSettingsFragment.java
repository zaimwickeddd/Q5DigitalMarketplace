package com.example.q5digitalmarketplace;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

public class AccountSettingsFragment extends Fragment {

    private SharedPreferences prefs;
    private static final String PREFS_NAME = "AccountSettings";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_settings, container, false);

        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Handle Back button click
        view.findViewById(R.id.btn_back).setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                // Fallback to Profile Dashboard if no backstack
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileDashboardFragment())
                        .commit();
            }
        });

        setupSwitches(view);

        return view;
    }

    private void setupSwitches(View view) {
        SwitchCompat switchPush = view.findViewById(R.id.switch_push);
        SwitchCompat switchEmail = view.findViewById(R.id.switch_email);
        SwitchCompat switchVolume = view.findViewById(R.id.switch_volume);
        SwitchCompat switchShowEmail = view.findViewById(R.id.switch_show_email);

        // Load saved states
        switchPush.setChecked(prefs.getBoolean("push_notifications", true));
        switchEmail.setChecked(prefs.getBoolean("email_notifications", false));
        switchVolume.setChecked(prefs.getBoolean("volume_notifications", false));
        switchShowEmail.setChecked(prefs.getBoolean("show_email", false));

        // Save states on change
        switchPush.setOnCheckedChangeListener((buttonView, isChecked) -> 
            prefs.edit().putBoolean("push_notifications", isChecked).apply());
        
        switchEmail.setOnCheckedChangeListener((buttonView, isChecked) -> 
            prefs.edit().putBoolean("email_notifications", isChecked).apply());
        
        switchVolume.setOnCheckedChangeListener((buttonView, isChecked) -> 
            prefs.edit().putBoolean("volume_notifications", isChecked).apply());
        
        switchShowEmail.setOnCheckedChangeListener((buttonView, isChecked) -> 
            prefs.edit().putBoolean("show_email", isChecked).apply());
    }
}