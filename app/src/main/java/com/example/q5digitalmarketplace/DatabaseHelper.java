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
    private static final int DATABASE_VERSION = 1;

    // Table and Column names
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
        // Create table SQL statement
        String CREATE_LISTINGS_TABLE = "CREATE TABLE " + TABLE_LISTINGS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_PRICE + " TEXT,"
                + COLUMN_CATEGORY + " TEXT,"
                + COLUMN_CONDITION + " TEXT,"
                + COLUMN_IMAGE_RES + " INTEGER" + ")";
        db.execSQL(CREATE_LISTINGS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LISTINGS);
        onCreate(db);
    }

    // Method to dynamically insert a new item into the SQLite DB
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

    // Method to query and retrieve all items dynamically from SQLite
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
}