package com.example.q5digitalmarketplace;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment implements ListingAdapter.OnListingActionListener {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private ListingAdapter adapter;
    private List<Listing> favoriteListings = new ArrayList<>();
    private int currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        dbHelper = new DatabaseHelper(getContext());
        recyclerView = view.findViewById(R.id.rv_favorite_items);

        // 1. Get current logged-in user profile identity metrics safely
        if (getActivity() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            String loggedInEmail = prefs.getString("user_email", "");
            currentUserId = dbHelper.getStuIDByEmail(loggedInEmail);
        }

        // 2. Setup Single Column Vertical List matching modernized layout theme
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));

        // 3. Spacing Decorator for list items
        int spacing = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.bottom = spacing;
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Load or update data every time the page is brought back into view focus
        loadFavorites();
    }

    private void loadFavorites() {
        // Query only user-pinned rows via the SQLite JOIN engine method
        favoriteListings = dbHelper.getWishlistListings(currentUserId);

        if (favoriteListings == null) {
            favoriteListings = new ArrayList<>();
        }

        // Instantiate adapter using the precise 4-argument constructor contract
        adapter = new ListingAdapter(favoriteListings, getContext(), currentUserId, this);
        if (recyclerView != null) {
            recyclerView.setAdapter(adapter);
        }
    }

    // ListingAdapter Action Contract Implementations
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