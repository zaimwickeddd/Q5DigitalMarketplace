package com.example.q5digitalmarketplace;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MyListingsActivity extends AppCompatActivity {

    private RecyclerView rvListings;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_listings);

        dbHelper = new DatabaseHelper(this);
        rvListings = findViewById(R.id.rv_my_listings);
        rvListings.setLayoutManager(new LinearLayoutManager(this));

        loadListings();
    }

    private void loadListings() {
        // PASS the user ID (1) to the helper method to fix the compilation error
        int currentUserId = 1;
        Cursor cursor = dbHelper.getMyListings(currentUserId);

        if (cursor != null && cursor.getCount() > 0) {
            // Setup your Adapter here
            // MyListingsAdapter adapter = new MyListingsAdapter(this, cursor);
            // rvListings.setAdapter(adapter);

            rvListings.setVisibility(View.VISIBLE);
            findViewById(R.id.layout_empty).setVisibility(View.GONE);
        } else {
            rvListings.setVisibility(View.GONE);
            findViewById(R.id.layout_empty).setVisibility(View.VISIBLE);

            findViewById(R.id.btn_create_listing).setOnClickListener(v -> {
                // Add your navigation to the Create Listing Activity here
            });
        }
        if (cursor != null) cursor.close();
    }
}