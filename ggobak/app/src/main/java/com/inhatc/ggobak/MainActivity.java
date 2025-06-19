package com.inhatc.ggobak;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.inhatc.ggobak.api.ExpenseService;
import com.inhatc.ggobak.model.Expense;
import com.inhatc.ggobak.api.RetrofitClient;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String PREF_NAME = "AuthPrefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String BASE_URL = "http://10.0.2.2:8080/"; // 에뮬레이터용
    // private static final String BASE_URL = "http://192.168.0.xxx:8080/"; // 실제 기기용 (실제 서버 IP로 변경 필요)

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private EditText editTextDate;
    private EditText editTextAmount;
    private EditText editTextMemo;
    private Spinner categorySpinner;
    private Button buttonIn;
    private Button buttonOut;
    private Calendar calendar;
    private ExpenseService expenseService;
    private SharedPreferences sharedPreferences;

    private final String[] categories = {
            "용돈(입금)", "월급(입금)", "이자(입금)", "식비(출금)", "숙박(출금)", "문화(출금)", "디저트(출금)"
            , "선물(출금)", "회비(출금)", "의류(출금)", "의료(출금)", "교육(출금)", "저축(출금)"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // SharedPreferences 초기화
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String token = sharedPreferences.getString(KEY_TOKEN, null);
        Log.d("MainActivity", "Using token: " + token);

        // Retrofit 설정
        expenseService = RetrofitClient.getInstance().getExpenseService();

        // 위치 서비스 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // UI 요소 초기화
        editTextDate = findViewById(R.id.editTextDate);
        editTextAmount = findViewById(R.id.editTextAmount);
        editTextMemo = findViewById(R.id.editTextMemo);
        categorySpinner = findViewById(R.id.categorySpinner);
        buttonIn = findViewById(R.id.buttonIn);
        buttonOut = findViewById(R.id.buttonOut);

        // 카테고리 스피너 설정
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // 날짜 선택 다이얼로그 설정
        calendar = Calendar.getInstance();
        editTextDate.setOnClickListener(v -> showDatePickerDialog());

        // 지도 초기화
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // 위치 권한 체크 및 현재 위치 가져오기
        checkLocationPermission();

        // 입금 버튼 클릭 리스너
        buttonIn.setOnClickListener(v -> {
            if (currentLocation == null) {
                Toast.makeText(MainActivity.this, "위치 정보를 가져오는 중입니다", Toast.LENGTH_SHORT).show();
                return;
            }
            saveExpense(true);
        });

        // 출금 버튼 클릭 리스너
        buttonOut.setOnClickListener(v -> {
            if (currentLocation == null) {
                Toast.makeText(MainActivity.this, "위치 정보를 가져오는 중입니다", Toast.LENGTH_SHORT).show();
                return;
            }
            saveExpense(false);
        });

        // Initialize calendar button
        ImageView calendarButton = findViewById(R.id.calender);
        calendarButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void saveExpense(boolean isIncome) {
        String date = editTextDate.getText().toString();
        String amountStr = editTextAmount.getText().toString();
        String memo = editTextMemo.getText().toString();
        String category = categorySpinner.getSelectedItem().toString();

        if (date.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(MainActivity.this, "날짜와 금액을 입력해주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        // 금액 처리
        int amount;
        try {
            amount = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(MainActivity.this, "올바른 금액을 입력해주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = sharedPreferences.getString(KEY_TOKEN, null);
        Long userId = sharedPreferences.getLong(KEY_USER_ID, -1);
        Log.d("MainActivity", "Using token: " + token);
        Log.d("MainActivity", "Using userId: " + userId);

        if (token == null || userId == -1) {
            Toast.makeText(MainActivity.this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        // Expense 객체 생성
        Expense expense = new Expense(amount, category, date, currentLocation.getLatitude()
                , currentLocation.getLongitude(), memo, isIncome);
        Log.d("MainActivity", "Created expense: " + expense.getAmount() + ", " +
                expense.getCategory() + ", " + expense.getDate() + ", isIncome: " + expense.isIncome());

        // API 호출
        String authHeader = token;
        Log.d("MainActivity", "Using token: " + authHeader);
        expenseService.createExpense(authHeader, expense).enqueue(new Callback<Expense>() {
            @Override
            public void onResponse(Call<Expense> call, Response<Expense> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Expense savedExpense = response.body();
                    Log.d("MainActivity", "Expense saved successfully");
                    Log.d("MainActivity", "Amount: " + savedExpense.getAmount());
                    Log.d("MainActivity", "Category: " + savedExpense.getCategory());
                    Log.d("MainActivity", "Date: " + savedExpense.getDate());
                    Toast.makeText(MainActivity.this, "저장되었습니다", Toast.LENGTH_SHORT).show();
                    
                    // 입력 필드 초기화
                    clearInputs();
                    
                    // 지도 새로고침
                    loadExpensesForMap();
                } else {
                    Log.e("MainActivity", "Save failed: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e("MainActivity", "Error response: " + errorBody);
                        } catch (IOException e) {
                            Log.e("MainActivity", "Error reading error body", e);
                        }
                    }
                    Toast.makeText(MainActivity.this, "저장에 실패했습니다", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Expense> call, Throwable t) {
                Log.e("MainActivity", "Network error", t); // 에러 로깅
                Toast.makeText(MainActivity.this, "네트워크 오류가 발생했습니다", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearInputs() {
        editTextAmount.setText("");
        editTextMemo.setText("");
        updateDateEditText(); // 현재 날짜로 초기화
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateEditText();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateEditText() {
        String format = "yyyy-MM-dd";
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        editTextDate.setText(dateFormat.format(calendar.getTime()));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        
        // 현재 위치로 이동
        if (currentLocation != null) {
            LatLng location = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
        }

        // 현재 위치 버튼 활성화
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        // 지출 내역 로드
        loadExpensesForMap();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            currentLocation = location;
                            Log.d("MainActivity", "Current location: " + location.getLatitude() + ", " + location.getLongitude());
                            
                            // 지도가 이미 로드되어 있다면 현재 위치로 이동
                            if (mMap != null) {
                                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                            }
                        } else {
                            Log.e("MainActivity", "Location is null");
                        }
                    })
                    .addOnFailureListener(this, e -> {
                        Log.e("MainActivity", "Error getting location", e);
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "위치 권한이 필요합니다", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadExpensesForMap() {
        // 현재 날짜 기준으로 한 달 전 날짜 계산
        Calendar calendar = Calendar.getInstance();
        String endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
        
        calendar.add(Calendar.MONTH, -1);
        String startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());

        Log.d("MainActivity", "Loading expenses from " + startDate + " to " + endDate);

        String token = sharedPreferences.getString(KEY_TOKEN, null);
        if (token != null) {
            expenseService.getExpensesByDateRange(token, startDate, endDate)
                    .enqueue(new Callback<List<Expense>>() {
                        @Override
                        public void onResponse(Call<List<Expense>> call, Response<List<Expense>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                List<Expense> expenses = response.body();
                                Log.d("MainActivity", "Received " + expenses.size() + " expenses");
                                
                                // 기존 마커 제거
                                if (mMap != null) {
                                    mMap.clear();
                                }

                                // 각 지출에 대해 마커 추가
                                for (Expense expense : expenses) {
                                    LatLng location = new LatLng(expense.getLatitude(), expense.getLongitude());
                                    MarkerOptions markerOptions = new MarkerOptions()
                                            .position(location)
                                            .title(expense.getCategory())
                                            .snippet(String.format("%s원 - %s", 
                                                    NumberFormat.getNumberInstance().format(expense.getAmount()),
                                                    expense.getMemo()));
                                    
                                    // 카테고리에 "입금"이 포함되어 있으면 초록색, 아니면 빨간색
                                    if (expense.getCategory().contains("입금")) {
                                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                    } else {
                                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                    }
                                    
                                    mMap.addMarker(markerOptions);
                                }
                            } else {
                                Log.e("MainActivity", "Failed to load expenses: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Expense>> call, Throwable t) {
                            Log.e("MainActivity", "Error loading expenses", t);
                        }
                    });
        }
    }
}