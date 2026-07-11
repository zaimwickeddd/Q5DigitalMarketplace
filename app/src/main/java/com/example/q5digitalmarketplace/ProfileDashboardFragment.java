package com.example.q5digitalmarketplace;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
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
    private ImageView ivProfileImage;
    private View btnEditProfile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_dashboard, container, false);

        dbHelper = new DatabaseHelper(getContext());

        // Initialize UI component resource bindings
        ivProfileImage = view.findViewById(R.id.iv_profile_image);
        tvName = view.findViewById(R.id.tv_profile_name);
        tvEmail = view.findViewById(R.id.tv_profile_email);
        tvListings = view.findViewById(R.id.tv_count_listings);
        tvFavourites = view.findViewById(R.id.tv_count_favourites);
        tvInitial = view.findViewById(R.id.tv_profile_initial);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);

        loadProfileData();

        // 1. 🛠️ CORRECTED ROUTE: Pencil Button opens the Dark Theme Edit Profile page
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new EditProfileFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        // 2. Clear SharedPreferences session details completely on sign-out click
        view.findViewById(R.id.btn_sign_out).setOnClickListener(v -> {
            if (getActivity() != null) {
                SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                prefs.edit().clear().apply();

                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
            }
        });

        // 3. Main Account Settings Menu Row (Routes to the White Theme Settings page)
        view.findViewById(R.id.rl_account_settings).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AccountSettingsFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // 4. Make My Listings button navigate to your standalone Activity
        View rlMyListings = view.findViewById(R.id.rl_my_listings);
        if (rlMyListings != null) {
            rlMyListings.setOnClickListener(v -> {
                if (getActivity() != null) {
                    Intent intent = new Intent(getActivity(), MyListingsActivity.class);
                    startActivity(intent);
                }
            });
        }

        // 5. My Favourites Fragment Navigation Action Mapping
        View rlMyFavourites = view.findViewById(R.id.rl_my_favourites);
        if (rlMyFavourites != null) {
            rlMyFavourites.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new FavoritesFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        // 6. Report & Analytics Navigation Click Handler
        View rlAnalytics = view.findViewById(R.id.rl_analytics);
        if (rlAnalytics != null) {
            rlAnalytics.setOnClickListener(v -> {
                if (getActivity() != null) {
                    Intent intent = new Intent(getActivity(), AnalyticsActivity.class);
                    startActivity(intent);
                }
            });
        }

        return view;
    }

    // 🛠️ ADDED: Refresh profile updates and mock metric metrics whenever returning to this tab viewport area
    @Override
    public void onResume() {
        super.onResume();
        loadProfileData();
    }

    private void loadProfileData() {
        String loggedInEmail = null;

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
                String imgPath = cursor.getString(5); // ProfileImage URL path index location

                tvName.setText(name);
                tvEmail.setText(email);

                // Toggle visibility between custom avatar image frame and text letter initial block
                if (imgPath != null && !imgPath.trim().isEmpty()) {
                    if (ivProfileImage != null) {
                        ivProfileImage.setImageURI(Uri.parse(imgPath));
                        ivProfileImage.setVisibility(View.VISIBLE);
                    }
                    if (tvInitial != null) tvInitial.setVisibility(View.GONE);
                } else {
                    if (ivProfileImage != null) ivProfileImage.setVisibility(View.GONE);
                    if (name != null && !name.isEmpty() && tvInitial != null) {
                        tvInitial.setText(String.valueOf(name.charAt(0)).toUpperCase());
                        tvInitial.setVisibility(View.VISIBLE);
                    }
                }
                cursor.close();
            } else {
                // Fallback to name from Users table if Student record row is missing
                String username = dbHelper.getUserNameByEmail(loggedInEmail);
                tvName.setText(username);
                tvEmail.setText(loggedInEmail);
                if (ivProfileImage != null) ivProfileImage.setVisibility(View.GONE);
                if (username != null && !username.isEmpty() && tvInitial != null) {
                    tvInitial.setText(String.valueOf(username.charAt(0)).toUpperCase());
                    tvInitial.setVisibility(View.VISIBLE);
                }
            }

            // Pull active dynamic user contextual counts safely using indexed IDs
            if (userId != -1) {
                tvListings.setText(String.valueOf(dbHelper.getMyListingsCount(userId)));
                tvFavourites.setText(String.valueOf(dbHelper.getWishlistListings(userId).size()));
            } else {
                tvListings.setText(String.valueOf(dbHelper.getListingsCount()));
                tvFavourites.setText(String.valueOf(dbHelper.getWishlistCount()));
            }
        } else {
            // Fallback placeholder defaults for Guest or invalid session profiles
            tvName.setText("Guest User");
            tvEmail.setText("Not signed in");
            tvListings.setText("0");
            tvFavourites.setText("0");
            if (ivProfileImage != null) ivProfileImage.setVisibility(View.GONE);
            if (tvInitial != null) {
                tvInitial.setText("G");
                tvInitial.setVisibility(View.VISIBLE);
            }
        }
    }
}