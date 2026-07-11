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
    // 🛠️ BUMPED TO VERSION 30: Ensures we stay ahead of any local versions to avoid downgrade crashes.
    private static final int DATABASE_VERSION = 30;

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
    public static final String COLUMN_WHATSAPP_CLICKS = "whatsapp_clicks";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. Create Student Table with Profile Image field properties
        db.execSQL("CREATE TABLE IF NOT EXISTS Student (" +
                "StuID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Name TEXT, " +
                "Email TEXT UNIQUE, " +
                "PhoneNum TEXT, " +
                "Password TEXT, " +
                "UserType TEXT, " +
                "ProfileImage TEXT);");

        // 2. Create Marketplace Item Listings Table with analytics tracker bounds
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_LISTINGS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_PRICE + " TEXT, " +
                COLUMN_CATEGORY + " TEXT, " +
                COLUMN_CONDITION + " TEXT, " +
                COLUMN_IMAGE_RES + " TEXT, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_FACULTY + " TEXT, " +
                COLUMN_SELLER_ID + " INTEGER, " +
                "status TEXT, " +
                COLUMN_TYPE + " TEXT, " +
                COLUMN_WHATSAPP_CLICKS + " INTEGER DEFAULT 0, " +
                "FOREIGN KEY(" + COLUMN_SELLER_ID + ") REFERENCES Student(StuID));");

        // 3. Create Buyer Profile Sub-table bounds
        db.execSQL("CREATE TABLE IF NOT EXISTS Buyer (" +
                "BuyerID INTEGER PRIMARY KEY, " +
                "BuyerBio TEXT, " +
                "Date_Joined TEXT, " +
                "FOREIGN KEY(BuyerID) REFERENCES Student(StuID));");

        // 4. Create Seller Profile Sub-table bounds
        db.execSQL("CREATE TABLE IF NOT EXISTS Seller (" +
                "SellerID INTEGER PRIMARY KEY, " +
                "StoreBio TEXT, " +
                "FOREIGN KEY(SellerID) REFERENCES Student(StuID));");

        // 5. Create Wishlist Relational Junction table framework
        db.execSQL("CREATE TABLE IF NOT EXISTS Wishlist (" +
                "WishListID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Date_Added TEXT, " +
                "BuyerID INTEGER, " +
                "ItemID INTEGER, " +
                "FOREIGN KEY(BuyerID) REFERENCES Student(StuID), " +
                "FOREIGN KEY(ItemID) REFERENCES " + TABLE_LISTINGS + "(" + COLUMN_ID + "));");

        // 6. Create Notifications Table
        db.execSQL("CREATE TABLE IF NOT EXISTS Notifications (" +
                "NotifID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "UserID INTEGER, " +
                "Title TEXT, " +
                "Message TEXT, " +
                "Timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "IsRead INTEGER DEFAULT 0, " +
                "FOREIGN KEY(UserID) REFERENCES Student(StuID));");

        // Seed default mock catalog dataset records on database instantiation
        insertMockData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Wishlist");
        db.execSQL("DROP TABLE IF EXISTS Notifications");
        db.execSQL("DROP TABLE IF EXISTS Seller");
        db.execSQL("DROP TABLE IF EXISTS Buyer");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LISTINGS);
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
                            cursor.getString(cursor.getColumnIndexOrThrow("status"))
                    ));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Listing> getAllListings() {
        return fetchListings("SELECT * FROM " + TABLE_LISTINGS, null);
    }

    public List<Listing> getListingsByCategory(String category) {
        return fetchListings("SELECT * FROM " + TABLE_LISTINGS + " WHERE " + COLUMN_CATEGORY + " = ?", new String[]{category});
    }

    public List<Listing> getListingsBySellerId(int sellerId) {
        return fetchListings("SELECT * FROM " + TABLE_LISTINGS + " WHERE " + COLUMN_SELLER_ID + " = ?", new String[]{String.valueOf(sellerId)});
    }

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

    public boolean updateListing(int id, String title, String price, String category, String condition, String imagePath, String description, String faculty, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_PRICE, price);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_CONDITION, condition);
        values.put(COLUMN_IMAGE_RES, imagePath);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_FACULTY, faculty);
        values.put(COLUMN_TYPE, type);

        int result = db.update(TABLE_LISTINGS, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return result > 0;
    }

    public Cursor getListingWithSellerPhone(int itemId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT l.*, s.Name, s.PhoneNum FROM " + TABLE_LISTINGS + " l " + "JOIN Student s ON l." + COLUMN_SELLER_ID + " = s.StuID " + "WHERE l." + COLUMN_ID + " = ?";
        return db.rawQuery(query, new String[]{String.valueOf(itemId)});
    }

    public void updateListingStatus(int id, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status);
        db.update(TABLE_LISTINGS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public Cursor getStudentProfile(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT Name, Email FROM Student WHERE StuID = ?", new String[]{String.valueOf(userId)});
    }

    public int getWishlistCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM Wishlist", null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    public int getWishlistCount(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM Wishlist WHERE BuyerID = ?", new String[]{String.valueOf(userId)});
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
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_LISTINGS + " WHERE status = ?", new String[]{status});

        if (cursor.moveToFirst()) {
            int idIdx = cursor.getColumnIndex("id");
            int titleIdx = cursor.getColumnIndex("title");
            int priceIdx = cursor.getColumnIndex("price");
            int catIdx = cursor.getColumnIndex("category");
            int condIdx = cursor.getColumnIndex("condition");
            int imgIdx = cursor.getColumnIndex("image_resource_id");
            int descIdx = cursor.getColumnIndex("description");
            int facIdx = cursor.getColumnIndex("faculty");
            int typeIdx = cursor.getColumnIndex("listing_type");
            int sellerIdx = cursor.getColumnIndex("seller_id");
            int statusIdx = cursor.getColumnIndex("status");

            do {
                Listing l = new Listing(cursor.getInt(idIdx), cursor.getString(titleIdx), cursor.getString(priceIdx), cursor.getString(catIdx), cursor.getString(condIdx), cursor.getString(imgIdx), cursor.getString(descIdx), cursor.getString(facIdx), cursor.getString(typeIdx), cursor.getInt(sellerIdx), cursor.getString(statusIdx));
                list.add(l);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public Cursor getStudentProfileByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query("Student", new String[]{"StuID", "Name", "Email", "PhoneNum", "UserType", "ProfileImage"},
                "Email=?", new String[]{email}, null, null, null);
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
        String query = "SELECT * FROM " + TABLE_LISTINGS + " WHERE " + COLUMN_SELLER_ID + " = ? AND status = ?";
        return fetchListings(query, new String[]{String.valueOf(sellerId), status});
    }

    public boolean isWishlisted(int buyerId, int itemId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Wishlist WHERE BuyerID = ? AND ItemID = ?", new String[]{String.valueOf(buyerId), String.valueOf(itemId)});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean removeFromWishlist(int buyerId, int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete("Wishlist", "BuyerID = ? AND ItemID = ?", new String[]{String.valueOf(buyerId), String.valueOf(itemId)});
        return result > 0;
    }

    public boolean insertWishlist(int buyerId, int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("BuyerID", buyerId);
        values.put("ItemID", itemId);

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        String currentDate = sdf.format(new java.util.Date());
        values.put("Date_Added", currentDate);

        long result = db.insert("Wishlist", null, values);
        db.close();
        return result != -1;
    }

    public void markItemAsSold(Context context, int itemId, String itemTitle) {
        updateListingStatus(itemId, "Sold");
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT BuyerID FROM Wishlist WHERE ItemID = ?", new String[]{String.valueOf(itemId)});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int buyerId = cursor.getInt(0);
                    String title = "Item Sold!";
                    String message = "An item in your Favorites (" + itemTitle + ") has been sold.";
                    addNotification(buyerId, title, message);
                    sendLocalNotification(context, title, message);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    public void addNotification(int userId, String title, String message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("UserID", userId);
        values.put("Title", title);
        values.put("Message", message);
        db.insert("Notifications", null, values);
        db.close();
    }

    public Cursor getNotifications(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM Notifications WHERE UserID = ? ORDER BY Timestamp DESC", new String[]{String.valueOf(userId)});
    }

    public int getUnreadNotificationsCount(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM Notifications WHERE UserID = ? AND IsRead = 0", new String[]{String.valueOf(userId)});
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    public void markNotificationAsRead(int notifId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("IsRead", 1);
        db.update("Notifications", values, "NotifID = ?", new String[]{String.valueOf(notifId)});
        db.close();
    }

    public void markAllNotificationsAsRead(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("IsRead", 1);
        db.update("Notifications", values, "UserID = ?", new String[]{String.valueOf(userId)});
        db.close();
    }

    private void sendLocalNotification(Context context, String title, String message) {
        String channelId = "item_updates_channel";
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(channelId, "Campus Marketplace Updates", android.app.NotificationManager.IMPORTANCE_DEFAULT);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        androidx.core.app.NotificationCompat.Builder builder = new androidx.core.app.NotificationCompat.Builder(context, channelId).setSmallIcon(android.R.drawable.stat_notify_chat).setContentTitle(title).setContentText(message).setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT).setAutoCancel(true);

        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    public void checkAndNotifyPriceChange(Context context, int itemId, String newPrice, String itemTitle) {
        SQLiteDatabase db = this.getReadableDatabase();
        String oldPrice = "";
        Cursor priceCursor = db.rawQuery("SELECT " + COLUMN_PRICE + " FROM " + TABLE_LISTINGS + " WHERE " + COLUMN_ID + " = ?", new String[]{String.valueOf(itemId)});
        if (priceCursor != null) {
            if (priceCursor.moveToFirst()) {
                oldPrice = priceCursor.getString(0);
            }
            priceCursor.close();
        }

        if (!oldPrice.isEmpty() && !oldPrice.equals(newPrice)) {
            Cursor watcherCursor = db.rawQuery("SELECT BuyerID FROM Wishlist WHERE ItemID = ?", new String[]{String.valueOf(itemId)});
            if (watcherCursor != null) {
                if (watcherCursor.moveToFirst()) {
                    do {
                        int buyerId = watcherCursor.getInt(0);
                        String title = "Price drop alert! ";
                        String message = "An item in your Favorites (" + itemTitle + ") changed from " + oldPrice + " to " + newPrice + ".";
                        addNotification(buyerId, title, message);
                        sendLocalNotification(context, title, message);
                    } while (watcherCursor.moveToNext());
                }
                watcherCursor.close();
            }
        }
    }

    public List<Listing> getWishlistListings(int buyerId) {
        List<Listing> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT l.* FROM " + TABLE_LISTINGS + " l " + "JOIN Wishlist w ON l." + COLUMN_ID + " = w.ItemID " + "WHERE w.BuyerID = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(buyerId)});

        if (cursor.moveToFirst()) {
            do {
                list.add(new Listing(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)), cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)), cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRICE)), cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)), cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONDITION)), cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_RES)), cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)), cursor.getString(cursor.getColumnIndexOrThrow(cursor.getColumnIndexOrThrow(COLUMN_FACULTY) >= 0 ? COLUMN_FACULTY : COLUMN_DESCRIPTION)), cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)), cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SELLER_ID)), cursor.getString(cursor.getColumnIndexOrThrow("status"))));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public Cursor getCategoryAnalyticsData() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_CATEGORY + ", SUM(" + COLUMN_WHATSAPP_CLICKS + ") AS TotalClicks " + "FROM " + TABLE_LISTINGS + " " + "GROUP BY " + COLUMN_CATEGORY + " " + "ORDER BY TotalClicks DESC";
        return db.rawQuery(query, null);
    }

    public Cursor getCategoryAnalyticsDataBySeller(int sellerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_CATEGORY + ", SUM(" + COLUMN_WHATSAPP_CLICKS + ") AS TotalClicks " +
                "FROM " + TABLE_LISTINGS + " " +
                "WHERE " + COLUMN_SELLER_ID + " = ? " +
                "GROUP BY " + COLUMN_CATEGORY + " " +
                "ORDER BY TotalClicks DESC";
        return db.rawQuery(query, new String[]{String.valueOf(sellerId)});
    }

    public Cursor getItemPerformanceDataBySeller(int sellerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_TITLE + ", " + COLUMN_WHATSAPP_CLICKS + " " +
                "FROM " + TABLE_LISTINGS + " " +
                "WHERE " + COLUMN_SELLER_ID + " = ? " +
                "ORDER BY " + COLUMN_WHATSAPP_CLICKS + " DESC";
        return db.rawQuery(query, new String[]{String.valueOf(sellerId)});
    }

    public void incrementWhatsAppClicks(int listingId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_LISTINGS + " SET " + COLUMN_WHATSAPP_CLICKS + " = " + COLUMN_WHATSAPP_CLICKS + " + 1 " + " WHERE " + COLUMN_ID + " = ?", new String[]{String.valueOf(listingId)});
    }

    public Cursor getItemDistributionCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT " + COLUMN_CATEGORY + ", COUNT(*) AS TotalCount " + "FROM " + TABLE_LISTINGS + " " + "GROUP BY " + COLUMN_CATEGORY + " " + "ORDER BY TotalCount DESC", null);
    }

    public Cursor getItemDistributionCountBySeller(int sellerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT " + COLUMN_CATEGORY + ", COUNT(*) AS TotalCount " +
                "FROM " + TABLE_LISTINGS + " " +
                "WHERE " + COLUMN_SELLER_ID + " = ? " +
                "GROUP BY " + COLUMN_CATEGORY + " " +
                "ORDER BY TotalCount DESC", new String[]{String.valueOf(sellerId)});
    }

    public boolean updateStudentProfileData(String email, String name, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Name", name);
        values.put("ProfileImage", imagePath);
        int result = db.update("Student", values, "Email=?", new String[]{email});
        db.close();
        return result > 0;
    }

    public boolean updateStudentProfileImage(String email, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ProfileImage", imagePath);
        int result = db.update("Student", values, "Email=?", new String[]{email});
        db.close();
        return result > 0;
    }

    // 🛠️ MOCK DATA SEED ENGINE: SEEDS 4 STUDENTS, 4 SELLERS, 10 OWNER-LINKED LISTINGS, AND 8 WISHLIST CONNECTORS
    private void insertMockData(SQLiteDatabase db) {
        ContentValues userValues = new ContentValues();

        // --- Seed 4 Student Profiles ---
        // 1. Zaim
        userValues.put("StuID", 1);
        userValues.put("Name", "Zaim");
        userValues.put("Email", "zaim@gmail.com");
        userValues.put("PhoneNum", "0166221844");
        userValues.put("Password", "1234567");
        userValues.put("UserType", "S");
        userValues.put("ProfileImage", "profile_zaim");
        db.insert("Student", null, userValues);
        userValues.clear();

        // 2. Nadzim
        userValues.put("StuID", 2);
        userValues.put("Name", "Nadzim");
        userValues.put("Email", "nadzim@gmail.com");
        userValues.put("PhoneNum", "60128794719");
        userValues.put("Password", "1234567");
        userValues.put("UserType", "S");
        userValues.put("ProfileImage", "profile_nadzim");
        db.insert("Student", null, userValues);
        userValues.clear();

        // 3. Karl
        userValues.put("StuID", 3);
        userValues.put("Name", "Karl");
        userValues.put("Email", "karl@gmail.com");
        userValues.put("PhoneNum", "60137010497");
        userValues.put("Password", "1234567");
        userValues.put("UserType", "S");
        userValues.put("ProfileImage", "profile_karl");
        db.insert("Student", null, userValues);
        userValues.clear();

        // 4. Najib
        userValues.put("StuID", 4);
        userValues.put("Name", "Najib");
        userValues.put("Email", "najib@gmail.com");
        userValues.put("PhoneNum", "0138519619");
        userValues.put("Password", "1234567");
        userValues.put("UserType", "S");
        userValues.put("ProfileImage", "profile_najib");
        db.insert("Student", null, userValues);
        userValues.clear();

        // --- Seed 4 Corresponding Stores into Seller Table ---
        ContentValues sellerValues = new ContentValues();

        sellerValues.put("SellerID", 1);
        sellerValues.put("StoreBio", "Zaim's Academic Bookstore & Reference Manuals");
        db.insert("Seller", null, sellerValues);
        sellerValues.clear();

        sellerValues.put("SellerID", 2);
        sellerValues.put("StoreBio", "Nadzim's Tech Corner - Keyboards & Coding Supplies");
        db.insert("Seller", null, sellerValues);
        sellerValues.clear();

        sellerValues.put("SellerID", 3);
        sellerValues.put("StoreBio", "Karl's Thrift Hub - Vintage Streetwear & Kits");
        db.insert("Seller", null, sellerValues);
        sellerValues.clear();

        sellerValues.put("SellerID", 4);
        sellerValues.put("StoreBio", "Najib's Engineering Gear & Digital Electronics Hub");
        db.insert("Seller", null, sellerValues);
        sellerValues.clear();

        // --- Seed Exactly 10 Relational Item Listings linked to Owners ---
        ContentValues values = new ContentValues();

        // Item 1: Lenovo LOQ Laptop (Seller 4: Najib)
        values.put(COLUMN_TITLE, "Lenovo LOQ Laptop");
        values.put(COLUMN_PRICE, "1500.00");
        values.put(COLUMN_CATEGORY, "Electronics");
        values.put(COLUMN_CONDITION, "Used");
        values.put(COLUMN_IMAGE_RES, "lenovo_loq");
        values.put(COLUMN_DESCRIPTION, "Powerful gaming laptop, perfect for programming and FYP tasks. 16GB RAM.");
        values.put(COLUMN_FACULTY, "FSKM");
        values.put(COLUMN_SELLER_ID, 4);
        values.put("status", "Active");
        values.put(COLUMN_TYPE, "Sell");
        values.put(COLUMN_WHATSAPP_CLICKS, 18);
        db.insert(TABLE_LISTINGS, null, values);
        values.clear();

        // Item 2: Introductory Mandarin Book (Seller 1: Zaim)
        values.put(COLUMN_TITLE, "Introductory Mandarin Book");
        values.put(COLUMN_PRICE, "30.00");
        values.put(COLUMN_CATEGORY, "Books");
        values.put(COLUMN_CONDITION, "Brand New");
        values.put(COLUMN_IMAGE_RES, "mandarin_books");
        values.put(COLUMN_DESCRIPTION, "Official campus textbook for beginner language courses. No markings inside.");
        values.put(COLUMN_FACULTY, "APB");
        values.put(COLUMN_SELLER_ID, 1);
        values.put("status", "Active");
        values.put(COLUMN_TYPE, "Sell");
        values.put(COLUMN_WHATSAPP_CLICKS, 6);
        db.insert(TABLE_LISTINGS, null, values);
        values.clear();

        // Item 3: Cabana Vintage Shirt (Seller 3: Karl)
        values.put(COLUMN_TITLE, "Cabana Vintage Shirt");
        values.put(COLUMN_PRICE, "50.00");
        values.put(COLUMN_CATEGORY, "Clothes");
        values.put(COLUMN_CONDITION, "Used");
        values.put(COLUMN_IMAGE_RES, "canaba_shirt");
        values.put(COLUMN_DESCRIPTION, "Comfortable vintage oversized short sleeve shirt. Size L.");
        values.put(COLUMN_FACULTY, "FSKM");
        values.put(COLUMN_SELLER_ID, 3);
        values.put("status", "Active");
        values.put(COLUMN_TYPE, "Sell");
        values.put(COLUMN_WHATSAPP_CLICKS, 11);
        db.insert(TABLE_LISTINGS, null, values);
        values.clear();

        // Item 4: Adidas Originals Backpack (Seller 3: Karl)
        values.put(COLUMN_TITLE, "Adidas Originals Backpack");
        values.put(COLUMN_PRICE, "129.00");
        values.put(COLUMN_CATEGORY, "Clothes");
        values.put(COLUMN_CONDITION, "Brand New");
        values.put(COLUMN_IMAGE_RES, "adidas_backpack");
        values.put(COLUMN_DESCRIPTION, "Spacious laptop bag. Water-resistant material, ideal for campus walking.");
        values.put(COLUMN_FACULTY, "FPP");
        values.put(COLUMN_SELLER_ID, 3);
        values.put("status", "Active");
        values.put(COLUMN_TYPE, "Sell");
        values.put(COLUMN_WHATSAPP_CLICKS, 14);
        db.insert(TABLE_LISTINGS, null, values);
        values.clear();

        // Item 5: Nike Shoes (Seller 4: Najib)
        values.put(COLUMN_TITLE, "Nike Shoes");
        values.put(COLUMN_PRICE, "210.00");
        values.put(COLUMN_CATEGORY, "Sports");
        values.put(COLUMN_CONDITION, "Used");
        values.put(COLUMN_IMAGE_RES, "nike_shoes");
        values.put(COLUMN_DESCRIPTION, "Comfortable high stability running track sneakers. Size UK 9.");
        values.put(COLUMN_FACULTY, "FSKM");
        values.put(COLUMN_SELLER_ID, 4);
        values.put("status", "Active");
        values.put(COLUMN_TYPE, "Sell");
        values.put(COLUMN_WHATSAPP_CLICKS, 1);
        db.insert(TABLE_LISTINGS, null, values);
        values.clear();

        // Item 6: Wireless Mechanical Keyboard (Seller 2: Nadzim)
        values.put(COLUMN_TITLE, "Wireless Mechanical Keyboard");
        values.put(COLUMN_PRICE, "180.00");
        values.put(COLUMN_CATEGORY, "Electronics");
        values.put(COLUMN_CONDITION, "Brand New");
        values.put(COLUMN_IMAGE_RES, "keyboard_res");
        values.put(COLUMN_DESCRIPTION, "Hot-swappable tactile switches with crisp customizable RGB lighting profiles.");
        values.put(COLUMN_FACULTY, "FSKM");
        values.put(COLUMN_SELLER_ID, 2);
        values.put("status", "Active");
        values.put(COLUMN_TYPE, "Sell");
        values.put(COLUMN_WHATSAPP_CLICKS, 9);
        db.insert(TABLE_LISTINGS, null, values);
        values.clear();

        // Item 7: Data Structures Textbook (Seller 1: Zaim)
        values.put(COLUMN_TITLE, "Data Structures Textbook");
        values.put(COLUMN_PRICE, "45.00");
        values.put(COLUMN_CATEGORY, "Books");
        values.put(COLUMN_CONDITION, "Used");
        values.put(COLUMN_IMAGE_RES, "dsa_book");
        values.put(COLUMN_DESCRIPTION, "Core reference textbook complete with clean assignment algorithm snippets.");
        values.put(COLUMN_FACULTY, "FSKM");
        values.put(COLUMN_SELLER_ID, 1);
        values.put("status", "Active");
        values.put(COLUMN_TYPE, "Sell");
        values.put(COLUMN_WHATSAPP_CLICKS, 12);
        db.insert(TABLE_LISTINGS, null, values);
        values.clear();

        // Item 8: Badminton Racket (Seller 3: Karl)
        values.put(COLUMN_TITLE, "Badminton Racket");
        values.put(COLUMN_PRICE, "85.00");
        values.put(COLUMN_CATEGORY, "Sports");
        values.put(COLUMN_CONDITION, "Used");
        values.put(COLUMN_IMAGE_RES, "racket_res");
        values.put(COLUMN_DESCRIPTION, "Ultralight carbon aerodynamic shaft including high tension frame strings.");
        values.put(COLUMN_FACULTY, "FPP");
        values.put(COLUMN_SELLER_ID, 3);
        values.put("status", "Active");
        values.put(COLUMN_TYPE, "Sell");
        values.put(COLUMN_WHATSAPP_CLICKS, 5);
        db.insert(TABLE_LISTINGS, null, values);
        values.clear();

        // Item 9: Calculus Study Sheets Pack (Seller 1: Zaim)
        values.put(COLUMN_TITLE, "Calculus Study Sheets Pack");
        values.put(COLUMN_PRICE, "15.00");
        values.put(COLUMN_CATEGORY, "Books");
        values.put(COLUMN_CONDITION, "Brand New");
        values.put(COLUMN_IMAGE_RES, "calculus_guide");
        values.put(COLUMN_DESCRIPTION, "Step-by-step solutions manual helper covering core derivation graphs.");
        values.put(COLUMN_FACULTY, "FSKM");
        values.put(COLUMN_SELLER_ID, 1);
        values.put("status", "Active");
        values.put(COLUMN_TYPE, "Sell");
        values.put(COLUMN_WHATSAPP_CLICKS, 3);
        db.insert(TABLE_LISTINGS, null, values);
        values.clear();

        // Item 10: Waterproof Travel Backpack (Seller 2: Nadzim)
        values.put(COLUMN_TITLE, "Waterproof Travel Backpack");
        values.put(COLUMN_PRICE, "95.00");
        values.put(COLUMN_CATEGORY, "Clothes");
        values.put(COLUMN_CONDITION, "Brand New");
        values.put(COLUMN_IMAGE_RES, "tech_backpack");
        values.put(COLUMN_DESCRIPTION, "Anti-theft school laptop travel bag with robust rain protective layers.");
        values.put(COLUMN_FACULTY, "FSKM");
        values.put(COLUMN_SELLER_ID, 2);
        values.put("status", "Active");
        values.put(COLUMN_TYPE, "Sell");
        values.put(COLUMN_WHATSAPP_CLICKS, 15);
        db.insert(TABLE_LISTINGS, null, values);
        values.clear();

        // --- 🛠️ TASK EXTENSION: SEED EXPLICIT FAVORITES MAPPING DATA ---
        ContentValues wishlistValues = new ContentValues();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        String currentDate = sdf.format(new java.util.Date());

        // 1. Zaim (Buyer 1) favorites Najib's Laptop (Item 1) and Nadzim's Keyboard (Item 6)
        wishlistValues.put("BuyerID", 1); wishlistValues.put("ItemID", 1); wishlistValues.put("Date_Added", currentDate);
        db.insert("Wishlist", null, wishlistValues); wishlistValues.clear();
        wishlistValues.put("BuyerID", 1); wishlistValues.put("ItemID", 6); wishlistValues.put("Date_Added", currentDate);
        db.insert("Wishlist", null, wishlistValues); wishlistValues.clear();

        // 2. Nadzim (Buyer 2) favorites Karl's Backpack (Item 4) and Najib's Shoes (Item 5)
        wishlistValues.put("BuyerID", 2); wishlistValues.put("ItemID", 4); wishlistValues.put("Date_Added", currentDate);
        db.insert("Wishlist", null, wishlistValues); wishlistValues.clear();
        wishlistValues.put("BuyerID", 2); wishlistValues.put("ItemID", 5); wishlistValues.put("Date_Added", currentDate);
        db.insert("Wishlist", null, wishlistValues); wishlistValues.clear();

        // 3. Karl (Buyer 3) favorites Zaim's Mandarin Book (Item 2) and Najib's Laptop (Item 1)
        wishlistValues.put("BuyerID", 3); wishlistValues.put("ItemID", 2); wishlistValues.put("Date_Added", currentDate);
        db.insert("Wishlist", null, wishlistValues); wishlistValues.clear();
        wishlistValues.put("BuyerID", 3); wishlistValues.put("ItemID", 1); wishlistValues.put("Date_Added", currentDate);
        db.insert("Wishlist", null, wishlistValues); wishlistValues.clear();

        // 4. Najib (Buyer 4) favorites Karl's Backpack (Item 4) and Zaim's Data Structures Book (Item 7)
        // Note: Najib favoriting Item 4 makes testing the "Mark as Sold" feature instant!
        wishlistValues.put("BuyerID", 4); wishlistValues.put("ItemID", 4); wishlistValues.put("Date_Added", currentDate);
        db.insert("Wishlist", null, wishlistValues); wishlistValues.clear();
        wishlistValues.put("BuyerID", 4); wishlistValues.put("ItemID", 7); wishlistValues.put("Date_Added", currentDate);
        db.insert("Wishlist", null, wishlistValues); wishlistValues.clear();
    }
}