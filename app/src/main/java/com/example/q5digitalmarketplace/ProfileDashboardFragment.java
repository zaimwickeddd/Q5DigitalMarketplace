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
                getActivity().finish(); // Simple exit action execution
            }
        });

        return view;
    }

    private void loadProfileData() {
        // Query record row for student session context (Using ID 1)
        Cursor cursor = dbHelper.getStudentProfile(1);
        if (cursor != null && cursor.moveToFirst()) {
            tvName.setText(cursor.getString(0));
            tvEmail.setText(cursor.getString(1));
            cursor.close();
        } else {
            // Fallback default placeholder info
            tvName.setText("Awang Najib");
            tvEmail.setText("najib123@gmail.com");
        }

        // Pull active contextual dynamic transaction counts
        tvListings.setText(String.valueOf(dbHelper.getListingsCount()));
        tvFavourites.setText(String.valueOf(dbHelper.getWishlistCount()));
    }
}