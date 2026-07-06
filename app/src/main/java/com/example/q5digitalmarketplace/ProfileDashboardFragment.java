package com.example.q5digitalmarketplace;

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

        // Handle logout trigger action button
        view.findViewById(R.id.btn_sign_out).setOnClickListener(v -> {
            if (getActivity() != null) {
                android.content.Intent intent = new android.content.Intent(getActivity(), WelcomeActivity.class);
                intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
            }
        });

        // Handle Account Settings navigation
        view.findViewById(R.id.rl_account_settings).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AccountSettingsFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void loadProfileData() {
        // 1. Safely capture session email from host activity login intent
        String loggedInEmail = null;
        if (getActivity() != null && getActivity().getIntent() != null) {
            loggedInEmail = getActivity().getIntent().getStringExtra("USER_EMAIL");
        }

        if (loggedInEmail != null && !loggedInEmail.isEmpty()) {
            // Query record row for the signed-in user
            Cursor cursor = dbHelper.getStudentProfileByEmail(loggedInEmail);
            if (cursor != null && cursor.moveToFirst()) {
                tvName.setText(cursor.getString(0)); // Name
                tvEmail.setText(cursor.getString(1)); // Email
                cursor.close();
            } else {
                // Fallback to name from Users table if Student record not found
                String username = dbHelper.getUserNameByEmail(loggedInEmail);
                tvName.setText(username);
                tvEmail.setText(loggedInEmail);
            }
        } else {
            // Fallback default placeholder info for Guest or missing session
            tvName.setText("Guest User");
            tvEmail.setText("Not signed in");
        }

        // Pull active contextual dynamic transaction counts
        tvListings.setText(String.valueOf(dbHelper.getListingsCount()));
        tvFavourites.setText(String.valueOf(dbHelper.getWishlistCount()));
    }
}