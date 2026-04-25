package com.iflytek.voicedemo.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.iflytek.voicedemo.MainActivity;
import com.iflytek.voicedemo.R;
import com.iflytek.voicedemo.auth.api.AuthService;
import com.iflytek.voicedemo.auth.model.LoginResult;
import com.iflytek.voicedemo.auth.model.SmsCodeResult;
import com.iflytek.voicedemo.auth.session.SessionManager;

public class RegisterActivity extends AppCompatActivity {

    private EditText etPhone, etSmsCode, etNickname;
    private Button btnSendCode, btnRegister;
    private CountDownTimer countDownTimer;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        authService = AuthService.getInstance(this);

        ImageView btnBack = findViewById(R.id.btn_back);
        etPhone = findViewById(R.id.et_phone);
        etSmsCode = findViewById(R.id.et_sms_code);
        etNickname = findViewById(R.id.et_nickname);
        btnSendCode = findViewById(R.id.btn_send_code);
        btnRegister = findViewById(R.id.btn_register);
        TextView tvToLogin = findViewById(R.id.tv_to_login);

        btnBack.setOnClickListener(v -> finish());
        btnSendCode.setOnClickListener(v -> sendSmsCode());
        btnRegister.setOnClickListener(v -> register());
        tvToLogin.setOnClickListener(v -> finish());
    }

    private void sendSmsCode() {
        String phone = etPhone.getText().toString().trim();
        if (!isValidPhone(phone)) {
            Toast.makeText(this, R.string.auth_error_phone_format, Toast.LENGTH_SHORT).show();
            return;
        }

        btnSendCode.setEnabled(false);
        authService.sendSmsCode(phone, new AuthService.ApiCallback<SmsCodeResult>() {
            @Override
            public void onSuccess(SmsCodeResult result) {
                Toast.makeText(RegisterActivity.this,
                        R.string.auth_code_sent + " (模拟: " + result.getMockCode() + ")",
                        Toast.LENGTH_LONG).show();
                startCountDown();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                btnSendCode.setEnabled(true);
            }
        });
    }

    private void startCountDown() {
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                btnSendCode.setText(getString(R.string.auth_code_resend, millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                btnSendCode.setText(R.string.auth_btn_send_code);
                btnSendCode.setEnabled(true);
            }
        }.start();
    }

    private void register() {
        String phone = etPhone.getText().toString().trim();
        String code = etSmsCode.getText().toString().trim();
        String nickname = etNickname.getText().toString().trim();

        if (!isValidPhone(phone)) {
            Toast.makeText(this, R.string.auth_error_phone_format, Toast.LENGTH_SHORT).show();
            return;
        }
        if (code.length() != 6) {
            Toast.makeText(this, R.string.auth_error_code_format, Toast.LENGTH_SHORT).show();
            return;
        }
        if (nickname.length() < 2 || nickname.length() > 16) {
            Toast.makeText(this, R.string.auth_error_nickname_format, Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setEnabled(false);
        authService.register(phone, code, nickname, new AuthService.ApiCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult result) {
                SessionManager.getInstance(RegisterActivity.this).createSession(result.getToken(), result.getUser());
                Toast.makeText(RegisterActivity.this, R.string.auth_success_register, Toast.LENGTH_SHORT).show();
                navigateToMain();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                btnRegister.setEnabled(true);
            }
        });
    }

    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("1[3-9]\\d{9}");
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
