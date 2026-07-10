package com.example.q5digitalmarketplace;

import java.io.Serializable; // 1. Added Import

public class Listing implements Serializable { // 2. Implemented Serializable

    // 3. Added Serial Version UID for consistent data transfer
    private static final long serialVersionUID = 1L;

    private int id;
    private final String title;
    private final String price;
    private final String category;
    private final String condition;
    private final String imagePath;
    private final String description;
    private final String faculty;
    private final String type;
    private int sellerId;
    private String status;

    // Production Database Constructor
    public Listing(int id, String title, String price, String category, String condition,
                   String imagePath, String description, String faculty, String type, int sellerId, String status) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.category = category;
        this.condition = condition;
        this.imagePath = imagePath;
        this.description = description;
        this.faculty = faculty;
        this.type = type;
        this.sellerId = sellerId;
        this.status = status;
    }

    // Optimized Creation Constructor
    public Listing(String title, String price, String category, String condition,
                   String imagePath, String description, String faculty, String type, int sellerId) {
        this.title = title;
        this.price = price;
        this.category = category;
        this.condition = condition;
        this.imagePath = imagePath;
        this.description = description;
        this.faculty = faculty;
        this.type = type;
        this.sellerId = sellerId;
        this.status = "Active";
    }

    // Compatibility Constructor
    public Listing(String title, String price, String category, String condition, String imagePath, String description) {
        this.title = title;
        this.price = price;
        this.category = category;
        this.condition = condition;
        this.imagePath = imagePath;
        this.description = description;
        this.faculty = "General";
        this.type = "Buy";
        this.sellerId = -1;
    }

    // Fallback constructor
    public Listing(String title, String price, String category, String condition, String imagePath) {
        this.title = title;
        this.price = price;
        this.category = category;
        this.condition = condition;
        this.imagePath = imagePath;
        this.description = "";
        this.faculty = "General";
        this.type = "Buy";
        this.sellerId = -1;
        this.status = "Active";
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
    public String getType() { return type; }
    public int getSellerId() { return sellerId; }
    public String getStatus() { return status != null ? status : "Active"; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }
    public void setStatus(String status) { this.status = status; }
}