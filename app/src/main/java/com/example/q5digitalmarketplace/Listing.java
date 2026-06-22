package com.example.q5digitalmarketplace;

public class Listing {
    private String title;
    private String price;
    private String category;
    private String condition;
    private int imageResourceId; // Temporary local drawable resource ID instead of a hardcoded string URL

    public Listing(String title, String price, String category, String condition, int imageResourceId) {
        this.title = title;
        this.price = price;
        this.category = category;
        this.condition = condition;
        this.imageResourceId = imageResourceId;
    }

    // Getters
    public String getTitle() { return title; }
    public String getPrice() { return price; }
    public String getCategory() { return category; }
    public String getCondition() { return condition; }
    public int getImageResourceId() { return imageResourceId; }
}