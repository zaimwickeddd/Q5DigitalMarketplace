package com.example.q5digitalmarketplace;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ListingDetailActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private String sellerPhone = "";
    private String sellerName = "Active Seller";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing_detail);

        dbHelper = new DatabaseHelper(this);

        // UI Components
        ImageView imgMain = findViewById(R.id.detail_image);
        TextView tvTitleTag = findViewById(R.id.detail_title_tag);
        TextView tvTitleMain = findViewById(R.id.detail_title_main);
        TextView tvPrice = findViewById(R.id.detail_price);
        TextView tvDescription = findViewById(R.id.detail_description);
        TextView tvSellerName = findViewById(R.id.tv_seller_name);

        TextView specCategory = findViewById(R.id.spec_category);
        TextView specCondition = findViewById(R.id.spec_condition);
        TextView specFaculty = findViewById(R.id.spec_faculty);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        Bundle extras = getIntent().getExtras();

        // Final variables for lambda compliance
        final String title;
        final String price;
        final String category;
        final String condition;
        final String faculty;
        final String description;
        final String imagePath;
        int itemId = -1;

        if (extras != null) {
            itemId = extras.getInt("id", -1);
            title = extras.getString("title", "Item Name");
            price = extras.getString("price", "RM --");
            category = extras.getString("category", "General");
            condition = extras.getString("condition", "Used");
            faculty = extras.getString("faculty", "General");
            imagePath = extras.getString("image_path", "");
            description = extras.getString("description", "No description provided.");
        } else {
            title = "Item Name";
            price = "RM --";
            category = "General";
            condition = "Used";
            faculty = "General";
            imagePath = "";
            description = "No description provided.";
        }

        tvTitleMain.setText(title);
        tvTitleTag.setText(category.toUpperCase());
        tvPrice.setText(price);
        tvDescription.setText(description);

        specCategory.setText(category);
        specCondition.setText(condition);
        specFaculty.setText(faculty);

        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                imgMain.setImageURI(Uri.parse(imagePath));
            } catch (Exception e) {
                imgMain.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            imgMain.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        if (itemId != -1) {
            Cursor cursor = dbHelper.getListingWithSellerPhone(itemId);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex("Name");
                    int phoneIndex = cursor.getColumnIndex("PhoneNum");

                    if (nameIndex != -1) {
                        sellerName = cursor.getString(nameIndex);
                        if (tvSellerName != null) tvSellerName.setText(sellerName);
                    }

                    if (phoneIndex != -1) {
                        sellerPhone = cursor.getString(phoneIndex);
                    }
                }
                cursor.close();
            }
        }

        // WhatsApp Deep Linking Action
        findViewById(R.id.btn_whatsapp).setOnClickListener(v -> {
            if (sellerPhone != null && !sellerPhone.trim().isEmpty()) {
                String cleanNumber = sellerPhone.replaceAll("[\\s+\\-]", "");
                if (cleanNumber.startsWith("0")) cleanNumber = "6" + cleanNumber;

                String messageBody = "Hello " + sellerName + "! I saw your listing for '" + title + "' on the student marketplace.";
                String apiUri = "https://wa.me/" + cleanNumber + "?text=" + Uri.encode(messageBody);

                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(apiUri)));
                } catch (Exception e) {
                    Toast.makeText(this, "Unable to launch WhatsApp.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Seller phone number not found.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}