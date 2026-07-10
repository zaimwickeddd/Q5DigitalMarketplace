package com.example.q5digitalmarketplace;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class HomeExploreFragment extends Fragment implements ListingAdapter.OnListingActionListener {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;

    // UI Elements
    private TextView tvGreetingTitle;
    private EditText etSearchBar;

    // SOURCE OF TRUTH: All data is stored here and never overwritten
    private List<Listing> allListingsFromDb = new ArrayList<>();

    // Tracking state for combined filtering
    private String currentSearchQuery = "";
    private int currentFilterId = R.id.filter_all;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_explore, container, false);

        dbHelper = new DatabaseHelper(getContext());
        recyclerView = view.findViewById(R.id.rv_explore_items);
        tvGreetingTitle = view.findViewById(R.id.tv_greeting_title);
        etSearchBar = view.findViewById(R.id.search_bar);

        // 1. Layout Configuration
        int screenWidthDp = getResources().getConfiguration().screenWidthDp;
        int spanCount = (screenWidthDp >= 720) ? 4 : (screenWidthDp >= 600) ? 3 : 2;
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), spanCount));

        // 2. Fix Spacing: Add 16dp spacing between cards to solve the "cramped" UI
        int spacing = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.left = spacing / 2;
                outRect.right = spacing / 2;
                outRect.bottom = spacing;
                // Add top margin only to the first row
                if (parent.getChildAdapterPosition(view) < spanCount) outRect.top = spacing;
            }
        });

        // 3. Wrench Filter Button: Logic to open the navigation drawer
        view.findViewById(R.id.btn_open_filters_home).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).getDrawerLayout().openDrawer(GravityCompat.START);
            }
        });

        // Logic Initialization
        setupUserGreeting();
        setupSearchLogic();

        // UI Click Listeners
        view.findViewById(R.id.iv_notif_bell).setOnClickListener(v ->
                Toast.makeText(getContext(), "Notifications coming soon!", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.iv_fav_star).setOnClickListener(v ->
                Toast.makeText(getContext(), "Favorites coming soon!", Toast.LENGTH_SHORT).show());

        // Initial Data Load
        refreshDataCache();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Handle filter passed from MainActivity during navigation
        if (getArguments() != null && getArguments().containsKey("FILTER_CATEGORY_ID")) {
            int filterId = getArguments().getInt("FILTER_CATEGORY_ID");
            filterMarketplaceCategory(filterId);
        }
    }

    private void refreshDataCache() {
        this.allListingsFromDb = dbHelper.getAllListings();
        if (this.allListingsFromDb == null) this.allListingsFromDb = new ArrayList<>();
        applyFilters(); // Initial render
    }

    private void setupSearchLogic() {
        etSearchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString();
                applyFilters(); // Re-filter on every keystroke
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    // Public method called by MainActivity/Sidebar
    public void filterMarketplaceCategory(int filterId) {
        this.currentFilterId = filterId;

        // Clear search box when category changes
        if (etSearchBar != null) etSearchBar.setText("");
        currentSearchQuery = "";

        applyFilters();
    }

    // THE ENGINE: Combines Search + Category logic in memory
    private void applyFilters() {
        List<Listing> filteredList = new ArrayList<>();
        String categoryName = getCategoryNameFromId(currentFilterId);

        for (Listing item : allListingsFromDb) {
            boolean matchesSearch = (item.getTitle() != null && item.getTitle().toLowerCase().contains(currentSearchQuery.toLowerCase().trim()));
            boolean matchesCategory = (currentFilterId == R.id.filter_all || (item.getCategory() != null && item.getCategory().equalsIgnoreCase(categoryName)));

            if (matchesSearch && matchesCategory) {
                filteredList.add(item);
            }
        }

        // --- FIXED FOR STEP B MATCHING ---
        // 1. Get the current user email session from SharedPreferences
        String loggedInEmail = "";
        if (getActivity() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            loggedInEmail = prefs.getString("user_email", "");
        }

        // 2. Fetch the actual student ID row index integer
        int currentUserId = dbHelper.getStuIDByEmail(loggedInEmail);

        // 3. Instantiated using the revised 3-argument constructor format
        recyclerView.setAdapter(new ListingAdapter(filteredList, getContext(), currentUserId, this));

        // Note: If you updated your constructor to accept the click interface as a 4th argument, use this instead:
        // recyclerView.setAdapter(new ListingAdapter(filteredList, getContext(), currentUserId, this));
    }

    private String getCategoryNameFromId(int filterId) {
        if (filterId == R.id.filter_electronics) return "Electronics";
        if (filterId == R.id.filter_books) return "Books";
        if (filterId == R.id.filter_clothes) return "Clothes";
        if (filterId == R.id.filter_bags) return "Bags";
        if (filterId == R.id.filter_services) return "Services";
        return "All";
    }

    private void setupUserGreeting() {
        if (getActivity() == null) return;
        SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userName = dbHelper.getUserNameByEmail(prefs.getString("user_email", ""));

        if (userName != null && !userName.isEmpty()) {
            String formatted = userName.substring(0, 1).toUpperCase() + userName.substring(1);
            tvGreetingTitle.setText(getString(R.string.welcome_back, formatted));
        } else {
            tvGreetingTitle.setText(getString(R.string.welcome_guest));
        }
    }

    // ListingAdapter Click Listeners
    @Override
    public void onItemClick(Listing listing) {
        if (getContext() == null || listing == null) return;
        Intent intent = new Intent(getContext(), ListingDetailActivity.class);
        intent.putExtra("selected_listing", listing);
        startActivity(intent);
    }

    @Override public void onEdit(Listing listing) {}
    @Override public void onDelete(Listing listing) {}
    @Override public void onMarkSold(Listing listing) {}
}