package com.inhatc.ggobak;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.inhatc.ggobak.api.ErrorResponse;
import com.inhatc.ggobak.api.RetrofitClient;
import com.inhatc.ggobak.model.LoginRequest;
import com.inhatc.ggobak.model.LoginResponse;
import com.inhatc.ggobak.model.User;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final String PREF_NAME = "AuthPrefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button signupButton;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // SharedPreferences 초기화
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // UI 요소 초기화
        emailEditText = findViewById(R.id.editTextTextEmailAddress);
        passwordEditText = findViewById(R.id.editTextTextPassword);
        loginButton = findViewById(R.id.button2);
        signupButton = findViewById(R.id.buttonSignup);

        // 로그인 버튼 클릭 리스너
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            // 입력값 검증
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "이메일과 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            // 로그인 요청
            LoginRequest loginRequest = new LoginRequest(email, password);
            RetrofitClient.getInstance()
                    .getApiService()
                    .login(loginRequest)
                    .enqueue(new Callback<LoginResponse>() {
                        @Override
                        public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                handleLoginResponse(response.body());
                            } else {
                                Toast.makeText(LoginActivity.this, "로그인에 실패했습니다", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<LoginResponse> call, Throwable t) {
                            Toast.makeText(LoginActivity.this, "네트워크 오류가 발생했습니다", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // 회원가입 버튼 클릭 리스너
        signupButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    private void handleLoginResponse(LoginResponse response) {
        if (response != null && response.getToken() != null && response.getUser() != null) {
            String token = response.getToken();
            long userId = response.getUser().getId();
            Log.d("LoginActivity", "Token received: " + token);
            Log.d("LoginActivity", "User ID received: " + userId);
            
            // 토큰과 사용자 ID 저장
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_TOKEN, token);
            editor.putLong(KEY_USER_ID, userId);
            boolean success = editor.commit();
            Log.d("LoginActivity", "Save success: " + success);

            // MainActivity로 이동
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(LoginActivity.this, "로그인에 실패했습니다", Toast.LENGTH_SHORT).show();
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
