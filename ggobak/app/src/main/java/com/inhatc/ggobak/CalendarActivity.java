package com.inhatc.ggobak;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.inhatc.ggobak.api.ExpenseService;
import com.inhatc.ggobak.model.Expense;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CalendarActivity extends AppCompatActivity {
    private static final String PREF_NAME = "AuthPrefs";
    private static final String KEY_TOKEN = "token";
    private GestureDetectorCompat gestureDetector;
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;
    
    private CalendarView calendarView;
    private TextView selectedDateText;
    private RecyclerView expenseRecyclerView;
    private ExpenseAdapter expenseAdapter;
    private ExpenseService expenseService;
    private SimpleDateFormat dateFormat;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar);

        // 제스처 디텍터 초기화
        gestureDetector = new GestureDetectorCompat(this, new GestureListener());

        // 날짜 포맷 초기화
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        // Retrofit 초기화
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        expenseService = retrofit.create(ExpenseService.class);

        // 뷰 초기화
        calendarView = findViewById(R.id.calendarView);
        selectedDateText = findViewById(R.id.selectedDateText);
        expenseRecyclerView = findViewById(R.id.expenseRecyclerView);
        
        // RecyclerView 설정
        expenseAdapter = new ExpenseAdapter(new ArrayList<>());
        expenseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        expenseRecyclerView.setAdapter(expenseAdapter);

        // 오늘 날짜로 초기화
        Calendar calendar = Calendar.getInstance();
        selectedDate = dateFormat.format(calendar.getTime());
        updateSelectedDateText(selectedDate);
        loadExpensesForDate(selectedDate);

        // 달력 날짜 선택 리스너
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(year, month, dayOfMonth);
            selectedDate = dateFormat.format(selectedCalendar.getTime());
            updateSelectedDateText(selectedDate);
            loadExpensesForDate(selectedDate);
        });
    }

    private void updateSelectedDateText(String date) {
        try {
            Date parsedDate = dateFormat.parse(date);
            SimpleDateFormat displayFormat = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault());
            selectedDateText.setText(displayFormat.format(parsedDate));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadExpensesForDate(String date) {
        String token = getSharedPreferences(PREF_NAME, MODE_PRIVATE).getString(KEY_TOKEN, "");
        Log.d("CalendarActivity", "Token: " + token);
        
        if (token.isEmpty()) {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Call<List<Expense>> call = expenseService.getExpensesByDateRange("Bearer " + token, date, date);
        call.enqueue(new Callback<List<Expense>>() {
            @Override
            public void onResponse(Call<List<Expense>> call, Response<List<Expense>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    expenseAdapter.updateExpenses(response.body());
                } else {
                    Log.e("CalendarActivity", "Error response: " + response.code());
                    Toast.makeText(CalendarActivity.this, "데이터를 불러오는데 실패했습니다", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Expense>> call, Throwable t) {
                Log.e("CalendarActivity", "Network error", t);
                Toast.makeText(CalendarActivity.this, "서버 연결에 실패했습니다", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private class GestureListener extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1 == null || e2 == null) return false;

            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();

            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        // 오른쪽으로 스와이프
                        finish();
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        return true;
                    }
                }
            }
            return false;
        }
    }
} 