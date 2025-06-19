package com.inhatc.ggobak;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.inhatc.ggobak.api.RetrofitClient;
import com.inhatc.ggobak.model.SignupRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {
    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextPasswordConfirm;
    private Button buttonSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        // TODO: signup.xml의 ID에 맞게 findViewById 수정 필요
        // 뷰 초기화
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextTextEmailAddress2);
        editTextPassword = findViewById(R.id.editTextTextPassword2);
        editTextPasswordConfirm = findViewById(R.id.editTextTextPassword3);
        buttonSignup = findViewById(R.id.button3);

        // 회원가입 버튼 클릭 리스너
        buttonSignup.setOnClickListener(v -> {
            String name = editTextName.getText().toString();
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();
            String passwordConfirm = editTextPasswordConfirm.getText().toString();

            // 입력값 검증
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty()) {
                Toast.makeText(SignupActivity.this, "모든 필드를 입력해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(passwordConfirm)) {
                Toast.makeText(SignupActivity.this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
                return;
            }

            // 회원가입 API 호출
            SignupRequest signupRequest = new SignupRequest(email, password, name);
            RetrofitClient.getInstance()
                    .getApiService()
                    .signup(signupRequest)
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(SignupActivity.this, "회원가입이 완료되었습니다", Toast.LENGTH_SHORT).show();
                                finish(); // 회원가입 성공 시 화면 종료
                            } else {
                                Toast.makeText(SignupActivity.this, "회원가입에 실패했습니다", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(SignupActivity.this, "서버 연결에 실패했습니다", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
} 