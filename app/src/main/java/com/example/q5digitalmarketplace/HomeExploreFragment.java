package com.example.q5digitalmarketplace;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HomeExploreFragment extends Fragment {

    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_explore, container, false);

        dbHelper = new DatabaseHelper(getContext());
        RecyclerView recyclerView = view.findViewById(R.id.rv_explore_items);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // 1. Fetch live records from local SQLite DB
        List<Listing> campusItemsList = dbHelper.getAllListings();

        // 2. Pre-populate default listings on first run so the app isn't empty for your lecturer
        if (campusItemsList.isEmpty()) {
            insertInitialData();
            campusItemsList = dbHelper.getAllListings(); // Re-fetch after filling
        }

        // 3. Bind UI elements dynamically
        ListingAdapter adapter = new ListingAdapter(campusItemsList);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void insertInitialData() {
        dbHelper.insertListing(new Listing("Lenovo LOQ Laptop", "RM 1,500", "Electronics", "Used", android.R.drawable.ic_menu_compass));
        dbHelper.insertListing(new Listing("Introductory Mandarin Book", "RM 30", "Books", "Brand New", android.R.drawable.ic_menu_help));
        dbHelper.insertListing(new Listing("Cabana Vintage Shirt", "RM 50", "Clothes", "Used", android.R.drawable.ic_menu_gallery));
        dbHelper.insertListing(new Listing("Adidas Originals Backpack", "RM 129", "Bags", "Brand New", android.R.drawable.ic_menu_save));
    }
}