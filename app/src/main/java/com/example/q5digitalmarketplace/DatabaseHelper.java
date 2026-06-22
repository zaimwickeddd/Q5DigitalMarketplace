package com.example.q5digitalmarketplace;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Q5Marketplace.db";
    private static final int DATABASE_VERSION = 2; // Incremented version to force database update

    // Table and Column names for Listings
    public static final String TABLE_LISTINGS = "listings";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_CONDITION = "condition";
    public static final String COLUMN_IMAGE_RES = "image_resource_id";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. Create Listings Table
        String CREATE_LISTINGS_TABLE = "CREATE TABLE " + TABLE_LISTINGS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_PRICE + " TEXT,"
                + COLUMN_CATEGORY + " TEXT,"
                + COLUMN_CONDITION + " TEXT,"
                + COLUMN_IMAGE_RES + " INTEGER" + ")";
        db.execSQL(CREATE_LISTINGS_TABLE);

        // 2. Create Student Table
        String CREATE_STUDENT_TABLE = "CREATE TABLE Student ("
                + "StuID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "Name TEXT, "
                + "Email TEXT, "
                + "PhoneNum TEXT, "
                + "UserType TEXT);";
        db.execSQL(CREATE_STUDENT_TABLE);

        // 3. Create Buyer Table
        String CREATE_BUYER_TABLE = "CREATE TABLE Buyer ("
                + "BuyerID INTEGER PRIMARY KEY, "
                + "BuyerBio TEXT, "
                + "Date_Joined TEXT, "
                + "FOREIGN KEY(BuyerID) REFERENCES Student(StuID));";
        db.execSQL(CREATE_BUYER_TABLE);

        // 4. Create Seller Table
        String CREATE_SELLER_TABLE = "CREATE TABLE Seller ("
                + "SellerID INTEGER PRIMARY KEY, "
                + "StoreBio TEXT, "
                + "FOREIGN KEY(SellerID) REFERENCES Student(StuID));";
        db.execSQL(CREATE_SELLER_TABLE);

        // 5. Create Wishlist Table
        String CREATE_WISHLIST_TABLE = "CREATE TABLE Wishlist ("
                + "WishListID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "Date_Added TEXT, "
                + "BuyerID INTEGER, "
                + "ItemID INTEGER, "
                + "FOREIGN KEY(BuyerID) REFERENCES Buyer(BuyerID), "
                + "FOREIGN KEY(ItemID) REFERENCES " + TABLE_LISTINGS + "(" + COLUMN_ID + "));";
        db.execSQL(CREATE_WISHLIST_TABLE);

        // Pre-populate your own profile into Student table so your Profile tab has active data
        db.execSQL("INSERT INTO Student (Name, Email, PhoneNum, UserType) VALUES ('Awang Najib', 'najib123@gmail.com', '0123456789', 'B');");
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

    // --- EXISTING LISTING METHODS ---

    public void insertListing(Listing listing) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, listing.getTitle());
        values.put(COLUMN_PRICE, listing.getPrice());
        values.put(COLUMN_CATEGORY, listing.getCategory());
        values.put(COLUMN_CONDITION, listing.getCondition());
        values.put(COLUMN_IMAGE_RES, listing.getImageResourceId());

        db.insert(TABLE_LISTINGS, null, values);
        db.close();
    }

    public List<Listing> getAllListings() {
        List<Listing> list = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_LISTINGS;
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(selectQuery, null)) {
            if (cursor.moveToFirst()) {
                do {
                    Listing item = new Listing(
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRICE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONDITION)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_RES))
                    );
                    list.add(item);
                } while (cursor.moveToNext());
            }
        } finally {
            db.close();
        }
        return list;
    }

    // --- WISHLIST & PROFILE PROFILE QUERIES ---

    public boolean insertWishlist(int buyerId, int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("BuyerID", buyerId);
        values.put("ItemID", itemId);
        values.put("Date_Added", "2026-06-22"); // Tracks current project simulation date
        long result = db.insert("Wishlist", null, values);
        db.close();
        return result != -1;
    }

    public Cursor getStudentProfile(int stuId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT Name, Email FROM Student WHERE StuID = ?", new String[]{String.valueOf(stuId)});
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
}