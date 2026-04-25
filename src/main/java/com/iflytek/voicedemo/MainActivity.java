package com.iflytek.voicedemo;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.text.HtmlCompat;

import com.iflytek.speech.setting.TtsSettings;
import com.iflytek.voicedemo.auth.AccountManageActivity;
import com.iflytek.voicedemo.auth.session.SessionManager;
import com.iflytek.voicedemo.faceonline.OnlineFaceDemo;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences mSharedPreferences;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

        SpeechApp.initializeMsc(this);

        if (SessionManager.getInstance(this).isLoggedIn()) {
            initView();
            mSharedPreferences = getSharedPreferences(TtsSettings.PREFER_NAME, Activity.MODE_PRIVATE);
            boolean privacyConfirm = mSharedPreferences.getBoolean(SpeechApp.PRIVACY_KEY, false);
            if (!privacyConfirm) {
                showPrivacyDialog();
            }
        } else {
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!SessionManager.getInstance(this).isLoggedIn()) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private Intent intent;

    private void isPrivacyConfirm(Intent i) {
        boolean privacyConfirm = mSharedPreferences.getBoolean(SpeechApp.PRIVACY_KEY, false);
        if (privacyConfirm) {
            intent = i;
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                mPermissionRequest.launch(new String[]{
                        Manifest.permission.RECORD_AUDIO
                });
            } else {
                mPermissionRequest.launch(new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO,
                });
            }
        }
    }

    private void isPrivacyCameraConfirm(Intent i) {
        boolean privacyConfirm = mSharedPreferences.getBoolean(SpeechApp.PRIVACY_KEY, false);
        if (privacyConfirm) {
            intent = i;
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                mPermissionRequest.launch(new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                });
            } else {
                mPermissionRequest.launch(new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                });
            }
        }
    }

    private void initView() {
        findViewById(R.id.iseBtn).setOnClickListener(v -> {
            isPrivacyConfirm(new Intent(MainActivity.this, IseDemo.class));
        });
        findViewById(R.id.aiChatBtn).setOnClickListener(v -> {
            isPrivacyConfirm(new Intent(MainActivity.this, ScenarioSelectionActivity.class));
        });
        findViewById(R.id.accountManageBtn).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AccountManageActivity.class));
        });
    }

    private Toast mToast;

    private void showTip(final String str) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT);
        mToast.show();
    }

    private void showPrivacyDialog() {
        AppCompatTextView textView = new AppCompatTextView(this);
        textView.setPadding(100, 50, 100, 50);
        textView.setText(
                HtmlCompat.fromHtml("我们非常重视对您个人信息的保护，承诺严格按照讯飞开放平台<font color='#3B5FF5'>《隐私政策》</font>保护及处理您的信息，是否确定同意？",
                        HtmlCompat.FROM_HTML_MODE_LEGACY));
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://www.xfyun.cn/doc/policy/sdk_privacy.html"));
                startActivity(intent);
            }
        });
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("温馨提示")
                .setView(textView)
                .setPositiveButton("同意", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSharedPreferences.edit().putBoolean(SpeechApp.PRIVACY_KEY, true).apply();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("不同意", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSharedPreferences.edit().putBoolean(SpeechApp.PRIVACY_KEY, false).apply();
                        finish();
                        System.exit(0);
                    }
                })
                .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private ActivityResultLauncher<String[]> mPermissionRequest = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                Boolean audioGranted = result.get(Manifest.permission.RECORD_AUDIO);
                if (audioGranted == null || audioGranted) {
                    if (intent != null) {
                        MainActivity.this.startActivity(intent);
                    }
                } else {
                    showTip("由于缺少录音权限，部分功能可能无法使用");
                    if (intent != null) {
                        MainActivity.this.startActivity(intent);
                    }
                }
                SpeechApp.initializeMsc(MainActivity.this);
            });

}
