package com.example.q5digitalmarketplace;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        dbHelper = new DatabaseHelper(this);
        layoutBarsList = findViewById(R.id.layoutBarsList);
        tvNoData = findViewById(R.id.tvNoData);
        pieChartView = findViewById(R.id.pieChartView);
        layoutPieLegend = findViewById(R.id.layoutPieLegend);

        // 1. Render the Horizontal Bar Chart (WhatsApp click metrics)
        renderCategoryChart();

        // 2. Render the Pie Chart (Listing distribution metrics)
        renderPieChartDistribution();

        // 3. Setup back button action router navigation rule
        findViewById(R.id.btn_back_analytics).setOnClickListener(v -> {
            finish(); // Closes this activity window and pops back to the Profile tab screen cleanly
        });
    }

    private void renderCategoryChart() {
        layoutBarsList.removeAllViews();
        Cursor cursor = dbHelper.getCategoryAnalyticsData();

        if (cursor == null || cursor.getCount() == 0) {
            tvNoData.setVisibility(View.VISIBLE);
            if (cursor != null) cursor.close();
            return;
        }

        tvNoData.setVisibility(View.GONE);

        // Find the highest click count among categories to keep the bars proportional
        int maxClicks = 1;
        if (cursor.moveToFirst()) {
            maxClicks = cursor.getInt(1); // The top category will always be first due to DESC sorting
        }
        cursor.moveToPosition(-1); // Reset cursor position

        // Build the chart rows dynamically
        while (cursor.moveToNext()) {
            String categoryName = cursor.getString(0);
            int totalCategoryClicks = cursor.getInt(1);

            // Create row layout for this category
            LinearLayout rowContainer = new LinearLayout(this);
            rowContainer.setOrientation(LinearLayout.VERTICAL);
            rowContainer.setPadding(0, 8, 0, 16);

            // Text label: e.g., "Electronics (45 clicks)"
            TextView tvLabel = new TextView(this);
            tvLabel.setText(categoryName + " (" + totalCategoryClicks + " clicks)");

            // 🛠️ FIXED: Changed from dark "#1A1C1E" to light "#F9FAFB" for complete visibility
            tvLabel.setTextColor(Color.parseColor("#F9FAFB"));
            tvLabel.setTextSize(14);
            rowContainer.addView(tvLabel);

            // Proportional horizontal visualization bar
            View viewBar = new View(this);
            float ratio = (float) totalCategoryClicks / maxClicks;
            int barWidth = Math.max((int) (ratio * 600), 25); // Scale bar width up to 600 pixels max

            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(barWidth, 40);
            barParams.topMargin = 6;
            viewBar.setLayoutParams(barParams);
            viewBar.setBackgroundColor(Color.parseColor("#34A853")); // WhatsApp Green color theme

            rowContainer.addView(viewBar);
            layoutBarsList.addView(rowContainer);
        }
        cursor.close();
    }

    private void renderPieChartDistribution() {
        layoutPieLegend.removeAllViews();
        Cursor cursor = dbHelper.getItemDistributionCount();

        if (cursor == null || cursor.getCount() == 0) {
            if (cursor != null) cursor.close();
            return;
        }

        List<Float> values = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        // A collection of clean material design chart colors
        int[] chartPalette = {
                Color.parseColor("#0061A4"), // Deep Blue
                Color.parseColor("#34A853"), // Emerald Green
                Color.parseColor("#FBBC05"), // Amber Yellow
                Color.parseColor("#EA4335"), // Coral Red
                Color.parseColor("#8E44AD")  // Amethyst Purple
        };

        int colorIndex = 0;
        while (cursor.moveToNext()) {
            String category = cursor.getString(0);
            int itemCount = cursor.getInt(1);

            values.add((float) itemCount);
            int chosenColor = chartPalette[colorIndex % chartPalette.length];
            colors.add(chosenColor);

            // Build a visual descriptive legend row dynamically
            LinearLayout legendItem = new LinearLayout(this);
            legendItem.setOrientation(LinearLayout.HORIZONTAL);
            legendItem.setGravity(android.view.Gravity.CENTER_VERTICAL);
            legendItem.setPadding(0, 4, 0, 4);

            // Small square color block indicator
            View colorBlock = new View(this);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(30, 30);
            p.setMargins(0, 0, 16, 0);
            colorBlock.setLayoutParams(p);
            colorBlock.setBackgroundColor(chosenColor);
            legendItem.addView(colorBlock);

            // Label details text
            TextView tvLegend = new TextView(this);
            tvLegend.setText(category + ": " + itemCount + " items total");

            // 🛠️ FIXED: Changed from dark "#1A1C1E" to light "#F9FAFB" for complete visibility
            tvLegend.setTextColor(Color.parseColor("#F9FAFB"));
            tvLegend.setTextSize(13);
            legendItem.addView(tvLegend);

            layoutPieLegend.addView(legendItem);
            colorIndex++;
        }
        cursor.close();

        // Push datasets to render onto the custom view canvas surface
        pieChartView.setData(values, colors);
    }
}