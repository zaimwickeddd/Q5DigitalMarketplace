package com.example.q5digitalmarketplace;

public class Listing {
    private String title;
    private String price;
    private String category;
    private String condition;
    private String imagePath; // <-- Changed from int to String to hold dynamic gallery images
    private String description;

    // Fallback constructor
    public Listing(String title, String price, String category, String condition, String imagePath) {
        this.title = title;
        this.price = price;
        this.category = category;
        this.condition = condition;
        this.imagePath = imagePath;
        this.description = "";
    }

    // Main 6-field constructor
    public Listing(String title, String price, String category, String condition, String imagePath, String description) {
        this.title = title;
        this.price = price;
        this.category = category;
        this.condition = condition;
        this.imagePath = imagePath;
        this.description = description;
    }

    public String getTitle() { return title; }
    public String getPrice() { return price; }
    public String getCardPrice() { return price; }
    public String getCategory() { return category; }
    public String getCondition() { return condition; }
    public String getImagePath() { return imagePath; } // <-- Updated Getter name to match usage
    public String getDescription() { return description; }
}