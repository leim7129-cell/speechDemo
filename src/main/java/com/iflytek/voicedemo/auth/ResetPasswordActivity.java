package com.iflytek.voicedemo.auth;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.iflytek.voicedemo.R;
import com.iflytek.voicedemo.auth.api.AuthService;
import com.iflytek.voicedemo.auth.model.LoginResult;
import com.iflytek.voicedemo.auth.model.SmsCodeResult;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText etPhone, etSmsCode, etNewPassword, etConfirmPassword;
    private Button btnSendCode, btnReset;
    private CountDownTimer countDownTimer;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        authService = AuthService.getInstance(this);

        ImageView btnBack = findViewById(R.id.btn_back);
        etPhone = findViewById(R.id.et_phone);
        etSmsCode = findViewById(R.id.et_sms_code);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnSendCode = findViewById(R.id.btn_send_code);
        btnReset = findViewById(R.id.btn_reset);
        TextView tvToLogin = findViewById(R.id.tv_to_login);

        btnBack.setOnClickListener(v -> finish());
        btnSendCode.setOnClickListener(v -> sendSmsCode());
        btnReset.setOnClickListener(v -> resetPassword());
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
                Toast.makeText(ResetPasswordActivity.this,
                        R.string.auth_code_sent + " (模拟: " + result.getMockCode() + ")",
                        Toast.LENGTH_LONG).show();
                startCountDown();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ResetPasswordActivity.this, message, Toast.LENGTH_SHORT).show();
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

    private void resetPassword() {
        String phone = etPhone.getText().toString().trim();
        String code = etSmsCode.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!isValidPhone(phone)) {
            Toast.makeText(this, R.string.auth_error_phone_format, Toast.LENGTH_SHORT).show();
            return;
        }
        if (code.length() != 6) {
            Toast.makeText(this, R.string.auth_error_code_format, Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPassword.length() < 6 || newPassword.length() > 20) {
            Toast.makeText(this, R.string.auth_hint_password, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, R.string.auth_error_password_mismatch, Toast.LENGTH_SHORT).show();
            return;
        }

        btnReset.setEnabled(false);
        authService.resetPassword(phone, code, newPassword, new AuthService.ApiCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult result) {
                Toast.makeText(ResetPasswordActivity.this, R.string.auth_success_reset, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ResetPasswordActivity.this, message, Toast.LENGTH_SHORT).show();
                btnReset.setEnabled(true);
            }
        });
    }

    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("1[3-9]\\d{9}");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
