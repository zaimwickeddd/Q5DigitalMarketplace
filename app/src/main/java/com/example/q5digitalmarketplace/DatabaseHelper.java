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
    private static final int DATABASE_VERSION = 11;

    public static final String TABLE_LISTINGS = "listings";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_CONDITION = "condition";
    public static final String COLUMN_IMAGE_RES = "image_resource_id";
    public static final String COLUMN_DESCRIPTION = "description";

    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_PASSWORD = "password";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            String CREATE_LISTINGS_TABLE = "CREATE TABLE " + TABLE_LISTINGS + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_TITLE + " TEXT,"
                    + COLUMN_PRICE + " TEXT,"
                    + COLUMN_CATEGORY + " TEXT,"
                    + COLUMN_CONDITION + " TEXT,"
                    + COLUMN_IMAGE_RES + " TEXT,"
                    + COLUMN_DESCRIPTION + " TEXT" + ")";
            db.execSQL(CREATE_LISTINGS_TABLE);

            String CREATE_STUDENT_TABLE = "CREATE TABLE Student ("
                    + "StuID INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "Name TEXT, "
                    + "Email TEXT, "
                    + "PhoneNum TEXT, "
                    + "UserType TEXT);";
            db.execSQL(CREATE_STUDENT_TABLE);

            String CREATE_BUYER_TABLE = "CREATE TABLE Buyer ("
                    + "BuyerID INTEGER PRIMARY KEY, "
                    + "BuyerBio TEXT, "
                    + "Date_Joined TEXT, "
                    + "FOREIGN KEY(BuyerID) REFERENCES Student(StuID));";
            db.execSQL(CREATE_BUYER_TABLE);

            String CREATE_SELLER_TABLE = "CREATE TABLE Seller ("
                    + "SellerID INTEGER PRIMARY KEY, "
                    + "StoreBio TEXT, "
                    + "FOREIGN KEY(SellerID) REFERENCES Student(StuID));";
            db.execSQL(CREATE_SELLER_TABLE);

            String CREATE_WISHLIST_TABLE = "CREATE TABLE Wishlist ("
                    + "WishListID INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "Date_Added TEXT, "
                    + "BuyerID INTEGER, "
                    + "ItemID INTEGER, "
                    + "FOREIGN KEY(BuyerID) REFERENCES Buyer(BuyerID), "
                    + "FOREIGN KEY(ItemID) REFERENCES " + TABLE_LISTINGS + "(" + COLUMN_ID + "));";
            db.execSQL(CREATE_WISHLIST_TABLE);

            String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                    + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_USERNAME + " TEXT,"
                    + COLUMN_USER_EMAIL + " TEXT UNIQUE,"
                    + COLUMN_USER_PASSWORD + " TEXT" + ")";
            db.execSQL(CREATE_USERS_TABLE);

            // Pre-populate sample profile and your test credentials inside authentication table
            db.execSQL("INSERT INTO Student (Name, Email, PhoneNum, UserType) VALUES ('Awang Najib', 'najib123@gmail.com', '0123456789', 'B');");
            db.execSQL("INSERT INTO " + TABLE_USERS + " (" + COLUMN_USERNAME + ", " + COLUMN_USER_EMAIL + ", " + COLUMN_USER_PASSWORD + ") VALUES ('Zaim', 'zaim@gmail.com', 'zaim123');");

            Log.d("DatabaseHelper", "Tables created successfully with upgraded schema attributes");
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error creating tables", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LISTINGS);
        db.execSQL("DROP TABLE IF EXISTS Wishlist");
        db.execSQL("DROP TABLE IF EXISTS Seller");
        db.execSQL("DROP TABLE IF EXISTS Buyer");
        db.execSQL("DROP TABLE IF EXISTS Student");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public boolean addUser(String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_USER_EMAIL, email);
        values.put(COLUMN_USER_PASSWORD, password);

        long result = -1;
        try {
            result = db.insert(TABLE_USERS, null, values);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error adding user: " + e.getMessage());
        } finally {
            db.close();
        }
        return result != -1;
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean isValid = false;
        try {
            cursor = db.query(TABLE_USERS, new String[]{COLUMN_USER_ID},
                    COLUMN_USER_EMAIL + "=? AND " + COLUMN_USER_PASSWORD + "=?",
                    new String[]{email, password}, null, null, null);
            isValid = cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error checking user", e);
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return isValid;
    }

    public String getUserNameByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String username = "User";
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_USERS, new String[]{COLUMN_USERNAME},
                    COLUMN_USER_EMAIL + "=?", new String[]{email},
                    null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME));
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error fetching username", e);
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return username;
    }

    public void insertListing(Listing listing) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, listing.getTitle());
        values.put(COLUMN_PRICE, listing.getPrice());
        values.put(COLUMN_CATEGORY, listing.getCategory());
        values.put(COLUMN_CONDITION, listing.getCondition());
        values.put(COLUMN_IMAGE_RES, listing.getImagePath());
        values.put(COLUMN_DESCRIPTION, listing.getDescription());

        try {
            db.insert(TABLE_LISTINGS, null, values);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error inserting listing", e);
        } finally {
            db.close();
        }
    }

    public List<Listing> getAllListings() {
        List<Listing> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_LISTINGS, null);
            if (cursor.moveToFirst()) {
                do {
                    list.add(new Listing(
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRICE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONDITION)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_RES)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION))
                    ));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return list;
    }

    public boolean insertWishlist(int buyerId, int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("BuyerID", buyerId);
        values.put("ItemID", itemId);
        values.put("Date_Added", "2026-06-22");
        long result = db.insert("Wishlist", null, values);
        db.close();
        return result != -1;
    }

    public Cursor getStudentProfile(int stuId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT Name, Email FROM Student WHERE StuID = ?", new String[]{String.valueOf(stuId)});
    }

    public Cursor getStudentProfileByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT Name, Email FROM Student WHERE Email = ?", new String[]{email});
    }

    public int getListingsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_LISTINGS, null);
        int count = 0;
        if (cursor.moveToFirst()) { count = cursor.getInt(0); }
        cursor.close();
        return count;
    }

    public int getWishlistCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM Wishlist", null);
        int count = 0;
        if (cursor.moveToFirst()) { count = cursor.getInt(0); }
        cursor.close();
        return count;
    }
    public List<Listing> getListingsByCategory(String categoryName) {
        List<Listing> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            // Query the database matching rows inside your COLUMN_CATEGORY column
            cursor = db.rawQuery("SELECT * FROM " + TABLE_LISTINGS + " WHERE " + COLUMN_CATEGORY + " = ?", new String[]{categoryName});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(new Listing(
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRICE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONDITION)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_RES)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION))
                    ));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "Error filtering categories", e);
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return list;
    }
}