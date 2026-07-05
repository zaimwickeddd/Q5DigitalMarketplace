package com.example.q5digitalmarketplace;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Load your initial Homepage on app launch
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeExploreFragment())
                    .commit();
        }

        // Handle bottom tab switching
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeExploreFragment(); // Your main homepage dashboard
            } else if (itemId == R.id.nav_explore_items) {
                selectedFragment = new ExploreItemsFragment(); // Your beautiful new items grid page
            } else if (itemId == R.id.nav_sell) {
                selectedFragment = new CreateListingFragment(); // CONNECTED: Form page that writes to your local SQLite DB
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileDashboardFragment(); // Your profile layout
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });
    }
}