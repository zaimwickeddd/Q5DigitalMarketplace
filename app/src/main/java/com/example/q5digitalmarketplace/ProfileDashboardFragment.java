package com.example.q5digitalmarketplace;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileDashboardFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private TextView tvName, tvEmail, tvListings, tvFavourites, tvInitial;
    private ImageView imgAvatar;
    private View btnEditProfile;
    private View llAnalyticsSection;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_dashboard, container, false);

        dbHelper = new DatabaseHelper(getContext());

        tvName = view.findViewById(R.id.tv_profile_name);
        tvEmail = view.findViewById(R.id.tv_profile_email);
        tvListings = view.findViewById(R.id.tv_count_listings);
        tvFavourites = view.findViewById(R.id.tv_count_favourites);
        tvInitial = view.findViewById(R.id.tv_profile_initial);
        imgAvatar = view.findViewById(R.id.iv_profile_image);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        llAnalyticsSection = view.findViewById(R.id.ll_analytics_section);

        loadProfileData();

        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new EditProfileFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

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

        // Handle Favorites navigation
        View rlMyFavourites = view.findViewById(R.id.rl_my_favourites);
        if (rlMyFavourites != null) {
            rlMyFavourites.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new FavoritesFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

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

    @Override
    public void onResume() {
        super.onResume();
        loadProfileData();
    }

    private void loadProfileData() {
        String loggedInEmail = null;

        // Read directly from the persistent file instead of a fragile intent string
        if (getActivity() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            loggedInEmail = prefs.getString("user_email", null);
        }

        if (loggedInEmail != null && !loggedInEmail.isEmpty()) {
            int userId = dbHelper.getStuIDByEmail(loggedInEmail);

            // Query record row for the signed-in user
            Cursor cursor = dbHelper.getStudentProfileByEmail(loggedInEmail);
            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(1);
                String email = cursor.getString(2);
                String imagePath = cursor.getString(5);

                tvName.setText(name);
                tvEmail.setText(email);

                if (imagePath != null && !imagePath.isEmpty()) {
                    imgAvatar.setImageURI(android.net.Uri.parse(imagePath));
                    imgAvatar.setVisibility(View.VISIBLE);
                    tvInitial.setVisibility(View.GONE);
                } else if (name != null && !name.isEmpty() && tvInitial != null) {
                    tvInitial.setText(String.valueOf(name.charAt(0)).toUpperCase());
                    imgAvatar.setVisibility(View.GONE);
                    tvInitial.setVisibility(View.VISIBLE);
                }
                cursor.close();
            } else {
                // Fallback to name from Users table if Student record not found
                String username = dbHelper.getUserNameByEmail(loggedInEmail);
                tvName.setText(username);
                tvEmail.setText(loggedInEmail);
                if (username != null && !username.isEmpty() && tvInitial != null) {
                    tvInitial.setText(String.valueOf(username.charAt(0)).toUpperCase());
                }
            }

            // Pull active contextual dynamic transaction counts
            int myListingsCount = dbHelper.getMyListingsCount(userId);
            tvListings.setText(String.valueOf(myListingsCount));
            tvFavourites.setText(String.valueOf(dbHelper.getWishlistCount(userId)));

            // 🛠️ Restrict Analytics access: Only show if user has at least 1 listing
            if (llAnalyticsSection != null) {
                if (myListingsCount > 0) {
                    llAnalyticsSection.setVisibility(View.VISIBLE);
                } else {
                    llAnalyticsSection.setVisibility(View.GONE);
                }
            }
        } else {
            // Fallback default placeholder info for Guest or missing session
            tvName.setText("Guest User");
            tvEmail.setText("Not signed in");
            tvListings.setText("0");
            tvFavourites.setText("0");
            if (llAnalyticsSection != null) {
                llAnalyticsSection.setVisibility(View.GONE);
            }
        }
    }
}
