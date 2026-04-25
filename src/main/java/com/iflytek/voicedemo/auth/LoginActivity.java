package com.iflytek.voicedemo.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.iflytek.voicedemo.auth.thirdparty.QqLoginHelper;
import com.iflytek.voicedemo.auth.thirdparty.ThirdPartyLoginCallback;
import com.iflytek.voicedemo.auth.thirdparty.WechatLoginHelper;

public class LoginActivity extends AppCompatActivity {

    private EditText etPhone, etSmsCode;
    private Button btnSendCode, btnLogin;
    private CheckBox cbPrivacy;
    private CountDownTimer countDownTimer;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SessionManager.getInstance(this).isLoggedIn()) {
            navigateToMain();
            return;
        }

        setContentView(R.layout.activity_login_new);
        authService = AuthService.getInstance(this);

        initViews();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btn_back);
        etPhone = findViewById(R.id.et_phone);
        etSmsCode = findViewById(R.id.et_sms_code);
        btnSendCode = findViewById(R.id.btn_send_code);
        btnLogin = findViewById(R.id.btn_login);
        cbPrivacy = findViewById(R.id.cb_privacy);
        TextView tvRegister = findViewById(R.id.tv_register);
        TextView tvForgotPassword = findViewById(R.id.tv_forgot_password);
        ImageView btnQqLogin = findViewById(R.id.btn_qq_login);
        ImageView btnWechatLogin = findViewById(R.id.btn_wechat_login);

        btnBack.setOnClickListener(v -> finish());
        btnSendCode.setOnClickListener(v -> sendSmsCode());
        btnLogin.setOnClickListener(v -> loginByPhone());
        tvRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        tvForgotPassword.setOnClickListener(v -> startActivity(new Intent(this, ResetPasswordActivity.class)));
        btnQqLogin.setOnClickListener(v -> loginByQq());
        btnWechatLogin.setOnClickListener(v -> loginByWechat());
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
                Toast.makeText(LoginActivity.this,
                        R.string.auth_code_sent + " (模拟: " + result.getMockCode() + ")",
                        Toast.LENGTH_LONG).show();
                startCountDown();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
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

    private void loginByPhone() {
        if (!cbPrivacy.isChecked()) {
            Toast.makeText(this, R.string.auth_error_privacy_first, Toast.LENGTH_SHORT).show();
            return;
        }

        String phone = etPhone.getText().toString().trim();
        String code = etSmsCode.getText().toString().trim();

        if (!isValidPhone(phone)) {
            Toast.makeText(this, R.string.auth_error_phone_format, Toast.LENGTH_SHORT).show();
            return;
        }
        if (code.length() != 6) {
            Toast.makeText(this, R.string.auth_error_code_format, Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        authService.loginByPhone(phone, code, new AuthService.ApiCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult result) {
                SessionManager.getInstance(LoginActivity.this).createSession(result.getToken(), result.getUser());
                Toast.makeText(LoginActivity.this, R.string.auth_success_login, Toast.LENGTH_SHORT).show();
                navigateToMain();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                btnLogin.setEnabled(true);
            }
        });
    }

    private void loginByQq() {
        if (!cbPrivacy.isChecked()) {
            Toast.makeText(this, R.string.auth_error_privacy_first, Toast.LENGTH_SHORT).show();
            return;
        }

        QqLoginHelper.login(this, new ThirdPartyLoginCallback() {
            @Override
            public void onSuccess(String openId, String accessToken) {
                authService.loginByQq(openId, accessToken, new AuthService.ApiCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult result) {
                        SessionManager.getInstance(LoginActivity.this).createSession(result.getToken(), result.getUser());
                        Toast.makeText(LoginActivity.this, R.string.auth_success_login, Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancel() {}

            @Override
            public void onError(String message) {
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginByWechat() {
        if (!cbPrivacy.isChecked()) {
            Toast.makeText(this, R.string.auth_error_privacy_first, Toast.LENGTH_SHORT).show();
            return;
        }

        WechatLoginHelper.login(this, new ThirdPartyLoginCallback() {
            @Override
            public void onSuccess(String openId, String accessToken) {
                authService.loginByWechat(openId, accessToken, new AuthService.ApiCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult result) {
                        SessionManager.getInstance(LoginActivity.this).createSession(result.getToken(), result.getUser());
                        Toast.makeText(LoginActivity.this, R.string.auth_success_login, Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancel() {}

            @Override
            public void onError(String message) {
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
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
