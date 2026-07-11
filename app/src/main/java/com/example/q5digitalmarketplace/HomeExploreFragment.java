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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class HomeExploreFragment extends Fragment implements ListingAdapter.OnListingActionListener {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;

    // UI Elements
    private TextView tvGreetingTitle;
    private EditText etSearchBar;
    private View notifBadge;

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
        notifBadge = view.findViewById(R.id.notif_badge);

        // 1. Layout Configuration: Changed to 1 column for a vertical list layout as requested
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));

        // 2. Spacing Logic: Vertical spacing between the cards - Increased for better visibility
        int verticalSpacing = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                // We only add bottom spacing as the XML handles horizontal margins
                outRect.bottom = verticalSpacing;
                if (parent.getChildAdapterPosition(view) == 0) outRect.top = verticalSpacing;
            }
        });

        // 3. Wrench Filter Button: Logic to open the navigation drawer
        View btnFilters = view.findViewById(R.id.btn_settings);
        if (btnFilters != null) {
            btnFilters.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).getDrawerLayout().openDrawer(GravityCompat.START);
                }
            });
        }

        // Logic Initialization
        setupUserGreeting();
        setupSearchLogic();

        // 🛠️ UPDATED: The notification bell now navigates to the NotificationsFragment
        View ivNotifBell = view.findViewById(R.id.btn_notif);
        if (ivNotifBell != null) {
            ivNotifBell.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new NotificationsFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        // FIXED: The top toolbar star icon now loads FavoritesFragment and syncs bottom navigation selection
        View ivFavStar = view.findViewById(R.id.btn_favs);
        if (ivFavStar != null) {
            ivFavStar.setOnClickListener(v -> {
                if (getActivity() != null) {
                    // 1. Swap current container to the Favorites page fragment
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new FavoritesFragment())
                            .commit();
                }
            });
        }

        // Initial Data Load
        refreshDataCache();
        updateNotificationBadge();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshDataCache();
        updateNotificationBadge();
    }

    private void updateNotificationBadge() {
        if (notifBadge == null) return;
        
        SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String email = prefs.getString("user_email", null);
        
        if (email != null) {
            int userId = dbHelper.getStuIDByEmail(email);
            int unreadCount = dbHelper.getUnreadNotificationsCount(userId);
            
            if (unreadCount > 0) {
                notifBadge.setVisibility(View.VISIBLE);
            } else {
                notifBadge.setVisibility(View.GONE);
            }
        } else {
            notifBadge.setVisibility(View.GONE);
        }
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
        if (etSearchBar != null) {
            etSearchBar.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchQuery = s.toString();
                    applyFilters(); // Re-filter on every keystroke
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
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

        // 1. Get the current user email session from SharedPreferences
        String loggedInEmail = "";
        if (getActivity() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            loggedInEmail = prefs.getString("user_email", "");
        }

        // 2. Fetch the actual student ID row index integer
        int currentUserId = dbHelper.getStuIDByEmail(loggedInEmail);

        // 3. FIXED: Properly matching the updated 4-argument constructor signature cleanly
        if (recyclerView != null) {
            recyclerView.setAdapter(new ListingAdapter(filteredList, getContext(), currentUserId, this));
        }
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
        if (getActivity() == null || tvGreetingTitle == null) return;
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
