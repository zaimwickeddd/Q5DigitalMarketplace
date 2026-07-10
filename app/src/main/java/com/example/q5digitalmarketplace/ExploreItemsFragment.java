package com.example.q5digitalmarketplace;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.navigation.NavigationView;
import java.util.ArrayList;
import java.util.List;

public class ExploreItemsFragment extends Fragment implements ListingAdapter.OnListingActionListener {

    private DatabaseHelper dbHelper;
    private DrawerLayout drawerLayout;
    private ListingAdapter adapter;
    private EditText etSearchBar;

    private final List<Listing> masterList = new ArrayList<>();
    private final List<Listing> activeFilteredList = new ArrayList<>();

    private String selectedCategoryFilter = "All";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore_items, container, false);

        dbHelper = new DatabaseHelper(getContext());
        drawerLayout = view.findViewById(R.id.explore_drawer_layout);
        etSearchBar = view.findViewById(R.id.et_search_bar);
        RecyclerView recyclerView = view.findViewById(R.id.rv_explore_items_list);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Initialize Adapter with the filtered list reference
        adapter = new ListingAdapter(activeFilteredList, this);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.btn_open_filters).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));

        setupTextSearchWatcher();
        setupSidebarMenuSelectionFilters(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    private void refreshData() {
        masterList.clear();
        masterList.addAll(dbHelper.getAllListings());
        applyCombinedSearchFilters();
    }

    private void setupTextSearchWatcher() {
        etSearchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyCombinedSearchFilters();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupSidebarMenuSelectionFilters(View view) {
        NavigationView filterSidebar = view.findViewById(R.id.filter_navigation_sidebar);
        filterSidebar.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.filter_all) selectedCategoryFilter = "All";
            else if (itemId == R.id.filter_electronics) selectedCategoryFilter = "Electronics";
            else if (itemId == R.id.filter_books) selectedCategoryFilter = "Books";
            else if (itemId == R.id.filter_clothes) selectedCategoryFilter = "Clothes";
            else if (itemId == R.id.filter_bags) selectedCategoryFilter = "Bags";
            else if (itemId == R.id.filter_services) selectedCategoryFilter = "Services";

            applyCombinedSearchFilters();
            drawerLayout.closeDrawer(GravityCompat.END);
            return true;
        });
    }

    private void applyCombinedSearchFilters() {
        String keyword = etSearchBar.getText().toString().trim().toLowerCase();
        activeFilteredList.clear();

        for (Listing item : masterList) {
            boolean matchesSearchText = item.getTitle().toLowerCase().contains(keyword);
            boolean matchesCategorySelection = selectedCategoryFilter.equals("All") ||
                    item.getCategory().equalsIgnoreCase(selectedCategoryFilter);

            if (matchesSearchText && matchesCategorySelection) {
                activeFilteredList.add(item);
            }
        }
        // Notify the adapter that the data inside the list has changed
        adapter.notifyDataSetChanged();
    }

    // FIXED: Navigation logic resides here in the Fragment
    @Override
    public void onItemClick(Listing listing) {
        if (getContext() == null || listing == null) return;

        // Start the Detail Activity and pass the serializable Listing object
        Intent intent = new Intent(getContext(), ListingDetailActivity.class);
        intent.putExtra("selected_listing", listing);
        startActivity(intent);
    }

    // Unused interface methods kept for interface compliance
    @Override public void onEdit(Listing listing) {}
    @Override public void onDelete(Listing listing) {}
    @Override public void onMarkSold(Listing listing) {}
}