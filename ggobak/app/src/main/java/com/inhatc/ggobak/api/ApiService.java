package com.inhatc.ggobak.api;

import com.inhatc.ggobak.model.LoginRequest;
import com.inhatc.ggobak.model.LoginResponse;
import com.inhatc.ggobak.model.SignupRequest;
import com.inhatc.ggobak.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/users/signin")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("/users/signup")
    Call<Void> signup(@Body SignupRequest request);

    @GET("/users/me")
    Call<User> getUserInfo(@Header("Authorization") String token);
} 