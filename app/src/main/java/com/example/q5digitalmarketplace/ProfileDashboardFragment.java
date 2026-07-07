package com.example.q5digitalmarketplace;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileDashboardFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private TextView tvName, tvEmail, tvListings, tvFavourites;
    // Hardcoded ID as per your current implementation
    private final int currentUserId = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_dashboard, container, false);

        dbHelper = new DatabaseHelper(getContext());

        // Initialize UI components
        tvName = view.findViewById(R.id.tv_profile_name);
        tvEmail = view.findViewById(R.id.tv_profile_email);
        tvListings = view.findViewById(R.id.tv_count_listings);
        tvFavourites = view.findViewById(R.id.tv_count_favourites);

        // Navigation to My Listings page
        view.findViewById(R.id.layout_my_listings).setOnClickListener(v -> {
            // Intent intent = new Intent(getActivity(), MyListingsActivity.class);
            // Optionally pass the ID if needed later: intent.putExtra("USER_ID", currentUserId);
            // startActivity(intent);
            Toast.makeText(getContext(), "My Listings feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Sign-out action
        view.findViewById(R.id.btn_sign_out).setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfileData();
    }

    private void loadProfileData() {
        // Query profile record for the specific user
        Cursor cursor = dbHelper.getStudentProfile(currentUserId);
        if (cursor != null && cursor.moveToFirst()) {
            tvName.setText(cursor.getString(0));
            tvEmail.setText(cursor.getString(1));
            cursor.close();
        } else {
            tvName.setText("User");
            tvEmail.setText("user@example.com");
        }

        // UPDATED: Use the new method to show count for THIS user only
        tvListings.setText(String.valueOf(dbHelper.getMyListingsCount(currentUserId)));

        // Assuming your wishlist count is global or also filtered by user
        tvFavourites.setText(String.valueOf(dbHelper.getWishlistCount()));
    }
}