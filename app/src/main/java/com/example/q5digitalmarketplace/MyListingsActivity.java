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
    private int currentTabPosition = 0;

    private EditText etSearchBar;
    private ImageButton btnFilter;
    private MyListingsAdapter adapter;
    private List<Listing> currentTabList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_listings);

        dbHelper = new DatabaseHelper(this);
        rvListings = findViewById(R.id.rv_my_listings);
        tabLayout = findViewById(R.id.tab_layout);
        rvListings.setLayoutManager(new LinearLayoutManager(this));

        etSearchBar = findViewById(R.id.et_search_bar);
        btnFilter = findViewById(R.id.btn_filter_list);

        // Bind click listener to the toolbar back navigation arrow element to prevent crashes
        View btnBackListings = findViewById(R.id.btn_back_listings);
        if (btnBackListings != null) {
            btnBackListings.setOnClickListener(v -> finish());
        }

        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String email = prefs.getString("user_email", "");
        currentUserId = dbHelper.getStuIDByEmail(email);

        etSearchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnFilter.setOnClickListener(this::showFilterPopupMenu);

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
        if (currentTabPosition == 0) {
            currentTabList = dbHelper.getListingsBySellerId(currentUserId);
        } else {
            String status = (currentTabPosition == 1) ? "Active" : "Sold";
            currentTabList = dbHelper.getListingsBySellerIdAndStatus(currentUserId, status);
        }

        adapter = null;
        filterList(etSearchBar.getText().toString());
    }

    private void filterList(String query) {
        List<Listing> filteredList = new ArrayList<>();

        if (currentTabList != null) {
            for (Listing item : currentTabList) {
                if (item.getTitle().toLowerCase().contains(query.toLowerCase().trim())) {
                    filteredList.add(item);
                }
            }
        }

        if (!filteredList.isEmpty()) {
            rvListings.setVisibility(View.VISIBLE);
            findViewById(R.id.layout_empty).setVisibility(View.GONE);

            if (adapter == null) {
                adapter = new MyListingsAdapter(filteredList, this);
                rvListings.setAdapter(adapter);
            } else {
                adapter.updateList(filteredList);
            }
        } else {
            rvListings.setVisibility(View.GONE);
            findViewById(R.id.layout_empty).setVisibility(View.VISIBLE);

            findViewById(R.id.btn_create_listing).setOnClickListener(v -> {
                finish();
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

            filterList(etSearchBar.getText().toString());
            return true;
        });
        popup.show();
    }

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
        // Security validation guard to prevent editing sold listings
        Listing freshRecord = dbHelper.getListingById(listing.getId());
        if (freshRecord != null && "Sold".equalsIgnoreCase(freshRecord.getStatus())) {
            Toast.makeText(this, "Sold items are archived and cannot be edited.", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(this, EditListingActivity.class);
        intent.putExtra("LISTING_ID", listing.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(Listing listing) {
        // 🛠️ FIXED: Added the explicit dark theme style parameter wrapper here to resolve the white text rendering bug
        new androidx.appcompat.app.AlertDialog.Builder(this, com.google.android.material.R.style.Theme_Material3_Dark_Dialog_Alert)
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
        // Added dark theme style wrapper parameter to ensure text visibility
        new androidx.appcompat.app.AlertDialog.Builder(this, com.google.android.material.R.style.Theme_Material3_Dark_Dialog_Alert)
                .setTitle("Mark as Sold")
                .setMessage("Are you sure you want to mark this item as sold? It will be moved to your 'Sold' tab.")
                .setPositiveButton("Mark Sold", (dialog, which) -> {
                    dbHelper.markItemAsSold(this, listing.getId(), listing.getTitle());
                    Toast.makeText(this, "Marked as sold!", Toast.LENGTH_SHORT).show();
                    loadListings();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onItemClick(Listing listing) {}
}