package com.iflytek.voicedemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.iflytek.voicedemo.auth.LoginActivity;
import com.iflytek.voicedemo.auth.session.SessionManager;

public class WelcomeActivity extends android.app.Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (SessionManager.getInstance(this).isLoggedIn()) {
            navigateToMain();
            return;
        }

        setContentView(R.layout.activity_welcome);

        CheckBox cbAgree = findViewById(R.id.cb_agree);
        Button btnToLogin = findViewById(R.id.btn_to_login);

        btnToLogin.setText(R.string.auth_btn_to_login);

        btnToLogin.setOnClickListener(v -> {
            if (cbAgree.isChecked()) {
                Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(WelcomeActivity.this, "请先勾选并同意用户协议", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
