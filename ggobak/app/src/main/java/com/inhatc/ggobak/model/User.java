package com.inhatc.ggobak.model;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("user_id")
    private Long userId;

    @SerializedName("email")
    private String email;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
} 