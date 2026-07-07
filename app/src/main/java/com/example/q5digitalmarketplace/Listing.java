package com.example.q5digitalmarketplace;

public class Listing {
    private int id; // Database row ID
    private final String title;
    private final String price;
    private final String category;
    private final String condition;
    private final String imagePath;
    private final String description;
    private final String faculty; // New field
    private int sellerId; // References Student.StuID

    // 1. Production Database Constructor (9 fields)
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

    // 2. Optimized Creation Constructor (8 fields)
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

    // 3. Main 6-field constructor (Preserved for compatibility)
    public Listing(String title, String price, String category, String condition, String imagePath, String description) {
        this.title = title;
        this.price = price;
        this.category = category;
        this.condition = condition;
        this.imagePath = imagePath;
        this.description = description;
        this.faculty = "General"; // Default fallback
        this.sellerId = -1;
    }

    // 4. Fallback constructor (5 fields)
    public Listing(String title, String price, String category, String condition, String imagePath) {
        this.title = title;
        this.price = price;
        this.category = category;
        this.condition = condition;
        this.imagePath = imagePath;
        this.description = "";
        this.faculty = "General";
        this.sellerId = -1;
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
    public String getFaculty() { return faculty; } // New getter
    public int getSellerId() { return sellerId; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }
}