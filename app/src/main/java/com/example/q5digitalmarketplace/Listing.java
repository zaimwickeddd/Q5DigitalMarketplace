package com.example.q5digitalmarketplace;

public class Listing {
    private int id;
    private String title;
    private String price;
    private String category;
    private String condition;
    private String imagePath;
    private String description;
    private String faculty;
    private int sellerId;

    // Default constructor for cases where ID and SellerID aren't immediately available
    public Listing(String title, String price, String category, String condition, String imagePath, String description) {
        this.title = title;
        this.price = price;
        this.category = category;
        this.condition = condition;
        this.imagePath = imagePath;
        this.description = description;
        this.faculty = "";
        this.sellerId = -1;
    }

    // Constructor with faculty and sellerId (no ID - e.g. before inserting to DB)
    public Listing(String title, String price, String category, String condition, String imagePath, String description, String faculty, int sellerId) {
        this.title = title;
        this.price = price;
        this.category = category;
        this.condition = condition;
        this.imagePath = imagePath;
        this.description = description;
        this.faculty = faculty;
        this.sellerId = sellerId;
    }

    // Full constructor (including ID and SellerID - e.g. when reading from DB)
    public Listing(int id, String title, String price, String category, String condition, String imagePath, String description, String faculty, int sellerId) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.category = category;
        this.condition = condition;
        this.imagePath = imagePath;
        this.description = description;
        this.faculty = faculty;
        this.sellerId = sellerId;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getPrice() { return price; }
    public String getCardPrice() { return price; }
    public String getCategory() { return category; }
    public String getCondition() { return condition; }
    public String getImagePath() { return imagePath; }
    public String getDescription() { return description; }
    public String getFaculty() { return faculty; }
    public int getSellerId() { return sellerId; }
}
