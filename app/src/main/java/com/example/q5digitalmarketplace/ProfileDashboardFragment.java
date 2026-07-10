package com.example.q5digitalmarketplace;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileDashboardFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private TextView tvName, tvEmail, tvListings, tvFavourites;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_dashboard, container, false);

        dbHelper = new DatabaseHelper(getContext());

        tvName = view.findViewById(R.id.tv_profile_name);
        tvEmail = view.findViewById(R.id.tv_profile_email);
        tvListings = view.findViewById(R.id.tv_count_listings);
        tvFavourites = view.findViewById(R.id.tv_count_favourites);

        loadProfileData();

        // 1. Fixed Lambda Warnings and Simplified Logic
        view.findViewById(R.id.btn_sign_out).setOnClickListener(v -> signOut());

        view.findViewById(R.id.rl_account_settings).setOnClickListener(v -> navigateTo(new AccountSettingsFragment()));

        // This handles the "My Favourites" row click
        view.findViewById(R.id.rl_my_favourites).setOnClickListener(v -> navigateTo(new FavoritesFragment()));

        View rlMyListings = view.findViewById(R.id.rl_my_listings);
        if (rlMyListings != null) {
            rlMyListings.setOnClickListener(v -> {
                if (getActivity() != null) {
                    startActivity(new Intent(getActivity(), MyListingsActivity.class));
                }
            });
        }

        return view;
    }

    // Helper to replace repetitive navigation code
    private void navigateTo(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void signOut() {
        if (getActivity() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            prefs.edit().clear().apply();

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }

    private void loadProfileData() {
        String loggedInEmail = null;
        if (getActivity() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            loggedInEmail = prefs.getString("user_email", null);
        }

        if (loggedInEmail != null && !loggedInEmail.isEmpty()) {
            Cursor cursor = dbHelper.getStudentProfileByEmail(loggedInEmail);
            if (cursor != null && cursor.moveToFirst()) {
                tvName.setText(cursor.getString(0));
                tvEmail.setText(cursor.getString(1));
                cursor.close();
            } else {
                tvName.setText(dbHelper.getUserNameByEmail(loggedInEmail));
                tvEmail.setText(loggedInEmail);
            }
        } else {
            // Replaced hardcoded strings with placeholders (use strings.xml for best practice)
            tvName.setText("Guest User");
            tvEmail.setText("Not signed in");
        }

        tvListings.setText(String.valueOf(dbHelper.getListingsCount()));
        tvFavourites.setText(String.valueOf(dbHelper.getWishlistCount()));
    }
}