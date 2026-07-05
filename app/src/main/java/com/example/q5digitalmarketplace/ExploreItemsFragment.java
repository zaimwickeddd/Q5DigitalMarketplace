package com.example.q5digitalmarketplace;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.navigation.NavigationView;
import java.util.List;

public class ExploreItemsFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private DrawerLayout drawerLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore_items, container, false);

        dbHelper = new DatabaseHelper(getContext());
        drawerLayout = view.findViewById(R.id.explore_drawer_layout);

        // Setup Grid List View
        RecyclerView recyclerView = view.findViewById(R.id.rv_explore_items_list);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        List<Listing> itemsList = dbHelper.getAllListings();
        ListingAdapter adapter = new ListingAdapter(itemsList);
        recyclerView.setAdapter(adapter);

        // 1. Open the Side Filter panel when clicking the blue header button
        view.findViewById(R.id.btn_open_filters).setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.END); // Opens smoothly from the right side
        });

        // 2. Listen for category selections inside your sidebar
        NavigationView filterSidebar = view.findViewById(R.id.filter_navigation_sidebar);
        filterSidebar.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.filter_recommended) {
                // Perform target query data filtering actions...
            } else if (itemId == R.id.filter_trending) {
                // Handle filter logic...
            }

            drawerLayout.closeDrawer(GravityCompat.END); // Auto slide closed after tap
            return true;
        });

        return view;
    }
}