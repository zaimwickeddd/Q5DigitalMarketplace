package com.example.q5digitalmarketplace;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AnalyticsActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private LinearLayout layoutBarsList;
    private TextView tvNoData;
    private PieChartView pieChartView;
    private LinearLayout layoutPieLegend;
    private LinearLayout layoutItemPerformance;
    private int currentUserId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        dbHelper = new DatabaseHelper(this);
        layoutBarsList = findViewById(R.id.layoutBarsList);
        tvNoData = findViewById(R.id.tvNoData);
        pieChartView = findViewById(R.id.pieChartView);
        layoutPieLegend = findViewById(R.id.layoutPieLegend);
        layoutItemPerformance = findViewById(R.id.layoutItemPerformance);

        // 🛠️ Fetch the current logged-in user ID to filter analytics data
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String loggedInEmail = prefs.getString("user_email", null);
        if (loggedInEmail != null) {
            currentUserId = dbHelper.getStuIDByEmail(loggedInEmail);
        }

        // 1. Render the Horizontal Bar Chart (WhatsApp click metrics for specific seller)
        renderCategoryChart();

        // 2. Render the Pie Chart (Listing distribution metrics for specific seller)
        renderPieChartDistribution();

        // 3. Render the Individual Item Performance List
        renderItemPerformance();

        // 4. Setup back button action router navigation rule
        findViewById(R.id.btn_back_analytics).setOnClickListener(v -> {
            finish(); // Closes this activity window and pops back to the Profile tab screen cleanly
        });
    }

    private void renderCategoryChart() {
        if (layoutBarsList == null) return;
        layoutBarsList.removeAllViews();
        
        // 🛠️ Filter results by current user's seller ID
        Cursor cursor = dbHelper.getCategoryAnalyticsDataBySeller(currentUserId);

        if (cursor == null || cursor.getCount() == 0) {
            tvNoData.setVisibility(View.VISIBLE);
            if (cursor != null) cursor.close();
            return;
        }

        tvNoData.setVisibility(View.GONE);

        int maxClicks = 1;
        if (cursor.moveToFirst()) {
            maxClicks = cursor.getInt(1); 
        }
        cursor.moveToPosition(-1); 

        while (cursor.moveToNext()) {
            String categoryName = cursor.getString(0);
            int totalCategoryClicks = cursor.getInt(1);

            LinearLayout rowContainer = new LinearLayout(this);
            rowContainer.setOrientation(LinearLayout.VERTICAL);
            rowContainer.setPadding(0, 8, 0, 16);

            TextView tvLabel = new TextView(this);
            tvLabel.setText(categoryName + " (" + totalCategoryClicks + " clicks)");
            tvLabel.setTextColor(Color.parseColor("#F9FAFB"));
            tvLabel.setTextSize(14);
            rowContainer.addView(tvLabel);

            View viewBar = new View(this);
            float ratio = (float) totalCategoryClicks / maxClicks;
            int barWidth = Math.max((int) (ratio * 600), 25); 

            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(barWidth, 40);
            barParams.topMargin = 6;
            viewBar.setLayoutParams(barParams);
            viewBar.setBackgroundColor(Color.parseColor("#34A853")); 

            rowContainer.addView(viewBar);
            layoutBarsList.addView(rowContainer);
        }
        cursor.close();
    }

    private void renderPieChartDistribution() {
        if (layoutPieLegend == null) return;
        layoutPieLegend.removeAllViews();
        
        // 🛠️ Filter results by current user's seller ID
        Cursor cursor = dbHelper.getItemDistributionCountBySeller(currentUserId);

        if (cursor == null || cursor.getCount() == 0) {
            if (cursor != null) cursor.close();
            return;
        }

        List<Float> values = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        int[] chartPalette = {
                Color.parseColor("#0061A4"), 
                Color.parseColor("#34A853"), 
                Color.parseColor("#FBBC05"), 
                Color.parseColor("#EA4335"), 
                Color.parseColor("#8E44AD")  
        };

        int colorIndex = 0;
        while (cursor.moveToNext()) {
            String category = cursor.getString(0);
            int itemCount = cursor.getInt(1);

            values.add((float) itemCount);
            int chosenColor = chartPalette[colorIndex % chartPalette.length];
            colors.add(chosenColor);

            LinearLayout legendItem = new LinearLayout(this);
            legendItem.setOrientation(LinearLayout.HORIZONTAL);
            legendItem.setGravity(android.view.Gravity.CENTER_VERTICAL);
            legendItem.setPadding(0, 4, 0, 4);

            View colorBlock = new View(this);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(30, 30);
            p.setMargins(0, 0, 16, 0);
            colorBlock.setLayoutParams(p);
            colorBlock.setBackgroundColor(chosenColor);
            legendItem.addView(colorBlock);

            TextView tvLegend = new TextView(this);
            tvLegend.setText(category + ": " + itemCount + " items");
            tvLegend.setTextColor(Color.parseColor("#F9FAFB"));
            tvLegend.setTextSize(13);
            legendItem.addView(tvLegend);

            layoutPieLegend.addView(legendItem);
            colorIndex++;
        }
        cursor.close();

        pieChartView.setData(values, colors);
    }

    private void renderItemPerformance() {
        if (layoutItemPerformance == null) return;
        layoutItemPerformance.removeAllViews();

        // 🛠️ Fetch clicks for each individual item owned by this user
        Cursor cursor = dbHelper.getItemPerformanceDataBySeller(currentUserId);

        if (cursor == null || cursor.getCount() == 0) {
            if (cursor != null) cursor.close();
            return;
        }

        while (cursor.moveToNext()) {
            String itemTitle = cursor.getString(0);
            int clicks = cursor.getInt(1);

            // 🛠️ FIXED: Pass layoutItemPerformance as parent to ensure correct LayoutParams resolution
            View itemView = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, layoutItemPerformance, false);
            TextView text1 = itemView.findViewById(android.R.id.text1);
            TextView text2 = itemView.findViewById(android.R.id.text2);

            text1.setText(itemTitle);
            text1.setTextColor(Color.WHITE);
            text2.setText(clicks + " WhatsApp clicks");
            text2.setTextColor(Color.parseColor("#9CA3AF"));

            layoutItemPerformance.addView(itemView);
        }
        cursor.close();
    }
}
