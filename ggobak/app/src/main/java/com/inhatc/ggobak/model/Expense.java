package com.inhatc.ggobak.model;

import com.google.gson.annotations.SerializedName;

public class Expense {
    @SerializedName("amount")
    private int amount;
    
    @SerializedName("category")
    private String category;
    
    @SerializedName("date")
    private String date;
    
    @SerializedName("latitude")
    private double latitude;
    
    @SerializedName("longitude")
    private double longitude;
    
    @SerializedName("memo")
    private String memo;

    @SerializedName("isIncome")
    private boolean isIncome;

    // 기본 생성자
    public Expense() {}

    // 현재 사용하는 생성자
    public Expense(int amount, String category, String date, double latitude, double longitude, String memo, boolean isIncome) {
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
        this.memo = memo;
        this.isIncome = isIncome;
    }

    // Getters and Setters
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    
    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }

    public boolean isIncome() {
        return isIncome;
    }

    public void setIncome(boolean income) {
        isIncome = income;
    }
} 