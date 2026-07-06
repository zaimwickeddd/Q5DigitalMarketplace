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
import java.util.List;

public class ExploreItemsFragment extends Fragment {

    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore_items, container, false);

        dbHelper = new DatabaseHelper(getContext());

        // Setup Grid List View
        RecyclerView recyclerView = view.findViewById(R.id.rv_explore_items_list);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        List<Listing> itemsList = dbHelper.getAllListings();
        ListingAdapter adapter = new ListingAdapter(itemsList);
        recyclerView.setAdapter(adapter);

        // ==================== STEP 3: GLOBAL SIDEBAR INTEGRATION ====================

        // Connect the blue button directly to the master layout wrapper inside MainActivity
        if (getActivity() != null) {
            // Find the global activity-level DrawerLayout wrapper instead of fragment-level right drawers
            DrawerLayout globalDrawerLayout = getActivity().findViewById(R.id.drawer_layout);
            View btnOpenFilters = view.findViewById(R.id.btn_open_filters);

            if (btnOpenFilters != null && globalDrawerLayout != null) {
                // Change GravityCompat.END to GravityCompat.START to match your primary left-hand side menu
                btnOpenFilters.setOnClickListener(v -> globalDrawerLayout.openDrawer(GravityCompat.START));
            }
        }

        // ============================================================================

        return view;
    }

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
}