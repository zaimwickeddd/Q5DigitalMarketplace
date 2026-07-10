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

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view_sidebar);

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeExploreFragment())
                    .commit();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) selectedFragment = new HomeExploreFragment();
            else if (itemId == R.id.nav_explore_items) selectedFragment = new ExploreItemsFragment();
            else if (itemId == R.id.nav_sell) selectedFragment = new CreateListingFragment();
            else if (itemId == R.id.nav_profile) selectedFragment = new ProfileDashboardFragment();

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                return true;
            }
            return false;
        });
    }

    // REQUIRED: This allows your Fragment to open the drawer from the filter icon
    public DrawerLayout getDrawerLayout() {
        return this.drawerLayout;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        // Filter Logic
        if (id == R.id.filter_all || id == R.id.filter_electronics || id == R.id.filter_books ||
                id == R.id.filter_clothes || id == R.id.filter_bags || id == R.id.filter_services) {

            if (!(currentFragment instanceof HomeExploreFragment)) {
                HomeExploreFragment homeFragment = new HomeExploreFragment();
                Bundle args = new Bundle();
                args.putInt("FILTER_CATEGORY_ID", id);
                homeFragment.setArguments(args);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();
            } else {
                ((HomeExploreFragment) currentFragment).filterMarketplaceCategory(id);
            }
        }

        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }
}