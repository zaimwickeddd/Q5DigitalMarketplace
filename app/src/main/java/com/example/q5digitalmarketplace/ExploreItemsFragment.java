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

public class ExploreItemsFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private DrawerLayout drawerLayout;
    private ListingAdapter adapter;

    private final List<Listing> masterList = new ArrayList<>();
    private final List<Listing> activeFilteredList = new ArrayList<>();

    private String selectedCategoryFilter = "All";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore_items, container, false);

        dbHelper = new DatabaseHelper(getContext());
        drawerLayout = view.findViewById(R.id.explore_drawer_layout);

        EditText etSearchBar = view.findViewById(R.id.et_search_bar);

        RecyclerView recyclerView = view.findViewById(R.id.rv_explore_items_list);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        masterList.clear();
        masterList.addAll(dbHelper.getAllListings());

        activeFilteredList.clear();
        activeFilteredList.addAll(masterList);

        adapter = new ListingAdapter(activeFilteredList);
        recyclerView.setAdapter(adapter);

        // UPDATED: Now forwards primary and foreign key IDs to fetch WhatsApp details accurately
        adapter.setOnItemClickListener(listing -> {
            Intent intent = new Intent(getContext(), ListingDetailActivity.class);
            intent.putExtra("id", listing.getId()); // Crucial for looking up phone data via SQL Join
            intent.putExtra("title", listing.getTitle());
            intent.putExtra("price", listing.getCardPrice());
            intent.putExtra("category", listing.getCategory());
            intent.putExtra("condition", listing.getCondition());
            intent.putExtra("image_path", listing.getImagePath());
            intent.putExtra("description", listing.getDescription());
            intent.putExtra("seller_id", listing.getSellerId());
            startActivity(intent);
        });

        view.findViewById(R.id.btn_open_filters).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));

        setupTextSearchWatcher(etSearchBar);
        setupSidebarMenuSelectionFilters(view);

        return view;
    }

    private void setupTextSearchWatcher(EditText etSearchBar) {
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

            if (itemId == R.id.filter_all) {
                selectedCategoryFilter = "All";
            } else if (itemId == R.id.filter_electronics) {
                selectedCategoryFilter = "Electronics";
            } else if (itemId == R.id.filter_books) {
                selectedCategoryFilter = "Books";
            } else if (itemId == R.id.filter_clothes) {
                selectedCategoryFilter = "Clothes";
            } else if (itemId == R.id.filter_bags) {
                selectedCategoryFilter = "Bags";
            } else if (itemId == R.id.filter_services) {
                selectedCategoryFilter = "Services";
            }

            applyCombinedSearchFilters();
            drawerLayout.closeDrawer(GravityCompat.END);
            return true;
        });
    }

    @SuppressWarnings("NotifyDataSetChanged")
    private void applyCombinedSearchFilters() {
        View view = getView();
        if (view == null) return;

        EditText etSearchBar = view.findViewById(R.id.et_search_bar);
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

        adapter.notifyDataSetChanged();
    }
}