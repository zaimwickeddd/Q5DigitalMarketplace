package com.example.q5digitalmarketplace;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Initialize Views (Bottom Tabs & Sidebar Navigation)
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view_sidebar);

        // Bind the sidebar listener to this activity
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }

        // 2. Load your initial Homepage on app launch
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeExploreFragment())
                    .commit();
        }

        // 3. Handle bottom tab switching
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeExploreFragment();
            } else if (itemId == R.id.nav_explore_items) {
                selectedFragment = new ExploreItemsFragment();
            } else if (itemId == R.id.nav_sell) {
                selectedFragment = new CreateListingFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileDashboardFragment();
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

    // 4. Handle Global Sidebar Drawer Item Clicks (On every page)
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        // Check if a category item was clicked
        if (id == R.id.cat_all || id == R.id.cat_electronics || id == R.id.cat_books || id == R.id.cat_clothes || id == R.id.cat_bags) {

            // If the user isn't currently looking at the Home page, drop them back to Home to show results
            if (!(currentFragment instanceof HomeExploreFragment)) {
                HomeExploreFragment homeFragment = new HomeExploreFragment();

                // Pack chosen category filter into an bundle argument row
                Bundle args = new Bundle();
                args.putInt("FILTER_CATEGORY_ID", id);
                homeFragment.setArguments(args);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, homeFragment)
                        .commit();
            } else {
                // If they are already on the home page, feed the layout list update straight into it natively
                ((HomeExploreFragment) currentFragment).filterMarketplaceCategory(id);
            }
        }

        // Smoothly close the navigation drawer layout side frame right after click selection
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }
}