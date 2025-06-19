package com.inhatc.ggobak.api;

import com.inhatc.ggobak.model.Expense;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ExpenseService {
    @POST("/expense")
    Call<Expense> createExpense(@Header("Authorization") String token, @Body Expense expense);

    @GET("/expense/range")
    Call<List<Expense>> getExpensesByDateRange(
            @Header("Authorization") String token,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );
} 