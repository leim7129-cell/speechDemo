package com.iflytek.voicedemo.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.iflytek.voicedemo.R;
import com.iflytek.voicedemo.WelcomeActivity;
import com.iflytek.voicedemo.auth.api.AuthService;
import com.iflytek.voicedemo.auth.model.LoginResult;
import com.iflytek.voicedemo.auth.model.SmsCodeResult;
import com.iflytek.voicedemo.auth.model.User;
import com.iflytek.voicedemo.auth.session.SessionManager;
import com.iflytek.voicedemo.auth.thirdparty.QqLoginHelper;
import com.iflytek.voicedemo.auth.thirdparty.ThirdPartyLoginCallback;
import com.iflytek.voicedemo.auth.thirdparty.WechatLoginHelper;

public class AccountManageActivity extends AppCompatActivity {

    private TextView tvNickname, tvUserId;
    private TextView tvPhoneStatus, tvQqStatus, tvWechatStatus;
    private TextView btnBindPhone, btnBindQq, btnBindWechat;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.getInstance(this).isLoggedIn()) {
            navigateToWelcome();
            return;
        }

        setContentView(R.layout.activity_account_manage);
        authService = AuthService.getInstance(this);

        initViews();
        refreshUI();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btn_back);
        tvNickname = findViewById(R.id.tv_nickname);
        tvUserId = findViewById(R.id.tv_user_id);
        tvPhoneStatus = findViewById(R.id.tv_phone_status);
        tvQqStatus = findViewById(R.id.tv_qq_status);
        tvWechatStatus = findViewById(R.id.tv_wechat_status);
        btnBindPhone = findViewById(R.id.btn_bind_phone);
        btnBindQq = findViewById(R.id.btn_bind_qq);
        btnBindWechat = findViewById(R.id.btn_bind_wechat);
        Button btnLogout = findViewById(R.id.btn_logout);
        TextView btnDeleteAccount = findViewById(R.id.btn_delete_account);

        btnBack.setOnClickListener(v -> finish());
        btnBindPhone.setOnClickListener(v -> handlePhoneBind());
        btnBindQq.setOnClickListener(v -> handleQqBind());
        btnBindWechat.setOnClickListener(v -> handleWechatBind());
        btnLogout.setOnClickListener(v -> showLogoutConfirm());
        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountConfirm());
    }

    private void refreshUI() {
        User user = SessionManager.getInstance(this).getCurrentUser();
        if (user == null) return;

        tvNickname.setText(user.getNickname());
        tvUserId.setText("ID: " + user.getUserId().substring(0, Math.min(8, user.getUserId().length())));

        if (user.isPhoneVerified() && user.getPhone() != null) {
            tvPhoneStatus.setText(user.getMaskedPhone());
            btnBindPhone.setText(R.string.auth_change);
        } else {
            tvPhoneStatus.setText(R.string.auth_not_bound);
            btnBindPhone.setText(R.string.auth_bind);
        }

        if (user.isQqBound()) {
            tvQqStatus.setText(R.string.auth_bound);
            btnBindQq.setText(R.string.auth_unbind);
        } else {
            tvQqStatus.setText(R.string.auth_not_bound);
            btnBindQq.setText(R.string.auth_bind);
        }

        if (user.isWechatBound()) {
            tvWechatStatus.setText(R.string.auth_bound);
            btnBindWechat.setText(R.string.auth_unbind);
        } else {
            tvWechatStatus.setText(R.string.auth_not_bound);
            btnBindWechat.setText(R.string.auth_bind);
        }
    }

    private void handlePhoneBind() {
        User user = SessionManager.getInstance(this).getCurrentUser();
        if (user == null) return;

        if (user.isPhoneVerified()) {
            // 已绑定 -> 弹出选择：更换或解绑
            new AlertDialog.Builder(this)
                    .setTitle("手机号管理")
                    .setItems(new CharSequence[]{"更换手机号", "解绑手机号"}, (dialog, which) -> {
                        if (which == 0) {
                            showBindPhoneDialog();
                        } else {
                            unbind(0);
                        }
                    })
                    .show();
        } else {
            showBindPhoneDialog();
        }
    }

    private void showBindPhoneDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 0);

        EditText etPhone = new EditText(this);
        etPhone.setHint("请输入手机号");
        etPhone.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        layout.addView(etPhone);

        EditText etCode = new EditText(this);
        etCode.setHint("请输入验证码");
        etCode.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etCode.setMaxLines(6);
        layout.addView(etCode);

        Button btnSend = new Button(this);
        btnSend.setText("获取验证码");
        btnSend.setAllCaps(false);
        layout.addView(btnSend);

        final String[] mockCode = {null};
        btnSend.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            if (!phone.matches("1[3-9]\\d{9}")) {
                Toast.makeText(this, R.string.auth_error_phone_format, Toast.LENGTH_SHORT).show();
                return;
            }
            authService.sendSmsCode(phone, new AuthService.ApiCallback<SmsCodeResult>() {
                @Override
                public void onSuccess(SmsCodeResult result) {
                    mockCode[0] = result.getMockCode();
                    Toast.makeText(AccountManageActivity.this,
                            "验证码已发送 (模拟: " + result.getMockCode() + ")",
                            Toast.LENGTH_LONG).show();
                    btnSend.setEnabled(false);
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(AccountManageActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        });

        new AlertDialog.Builder(this)
                .setTitle("绑定手机号")
                .setView(layout)
                .setPositiveButton("确定", (dialog, which) -> {
                    String phone = etPhone.getText().toString().trim();
                    String code = etCode.getText().toString().trim();
                    authService.bindPhone(phone, code, new AuthService.ApiCallback<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult result) {
                            SessionManager.getInstance(AccountManageActivity.this).updateUser(result.getUser());
                            Toast.makeText(AccountManageActivity.this, R.string.auth_success_bind, Toast.LENGTH_SHORT).show();
                            refreshUI();
                        }

                        @Override
                        public void onError(String message) {
                            Toast.makeText(AccountManageActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void handleQqBind() {
        User user = SessionManager.getInstance(this).getCurrentUser();
        if (user == null) return;

        if (user.isQqBound()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.auth_confirm_unbind)
                    .setPositiveButton("确定", (dialog, which) -> unbind(1))
                    .setNegativeButton("取消", null)
                    .show();
        } else {
            QqLoginHelper.login(this, new ThirdPartyLoginCallback() {
                @Override
                public void onSuccess(String openId, String accessToken) {
                    authService.bindQq(openId, accessToken, new AuthService.ApiCallback<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult result) {
                            SessionManager.getInstance(AccountManageActivity.this).updateUser(result.getUser());
                            Toast.makeText(AccountManageActivity.this, R.string.auth_success_bind, Toast.LENGTH_SHORT).show();
                            refreshUI();
                        }

                        @Override
                        public void onError(String message) {
                            Toast.makeText(AccountManageActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onCancel() {}

                @Override
                public void onError(String message) {
                    Toast.makeText(AccountManageActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void handleWechatBind() {
        User user = SessionManager.getInstance(this).getCurrentUser();
        if (user == null) return;

        if (user.isWechatBound()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.auth_confirm_unbind)
                    .setPositiveButton("确定", (dialog, which) -> unbind(2))
                    .setNegativeButton("取消", null)
                    .show();
        } else {
            WechatLoginHelper.login(this, new ThirdPartyLoginCallback() {
                @Override
                public void onSuccess(String openId, String accessToken) {
                    authService.bindWechat(openId, accessToken, new AuthService.ApiCallback<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult result) {
                            SessionManager.getInstance(AccountManageActivity.this).updateUser(result.getUser());
                            Toast.makeText(AccountManageActivity.this, R.string.auth_success_bind, Toast.LENGTH_SHORT).show();
                            refreshUI();
                        }

                        @Override
                        public void onError(String message) {
                            Toast.makeText(AccountManageActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onCancel() {}

                @Override
                public void onError(String message) {
                    Toast.makeText(AccountManageActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void unbind(int type) {
        authService.unbind(type, new AuthService.ApiCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult result) {
                SessionManager.getInstance(AccountManageActivity.this).updateUser(result.getUser());
                Toast.makeText(AccountManageActivity.this, R.string.auth_success_unbind, Toast.LENGTH_SHORT).show();
                refreshUI();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AccountManageActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLogoutConfirm() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.auth_confirm_logout)
                .setPositiveButton("确定", (dialog, which) -> {
                    SessionManager.getInstance(this).clearSession();
                    navigateToWelcome();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showDeleteAccountConfirm() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.auth_confirm_delete_account)
                .setPositiveButton("确定注销", (dialog, which) -> {
                    SessionManager.getInstance(this).clearSession();
                    navigateToWelcome();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void navigateToWelcome() {
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
