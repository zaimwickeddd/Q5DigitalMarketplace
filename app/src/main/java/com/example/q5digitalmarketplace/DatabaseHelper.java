package com.example.q5digitalmarketplace;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Q5Marketplace.db";
    private static final int DATABASE_VERSION = 19; // Incremented for schema change

    public static final String TABLE_LISTINGS = "listings";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_CONDITION = "condition";
    public static final String COLUMN_IMAGE_RES = "image_resource_id";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_FACULTY = "faculty";
    public static final String COLUMN_SELLER_ID = "seller_id";
    public static final String COLUMN_TYPE = "listing_type";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Student (StuID INTEGER PRIMARY KEY AUTOINCREMENT, Name TEXT, Email TEXT UNIQUE, PhoneNum TEXT, Password TEXT, UserType TEXT);");
        db.execSQL("CREATE TABLE " + TABLE_LISTINGS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TITLE + " TEXT, " + COLUMN_PRICE + " TEXT, " + COLUMN_CATEGORY + " TEXT, "
                + COLUMN_CONDITION + " TEXT, " + COLUMN_IMAGE_RES + " TEXT, " + COLUMN_DESCRIPTION + " TEXT, "
                + COLUMN_FACULTY + " TEXT, " + COLUMN_SELLER_ID + " INTEGER, status TEXT, " + COLUMN_TYPE + " TEXT, "
                + "FOREIGN KEY(" + COLUMN_SELLER_ID + ") REFERENCES Student(StuID));");

        db.execSQL("CREATE TABLE Buyer (BuyerID INTEGER PRIMARY KEY, BuyerBio TEXT, Date_Joined TEXT, FOREIGN KEY(BuyerID) REFERENCES Student(StuID));");
        db.execSQL("CREATE TABLE Seller (SellerID INTEGER PRIMARY KEY, StoreBio TEXT, FOREIGN KEY(SellerID) REFERENCES Student(StuID));");
        db.execSQL("CREATE TABLE Wishlist (WishListID INTEGER PRIMARY KEY AUTOINCREMENT, Date_Added TEXT, BuyerID INTEGER, ItemID INTEGER, FOREIGN KEY(BuyerID) REFERENCES Buyer(BuyerID), FOREIGN KEY(ItemID) REFERENCES " + TABLE_LISTINGS + "(" + COLUMN_ID + "));");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LISTINGS);
        db.execSQL("DROP TABLE IF EXISTS Wishlist");
        db.execSQL("DROP TABLE IF EXISTS Seller");
        db.execSQL("DROP TABLE IF EXISTS Buyer");
        db.execSQL("DROP TABLE IF EXISTS Student");
        onCreate(db);
    }

    // --- Listing Operations ---

    public boolean insertListing(Listing listing) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, listing.getTitle());
        values.put(COLUMN_PRICE, listing.getPrice());
        values.put(COLUMN_CATEGORY, listing.getCategory());
        values.put(COLUMN_CONDITION, listing.getCondition());
        values.put(COLUMN_IMAGE_RES, listing.getImagePath());
        values.put(COLUMN_DESCRIPTION, listing.getDescription());
        values.put(COLUMN_FACULTY, listing.getFaculty());
        values.put(COLUMN_SELLER_ID, listing.getSellerId());
        values.put("status", "Active");
        values.put(COLUMN_TYPE, listing.getType());
        long result = db.insert(TABLE_LISTINGS, null, values);
        db.close();
        return result != -1;
    }

    private List<Listing> fetchListings(String query, String[] args) {
        List<Listing> list = new ArrayList<>();
        // Use try-with-resources to ensure the database is closed automatically
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery(query, args)) {

            if (cursor.moveToFirst()) {
                do {
                    list.add(new Listing(
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRICE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONDITION)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_RES)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FACULTY)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SELLER_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow("status")) // Pass the 11th argument
                    ));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Listing> getAllListings() { return fetchListings("SELECT * FROM " + TABLE_LISTINGS, null); }
    public List<Listing> getListingsByCategory(String category) { return fetchListings("SELECT * FROM " + TABLE_LISTINGS + " WHERE " + COLUMN_CATEGORY + " = ?", new String[]{category}); }
    public List<Listing> getListingsBySellerId(int sellerId) { return fetchListings("SELECT * FROM " + TABLE_LISTINGS + " WHERE " + COLUMN_SELLER_ID + " = ?", new String[]{String.valueOf(sellerId)}); }
    public Listing getListingById(int id) {
        List<Listing> list = fetchListings("SELECT * FROM " + TABLE_LISTINGS + " WHERE " + COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        return list.isEmpty() ? null : list.get(0);
    }

    // --- User Management ---

    public boolean addUser(String username, String email, String phone, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Name", username);
        values.put("Email", email);
        values.put("PhoneNum", phone);
        values.put("Password", password);
        values.put("UserType", "B");
        return db.insert("Student", null, values) != -1;
    }

    // Inside DatabaseHelper.java
    public int getStuIDByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        int stuId = -1;
        Cursor cursor = null;
        try {
            cursor = db.query("Student", new String[]{"StuID"}, "Email=?", new String[]{email}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                stuId = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return stuId;
    }
    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("Student", new String[]{"StuID"}, "Email=? AND Password=?", new String[]{email, password}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public String getUserNameByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("Student", new String[]{"Name"}, "Email=?", new String[]{email}, null, null, null);
        String name = cursor.moveToFirst() ? cursor.getString(0) : "User";
        cursor.close();
        return name;
    }

    public int getMyListingsCount(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_LISTINGS + " WHERE " + COLUMN_SELLER_ID + " = ?", new String[]{String.valueOf(userId)});
        int count = cursor.moveToFirst() ? cursor.getInt(0) : 0;
        cursor.close();
        return count;
    }

    public void deleteListing(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LISTINGS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public boolean updateListing(int id, String title, String price, String category,
                                 String condition, String imagePath, String description,
                                 String faculty, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_PRICE, price);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_CONDITION, condition);
        values.put(COLUMN_IMAGE_RES, imagePath); // Updates the image path string
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_FACULTY, faculty);
        values.put(COLUMN_TYPE, type);

        int result = db.update(TABLE_LISTINGS, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return result > 0;
    }

    public Cursor getListingWithSellerPhone(int itemId) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Ensure COLUMN_ID and COLUMN_SELLER_ID are defined as constants in your class
        String query = "SELECT l.*, s.Name, s.PhoneNum FROM " + TABLE_LISTINGS + " l " +
                "JOIN Student s ON l." + COLUMN_SELLER_ID + " = s.StuID " +
                "WHERE l." + COLUMN_ID + " = ?";
        return db.rawQuery(query, new String[]{String.valueOf(itemId)});
    }

    public void updateListingStatus(int id, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status); // Ensure your table has a 'status' column
        db.update(TABLE_LISTINGS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public Cursor getStudentProfile(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        // This queries the Name and Email for a specific student ID
        return db.rawQuery("SELECT Name, Email FROM Student WHERE StuID = ?",
                new String[]{String.valueOf(userId)});
    }

    public int getWishlistCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM Wishlist", null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    public List<Listing> getListingsByStatus(String status) {
        List<Listing> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query for all columns including status
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_LISTINGS + " WHERE status = ?", new String[]{status});

        if (cursor.moveToFirst()) {
            int idIdx = cursor.getColumnIndex("id");
            int titleIdx = cursor.getColumnIndex("title");
            int priceIdx = cursor.getColumnIndex("price");
            int catIdx = cursor.getColumnIndex("category");
            int condIdx = cursor.getColumnIndex("condition");
            int imgIdx = cursor.getColumnIndex("image_path");
            int descIdx = cursor.getColumnIndex("description");
            int facIdx = cursor.getColumnIndex("faculty");
            int typeIdx = cursor.getColumnIndex("listing_type");
            int sellerIdx = cursor.getColumnIndex("seller_id");
            int statusIdx = cursor.getColumnIndex("status");

            do {
                // FIX: Pass all 11 arguments here
                Listing l = new Listing(
                        cursor.getInt(idIdx),
                        cursor.getString(titleIdx),
                        cursor.getString(priceIdx),
                        cursor.getString(catIdx),
                        cursor.getString(condIdx),
                        cursor.getString(imgIdx),
                        cursor.getString(descIdx),
                        cursor.getString(facIdx),
                        cursor.getString(typeIdx),
                        cursor.getInt(sellerIdx),
                        cursor.getString(statusIdx) // This is the 11th argument
                );
                list.add(l);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }
    public Cursor getStudentProfileByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query("Student",
                new String[]{"Name", "Email", "PhoneNum", "UserType"},
                "Email=?",
                new String[]{email}, null, null, null);
    }

    public boolean updateStudentProfile(String email, String name, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Name", name);
        values.put("PhoneNum", phone);
        int result = db.update("Student", values, "Email=?", new String[]{email});
        db.close();
        return result > 0;
    }

    public boolean updateStudentPassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Password", newPassword);
        int result = db.update("Student", values, "Email=?", new String[]{email});
        db.close();
        return result > 0;
    }

    public List<Listing> getListingsBySellerIdAndStatus(int sellerId, String status) {
        String query = "SELECT * FROM " + TABLE_LISTINGS +
                " WHERE " + COLUMN_SELLER_ID + " = ? AND status = ?";
        return fetchListings(query, new String[]{String.valueOf(sellerId), status});
    }
    public int getListingsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;

        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_LISTINGS, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    // Check if the item is already favorited by this user
    public boolean isWishlisted(int buyerId, int itemId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Wishlist WHERE BuyerID = ? AND ItemID = ?",
                new String[]{String.valueOf(buyerId), String.valueOf(itemId)});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Remove item from wishlist
    public boolean removeFromWishlist(int buyerId, int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete("Wishlist", "BuyerID = ? AND ItemID = ?",
                new String[]{String.valueOf(buyerId), String.valueOf(itemId)});
        return result > 0;
    }

    // --- Wishlist & Notification Operations ---

    // FIXED: Resolves the 'insertWishlist' compilation error in ListingAdapter
    public boolean insertWishlist(int buyerId, int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("BuyerID", buyerId);
        values.put("ItemID", itemId);

        // Dynamically add today's date
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        String currentDate = sdf.format(new java.util.Date());
        values.put("Date_Added", currentDate);

        long result = db.insert("Wishlist", null, values);
        db.close();
        return result != -1;
    }

    // Advanced Operation: Marks an item as sold and triggers local notifications for watchers
    public void markItemAsSold(Context context, int itemId, String itemTitle) {
        // 1. Update the item's status in the database to 'Sold'
        updateListingStatus(itemId, "Sold");

        // 2. Query all buyers who added this specific item to their wishlist
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT BuyerID FROM Wishlist WHERE ItemID = ?", new String[]{String.valueOf(itemId)});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    // Send a system channel update for every user tracking the item
                    sendLocalNotification(context,
                            "An item in your Favorites has been sold!",
                            itemTitle + " is no longer available.");
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    // Local System Tray Notification Builder Utility
    private void sendLocalNotification(Context context, String title, String message) {
        String channelId = "item_updates_channel";
        android.app.NotificationManager notificationManager =
                (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                    channelId, "Campus Marketplace Updates",
                    android.app.NotificationManager.IMPORTANCE_DEFAULT);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        androidx.core.app.NotificationCompat.Builder builder =
                new androidx.core.app.NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(android.R.drawable.stat_notify_chat)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true);

        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
    // Notifies all tracking buyers if a seller reduces or changes an item's price
    public void checkAndNotifyPriceChange(Context context, int itemId, String newPrice, String itemTitle) {
        SQLiteDatabase db = this.getReadableDatabase();

        // 1. Fetch the old price from the database first to compare
        String oldPrice = "";
        Cursor priceCursor = db.rawQuery("SELECT " + COLUMN_PRICE + " FROM " + TABLE_LISTINGS + " WHERE " + COLUMN_ID + " = ?", new String[]{String.valueOf(itemId)});
        if (priceCursor != null) {
            if (priceCursor.moveToFirst()) {
                oldPrice = priceCursor.getString(0);
            }
            priceCursor.close();
        }

        // 2. If the price actually changed, find everyone who wishlisted it
        if (!oldPrice.isEmpty() && !oldPrice.equals(newPrice)) {
            Cursor watcherCursor = db.rawQuery("SELECT BuyerID FROM Wishlist WHERE ItemID = ?", new String[]{String.valueOf(itemId)});
            if (watcherCursor != null) {
                if (watcherCursor.moveToFirst()) {
                    do {
                        sendLocalNotification(context,
                                "Price drop alert! ",
                                "An item in your Favorites (" + itemTitle + ") changed from RM " + oldPrice + " to RM " + newPrice + ".");
                    } while (watcherCursor.moveToNext());
                }
                watcherCursor.close();
            }
        }
    }
    public List<Listing> getWishlistListings(int buyerId) {
        List<Listing> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // SQL JOIN to pull complete listing details matching rows saved in the Wishlist table
        String query = "SELECT l.* FROM " + TABLE_LISTINGS + " l " +
                "JOIN Wishlist w ON l." + COLUMN_ID + " = w.ItemID " +
                "WHERE w.BuyerID = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(buyerId)});

        if (cursor.moveToFirst()) {
            do {
                list.add(new Listing(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRICE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONDITION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_RES)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FACULTY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SELLER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow("status"))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
}

