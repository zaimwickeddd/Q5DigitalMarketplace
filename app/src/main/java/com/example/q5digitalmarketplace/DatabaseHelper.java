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
    private static final int DATABASE_VERSION = 9;

    public static final String TABLE_LISTINGS = "listings";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_CONDITION = "condition";
    public static final String COLUMN_IMAGE_RES = "image_resource_id";

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
            db.execSQL("CREATE TABLE " + TABLE_LISTINGS + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_TITLE + " TEXT,"
                    + COLUMN_PRICE + " TEXT,"
                    + COLUMN_CATEGORY + " TEXT,"
                    + COLUMN_CONDITION + " TEXT,"
                    + COLUMN_IMAGE_RES + " INTEGER" + ")");

            db.execSQL("CREATE TABLE " + TABLE_USERS + "("
                    + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_USERNAME + " TEXT,"
                    + COLUMN_USER_EMAIL + " TEXT UNIQUE,"
                    + COLUMN_USER_PASSWORD + " TEXT" + ")");
            Log.d("DatabaseHelper", "Tables created successfully");
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error creating tables", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LISTINGS);
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

    public void insertListing(Listing listing) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, listing.getTitle());
        values.put(COLUMN_PRICE, listing.getPrice());
        values.put(COLUMN_CATEGORY, listing.getCategory());
        values.put(COLUMN_CONDITION, listing.getCondition());
        values.put(COLUMN_IMAGE_RES, listing.getImageResourceId());

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
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_RES))
                    ));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return list;
    }
}