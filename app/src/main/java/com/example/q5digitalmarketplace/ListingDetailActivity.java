package com.example.q5digitalmarketplace;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ListingDetailActivity extends AppCompatActivity {

    private static final String TAG = "ListingDetailActivity";
    private DatabaseHelper dbHelper;

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

        // 🛠️ BOUND: Reference to the seller's avatar layout image placeholder frame
        ImageView ivSellerAvatar = findViewById(R.id.iv_seller_avatar);

        TextView specCategory = findViewById(R.id.spec_category);
        TextView specCondition = findViewById(R.id.spec_condition);
        TextView specFaculty = findViewById(R.id.spec_faculty);
        TextView specType = findViewById(R.id.spec_type);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Baseline tracking data variables
        String sellerPhone = "";
        String sellerName = "Active Seller";

        int itemId = -1;
        String title = "Item Name";
        String price = "RM --";
        String category = "General";
        String condition = "Used";
        String faculty = "General";
        String type = "Buy";
        String imagePath = "";
        String description = "No description provided.";

        // Extract the listing object sent by the HomeExploreFragment
        Listing listing = (Listing) getIntent().getSerializableExtra("selected_listing");

        if (listing != null) {
            itemId = listing.getId();
            title = listing.getTitle() != null ? listing.getTitle() : "Item Name";

            // Format Price safely to eliminate potential "RM RM" display glitches
            String rawPrice = String.valueOf(listing.getPrice()).trim();
            if (rawPrice.toUpperCase().startsWith("RM")) {
                price = rawPrice;
            } else {
                price = getString(R.string.price_format, rawPrice);
            }

            category = listing.getCategory() != null ? listing.getCategory() : "General";
            condition = listing.getCondition() != null ? listing.getCondition() : "Used";
            faculty = listing.getFaculty() != null ? listing.getFaculty() : "General";
            imagePath = listing.getImagePath() != null ? listing.getImagePath() : "";
            description = listing.getDescription() != null ? listing.getDescription() : "No description provided.";
            type = listing.getType() != null ? listing.getType() : "Buy";

            Log.d(TAG, "Successfully processed Object Item ID: " + itemId);
        } else {
            Log.w(TAG, "Object extra was null, checking for legacy key fallbacks...");
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                itemId = extras.getInt("id", -1);
                title = extras.getString("title", "Item Name");
                price = extras.getString("price", "RM --");
                category = extras.getString("category", "General");
                condition = extras.getString("condition", "Used");
                faculty = extras.getString("faculty", "General");
                type = extras.getString("listing_type", "Buy");
                imagePath = extras.getString("image_path", "");
                description = extras.getString("description", "No description provided.");
            }
        }

        // Set UI Text Elements
        tvTitleMain.setText(title);
        tvTitleTag.setText(type.toUpperCase());
        tvPrice.setText(price);
        tvDescription.setText(description);
        specCategory.setText(category);
        specCondition.setText(condition);
        specFaculty.setText(faculty);
        if (specType != null) specType.setText(type);

        // Process Database Image Strings safely
        if (imgMain != null) {
            if (imagePath != null && !imagePath.trim().isEmpty()) {
                String cleanImg = imagePath.trim();
                try {
                    // 1. CHECK FOR SEEDED DRAWABLE ASSET STRINGS FIRST (e.g., "lenovo_laptop")
                    int imageResId = getResources().getIdentifier(cleanImg, "drawable", getPackageName());

                    if (imageResId != 0) {
                        imgMain.setImageResource(imageResId);
                    }
                    // 2. FALLBACK TO URI PATH IDENTIFIERS (Gallery uploads)
                    else if (cleanImg.startsWith("content://") || cleanImg.startsWith("file://") || cleanImg.startsWith("/")) {
                        imgMain.setImageURI(Uri.parse(cleanImg));
                    }
                    // 3. FALLBACK TO BASE64 DECODING SCHEMES
                    else {
                        if (cleanImg.contains(",")) {
                            cleanImg = cleanImg.substring(cleanImg.indexOf(",") + 1);
                        }
                        byte[] decodedBytes = Base64.decode(cleanImg, Base64.DEFAULT);
                        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                        if (decodedBitmap != null) {
                            imgMain.setImageBitmap(decodedBitmap);
                        } else {
                            imgMain.setImageResource(android.R.drawable.ic_menu_gallery);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error rendering image asset path", e);
                    imgMain.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } else {
                imgMain.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        // Fetch Seller Info from SQLite via database layer queries
        if (itemId != -1) {
            Cursor cursor = dbHelper.getListingWithSellerPhone(itemId);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex("Name");
                    int phoneIndex = cursor.getColumnIndex("PhoneNum");
                    int sellerIdIndex = cursor.getColumnIndex("seller_id"); // 🛠️ EXTRACTED: Reference key to track the item's author

                    if (nameIndex != -1) sellerName = cursor.getString(nameIndex);
                    if (phoneIndex != -1) sellerPhone = cursor.getString(phoneIndex);

                    if (tvSellerName != null) tvSellerName.setText(sellerName);
                    Log.d(TAG, "Seller found: " + sellerName + " | Phone: " + sellerPhone);

                    // 🛠️ FIXED: DYNAMIC SMART PARSER FOR SELLER AVATAR DISPLAY
                    if (sellerIdIndex != -1 && ivSellerAvatar != null) {
                        int sellerId = cursor.getInt(sellerIdIndex);

                        // Query the student profile directly from SQLite using the seller ID context
                        Cursor profileCursor = dbHelper.getReadableDatabase().rawQuery(
                                "SELECT ProfileImage FROM Student WHERE StuID = ?",
                                new String[]{String.valueOf(sellerId)});

                        if (profileCursor != null) {
                            if (profileCursor.moveToFirst()) {
                                String imgPath = profileCursor.getString(0);
                                if (imgPath != null && !imgPath.trim().isEmpty()) {
                                    String cleanProfileImg = imgPath.trim();

                                    // Verify if the value matches a hardcoded local drawable resource string name
                                    int resId = getResources().getIdentifier(cleanProfileImg, "drawable", getPackageName());
                                    if (resId != 0) {
                                        ivSellerAvatar.setImageResource(resId);
                                    } else {
                                        // Fallback: Parse string path structure as a local device device content URI
                                        ivSellerAvatar.setImageURI(Uri.parse(cleanProfileImg));
                                    }
                                } else {
                                    // Default fallback image frame if the data path layout token is blank
                                    ivSellerAvatar.setImageResource(android.R.drawable.ic_menu_gallery);
                                }
                            }
                            profileCursor.close();
                        }
                    }
                } else {
                    Log.e(TAG, "Cursor empty for Item ID: " + itemId);
                }
                cursor.close();
            }
        }

        // Prepare final variable bounds for inner lambda use
        final int finalItemId = itemId;
        final String finalTitle = title;
        final String finalType = type;
        final String finalSellerName = sellerName;
        final String finalSellerPhone = sellerPhone;

        // WhatsApp Intent Action Integration
        findViewById(R.id.btn_whatsapp).setOnClickListener(v -> {
            if (finalItemId != -1) {
                dbHelper.incrementWhatsAppClicks(finalItemId);
            }

            if (finalSellerPhone != null && !finalSellerPhone.trim().isEmpty()) {
                String cleanNumber = finalSellerPhone.replaceAll("[^0-9]", "");
                if (cleanNumber.startsWith("0")) cleanNumber = "6" + cleanNumber;

                String messageBody = "Hello " + finalSellerName + "! I am interested in your " + finalType + " listing: '" + finalTitle + "'.";
                String apiUri = "https://wa.me/" + cleanNumber + "?text=" + Uri.encode(messageBody);

                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(apiUri)));
                } catch (Exception e) {
                    Toast.makeText(this, "WhatsApp not installed.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Seller phone number not found.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}