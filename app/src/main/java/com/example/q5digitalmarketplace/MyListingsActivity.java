package com.example.q5digitalmarketplace;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyListingsActivity extends AppCompatActivity implements ListingAdapter.OnListingActionListener {

    private RecyclerView rvListings;
    private DatabaseHelper dbHelper;
    private int currentUserId;
    private TabLayout tabLayout;
    private int currentTabPosition = 0; // 0: All, 1: Active, 2: Sold

    // New variables for search and sorting
    private EditText etSearchBar;
    private ImageButton btnFilter;
    private MyListingsAdapter adapter;
    private List<Listing> currentTabList = new ArrayList<>(); // Master reference for the current tab

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_listings);

        dbHelper = new DatabaseHelper(this);
        rvListings = findViewById(R.id.rv_my_listings);
        tabLayout = findViewById(R.id.tab_layout);
        rvListings.setLayoutManager(new LinearLayoutManager(this));

        // Initialize new search and filter widgets
        etSearchBar = findViewById(R.id.et_search_bar);
        btnFilter = findViewById(R.id.btn_filter_list);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String email = prefs.getString("user_email", "");
        currentUserId = dbHelper.getStuIDByEmail(email);

        // Setup real-time typing listener for the search bar
        etSearchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Setup drop-down option menu for sorting configuration rules
        btnFilter.setOnClickListener(this::showFilterPopupMenu);

        // Setup Tab Listener for 3 tabs
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTabPosition = tab.getPosition();
                loadListings();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadListings();
    }

    private void loadListings() {
        // Query SQLite database snapshots according to selected filter tab layout
        if (currentTabPosition == 0) {
            currentTabList = dbHelper.getListingsBySellerId(currentUserId);
        } else {
            String status = (currentTabPosition == 1) ? "Active" : "Sold";
            currentTabList = dbHelper.getListingsBySellerIdAndStatus(currentUserId, status);
        }

        // Reset adapter object tracker to clean view binding configurations when swapping datasets
        adapter = null;
        filterList(etSearchBar.getText().toString());
    }

    private void filterList(String query) {
        List<Listing> filteredList = new ArrayList<>();

        if (currentTabList != null) {
            for (Listing item : currentTabList) {
                // Character match search query (Case-insensitive)
                if (item.getTitle().toLowerCase().contains(query.toLowerCase().trim())) {
                    filteredList.add(item);
                }
            }
        }

        // Update list states and fallbacks visually
        if (!filteredList.isEmpty()) {
            rvListings.setVisibility(View.VISIBLE);
            findViewById(R.id.layout_empty).setVisibility(View.GONE);

            if (adapter == null) {
                adapter = new MyListingsAdapter(filteredList, this);
                rvListings.setAdapter(adapter);
            } else {
                adapter.updateList(filteredList); // Light-speed update without losing view positions
            }
        } else {
            rvListings.setVisibility(View.GONE);
            findViewById(R.id.layout_empty).setVisibility(View.VISIBLE);

            findViewById(R.id.btn_create_listing).setOnClickListener(v -> {
                startActivity(new Intent(this, CreateListingFragment.class));
            });
        }
    }

    private void showFilterPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenu().add("Sort by Title (A-Z)");
        popup.getMenu().add("Price: Low to High");
        popup.getMenu().add("Price: High to Low");

        popup.setOnMenuItemClickListener(item -> {
            if (currentTabList == null || currentTabList.isEmpty()) return false;

            String selectedOption = item.getTitle().toString();
            if ("Sort by Title (A-Z)".equals(selectedOption)) {
                Collections.sort(currentTabList, (o1, o2) -> o1.getTitle().compareToIgnoreCase(o2.getTitle()));
            } else if ("Price: Low to High".equals(selectedOption)) {
                Collections.sort(currentTabList, (o1, o2) -> Double.compare(extractPrice(o1.getPrice()), extractPrice(o2.getPrice())));
            } else if ("Price: High to Low".equals(selectedOption)) {
                Collections.sort(currentTabList, (o1, o2) -> Double.compare(extractPrice(o2.getPrice()), extractPrice(o1.getPrice())));
            }

            // Re-apply current text query matching to the newly arranged sequence layout
            filterList(etSearchBar.getText().toString());
            return true;
        });
        popup.show();
    }

    // Helper method to safely extract raw numbers out of price text strings (e.g., "RM 150.50" -> 150.50)
    private double extractPrice(String priceStr) {
        try {
            if (priceStr == null) return 0.0;
            String cleaned = priceStr.replaceAll("[^\\d.]", "");
            return cleaned.isEmpty() ? 0.0 : Double.parseDouble(cleaned);
        } catch (Exception e) {
            return 0.0;
        }
    }

    @Override
    public void onEdit(Listing listing) {
        Intent intent = new Intent(this, EditListingActivity.class);
        intent.putExtra("LISTING_ID", listing.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(Listing listing) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Listing")
                .setMessage("Are you sure you want to delete this?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteListing(listing.getId());
                    loadListings();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onMarkSold(Listing listing) {
        dbHelper.updateListingStatus(listing.getId(), "Sold");
        Toast.makeText(this, "Marked as sold!", Toast.LENGTH_SHORT).show();
        loadListings();
    }

    @Override
    public void onItemClick(Listing listing) {}
}