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
    // Incremented to 17 to accommodate the new 'faculty' column
    private static final int DATABASE_VERSION = 17;

    // Listings Table Constants
    public static final String TABLE_LISTINGS = "listings";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_CONDITION = "condition";
    public static final String COLUMN_IMAGE_RES = "image_resource_id";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_FACULTY = "faculty"; // NEW COLUMN
    public static final String COLUMN_SELLER_ID = "seller_id";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            String CREATE_STUDENT_TABLE = "CREATE TABLE Student ("
                    + "StuID INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "Name TEXT, "
                    + "Email TEXT UNIQUE, "
                    + "PhoneNum TEXT, "
                    + "Password TEXT, "
                    + "UserType TEXT);";
            db.execSQL(CREATE_STUDENT_TABLE);

            String CREATE_LISTINGS_TABLE = "CREATE TABLE " + TABLE_LISTINGS + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_TITLE + " TEXT,"
                    + COLUMN_PRICE + " TEXT,"
                    + COLUMN_CATEGORY + " TEXT,"
                    + COLUMN_CONDITION + " TEXT,"
                    + COLUMN_IMAGE_RES + " TEXT,"
                    + COLUMN_DESCRIPTION + " TEXT,"
                    + COLUMN_FACULTY + " TEXT," // Added faculty
                    + COLUMN_SELLER_ID + " INTEGER,"
                    + "FOREIGN KEY(" + COLUMN_SELLER_ID + ") REFERENCES Student(StuID)" + ")";
            db.execSQL(CREATE_LISTINGS_TABLE);

            db.execSQL("CREATE TABLE Buyer (BuyerID INTEGER PRIMARY KEY, BuyerBio TEXT, Date_Joined TEXT, FOREIGN KEY(BuyerID) REFERENCES Student(StuID));");
            db.execSQL("CREATE TABLE Seller (SellerID INTEGER PRIMARY KEY, StoreBio TEXT, FOREIGN KEY(SellerID) REFERENCES Student(StuID));");
            db.execSQL("CREATE TABLE Wishlist (WishListID INTEGER PRIMARY KEY AUTOINCREMENT, Date_Added TEXT, BuyerID INTEGER, ItemID INTEGER, FOREIGN KEY(BuyerID) REFERENCES Buyer(BuyerID), FOREIGN KEY(ItemID) REFERENCES " + TABLE_LISTINGS + "(" + COLUMN_ID + "));");

            db.execSQL("INSERT INTO Student (Name, Email, PhoneNum, Password, UserType) VALUES ('Awang Najib', 'najib123@gmail.com', '0123456789', 'najib123', 'B');");
            db.execSQL("INSERT INTO Student (Name, Email, PhoneNum, Password, UserType) VALUES ('Zaim', 'zaim@gmail.com', '0112233445', 'zaim123', 'S');");

            Log.d("DatabaseHelper", "Tables created successfully with Faculty column support");
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
        onCreate(db);
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
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error retrieving StuID", e);
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return stuId;
    }

    public boolean addUser(String username, String email, String phone, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Name", username);
        values.put("Email", email);
        values.put("PhoneNum", phone);
        values.put("Password", password);
        values.put("UserType", "B");
        long result = db.insert("Student", null, values);
        db.close();
        return result != -1;
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean isValid = false;
        try {
            cursor = db.query("Student", new String[]{"StuID"}, "Email=? AND Password=?", new String[]{email, password}, null, null, null);
            isValid = cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error checking student credentials", e);
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
            cursor = db.query("Student", new String[]{"Name"}, "Email=?", new String[]{email}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                username = cursor.getString(cursor.getColumnIndexOrThrow("Name"));
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error fetching student name", e);
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return username;
    }

    public Cursor getStudentProfileByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT Name, Email FROM Student WHERE Email = ?", new String[]{email});
    }

    public boolean insertListing(Listing listing) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, listing.getTitle());
        values.put(COLUMN_PRICE, listing.getPrice());
        values.put(COLUMN_CATEGORY, listing.getCategory());
        values.put(COLUMN_CONDITION, listing.getCondition());
        values.put(COLUMN_IMAGE_RES, listing.getImagePath());
        values.put(COLUMN_DESCRIPTION, listing.getDescription());
        values.put(COLUMN_FACULTY, listing.getFaculty()); // Add faculty data
        values.put(COLUMN_SELLER_ID, listing.getSellerId());

        long result = -1;
        try {
            result = db.insert(TABLE_LISTINGS, null, values);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error inserting listing", e);
        } finally {
            db.close();
        }
        return result != -1;
    }

    public Cursor getListingWithSellerPhone(int itemId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + TABLE_LISTINGS + ".*, Student.Name, Student.PhoneNum FROM " + TABLE_LISTINGS + " "
                + "JOIN Student ON " + TABLE_LISTINGS + "." + COLUMN_SELLER_ID + " = Student.StuID "
                + "WHERE " + TABLE_LISTINGS + "." + COLUMN_ID + " = ?";
        return db.rawQuery(query, new String[]{String.valueOf(itemId)});
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
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRICE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONDITION)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_RES)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FACULTY)), // Retrieve faculty
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SELLER_ID))
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
        values.put("Date_Added", "2026-07-07");
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

    // Retrieves ONLY the listings belonging to a specific user
    public Cursor getMyListings(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Filters the listings table by the logged-in user's ID
        return db.rawQuery("SELECT * FROM " + TABLE_LISTINGS + " WHERE " + COLUMN_SELLER_ID + " = ?",
                new String[]{String.valueOf(userId)});
    }

    // Retrieves ONLY the count of listings for a specific user
    public int getMyListingsCount(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_LISTINGS + " WHERE " + COLUMN_SELLER_ID + " = ?",
                new String[]{String.valueOf(userId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
}

