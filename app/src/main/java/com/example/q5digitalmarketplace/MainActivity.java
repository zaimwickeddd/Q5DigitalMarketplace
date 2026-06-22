package com.example.q5digitalmarketplace;

import android.os.Bundle;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Enable edge-to-edge layout support
        EdgeToEdge.enable(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Prevent layout from getting buried under the top camera notch or status bar
        View mainLayout = findViewById(R.id.main_root_layout);
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
                Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                // Add padding to the top of your view equal to the status bar/notch height
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return windowInsets;
            });
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Load the initial Home/Explore Fragment on startup
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeExploreFragment())
                    .commit();
        }

        // Handle tab item clicks
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeExploreFragment();
            } else if (itemId == R.id.nav_sell) {
                // selectedFragment = new CreateListingFragment();
            } else if (itemId == R.id.nav_favourites) {
                // selectedFragment = new FavouritesFragment();
            } else if (itemId == R.id.nav_profile) {
                // selectedFragment = new ProfileDashboardFragment();
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