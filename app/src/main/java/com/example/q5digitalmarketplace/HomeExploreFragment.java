package com.example.q5digitalmarketplace;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HomeExploreFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private ListingAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_explore, container, false);

        dbHelper = new DatabaseHelper(getContext());
        TextView tvGreetingTitle = view.findViewById(R.id.tv_greeting_title);

        String loggedInEmail = null;
        if (getActivity() != null && getActivity().getIntent() != null) {
            loggedInEmail = getActivity().getIntent().getStringExtra("USER_EMAIL");
        }

        String displayName = (loggedInEmail != null && !loggedInEmail.isEmpty())
                ? dbHelper.getUserNameByEmail(loggedInEmail) : "Guest";

        if (tvGreetingTitle != null) {
            tvGreetingTitle.setText(String.format("Welcome Back,\n%s 👋", displayName));
        }

        recyclerView = view.findViewById(R.id.rv_explore_items);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Check if this fragment was launched from another page with a pre-selected category filter
        int preSelectedCategoryId = (getArguments() != null) ? getArguments().getInt("FILTER_CATEGORY_ID", -1) : -1;

        if (preSelectedCategoryId != -1) {
            filterMarketplaceCategory(preSelectedCategoryId);
        } else {
            loadDefaultData();
        }

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() != null) {
            DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);
            View btnOpenFiltersHome = view.findViewById(R.id.btn_open_filters_home);

            if (btnOpenFiltersHome != null && drawerLayout != null) {
                btnOpenFiltersHome.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
            }
        }
    }

    private void loadDefaultData() {
        List<Listing> campusItemsList = dbHelper.getAllListings();
        if (campusItemsList.isEmpty()) {
            insertInitialData();
            campusItemsList = dbHelper.getAllListings();
        }
        adapter = new ListingAdapter(campusItemsList);
        recyclerView.setAdapter(adapter);
    }

    // Public method accessible by MainActivity to update the product feed immediately
    public void filterMarketplaceCategory(int menuItemId) {
        if (dbHelper == null) return;

        List<Listing> filteredItemsList;
        if (menuItemId == R.id.cat_all) {
            filteredItemsList = dbHelper.getAllListings();
        } else if (menuItemId == R.id.cat_electronics) {
            filteredItemsList = dbHelper.getListingsByCategory("Electronics");
        } else if (menuItemId == R.id.cat_books) {
            filteredItemsList = dbHelper.getListingsByCategory("Books");
        } else if (menuItemId == R.id.cat_clothes) {
            filteredItemsList = dbHelper.getListingsByCategory("Clothes");
        } else if (menuItemId == R.id.cat_bags) {
            filteredItemsList = dbHelper.getListingsByCategory("Bags");
        } else {
            filteredItemsList = dbHelper.getAllListings();
        }

        adapter = new ListingAdapter(filteredItemsList);
        if (recyclerView != null) {
            recyclerView.setAdapter(adapter);
        }
    }

    private void insertInitialData() {
        dbHelper.insertListing(new Listing("Lenovo LOQ Laptop", "RM 1,500", "Electronics", "Used", "lenovo_loq", "Reliable entry-level gaming laptop. Used for assignments only."));
        dbHelper.insertListing(new Listing("Introductory Mandarin Book", "RM 30", "Books", "Brand New", "mandarin_books", "Excellent reference textbook with full exercises and basic conversational guides."));
        dbHelper.insertListing(new Listing("Cabana Vintage Shirt", "RM 50", "Clothes", "Used", "canaba_shirt", "Comfortable casual wear shirt. Good condition with zero faded spots."));
        dbHelper.insertListing(new Listing("Adidas Originals Backpack", "RM 129", "Bags", "Brand New", "adidas_backpack", "Spacious main compartment. Brand new item received as an unneeded gift."));
    }
}