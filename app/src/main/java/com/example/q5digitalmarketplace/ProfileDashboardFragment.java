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

        // 1. Clear SharedPreferences session details completely on sign-out click
        view.findViewById(R.id.btn_sign_out).setOnClickListener(v -> {
            if (getActivity() != null) {
                // Wipe the stored credentials block
                SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                prefs.edit().clear().apply();

                // Kick back to the Login screen safely
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
            }
        });

        // 2. Handle Account Settings navigation
        view.findViewById(R.id.rl_account_settings).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AccountSettingsFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // 3. Make My Listings button navigate to your standalone Activity
        View rlMyListings = view.findViewById(R.id.rl_my_listings);
        if (rlMyListings != null) {
            rlMyListings.setOnClickListener(v -> {
                if (getActivity() != null) {
                    // Launch the MyListingsActivity screen using a clean Intent wrapper
                    Intent intent = new Intent(getActivity(), MyListingsActivity.class);
                    startActivity(intent);
                }
            });
        }

        // 4. MERGED: Report & Analytics Navigation Click Handler
        View rlAnalytics = view.findViewById(R.id.rl_analytics);
        if (rlAnalytics != null) {
            rlAnalytics.setOnClickListener(v -> {
                if (getActivity() != null) {
                    // Launch the AnalyticsActivity chart page dashboard smoothly
                    Intent intent = new Intent(getActivity(), AnalyticsActivity.class);
                    startActivity(intent);
                }
            });
        }

        return view;
    }

    private void loadProfileData() {
        String loggedInEmail = null;

        // Read directly from the persistent file instead of a fragile intent string
        if (getActivity() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            loggedInEmail = prefs.getString("user_email", null);
        }

        if (loggedInEmail != null && !loggedInEmail.isEmpty()) {
            // Query record row for the signed-in user
            Cursor cursor = dbHelper.getStudentProfileByEmail(loggedInEmail);
            if (cursor != null && cursor.moveToFirst()) {
                tvName.setText(cursor.getString(1)); // Name (Index 1 matching updated DB helper query index positions)
                tvEmail.setText(cursor.getString(2)); // Email (Index 2 matching updated DB helper query index positions)
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