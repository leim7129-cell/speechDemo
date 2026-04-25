package com.iflytek.voicedemo.auth.thirdparty;

import android.app.Activity;
import android.app.AlertDialog;

import java.util.UUID;

public class QqLoginHelper {

    // 真实SDK需填写: private static final String QQ_APP_ID = "xxxxxx";

    public static void login(Activity activity, ThirdPartyLoginCallback callback) {
        // 模拟QQ登录：弹出对话框模拟授权
        new AlertDialog.Builder(activity)
                .setTitle("QQ登录 (模拟)")
                .setMessage("这是QQ登录的模拟界面。\n真实环境下将调用QQ SDK进行授权。")
                .setPositiveButton("授权登录", (dialog, which) -> {
                    String openId = "qq_" + UUID.randomUUID().toString().substring(0, 8);
                    String accessToken = "qq_token_" + System.currentTimeMillis();
                    callback.onSuccess(openId, accessToken);
                })
                .setNegativeButton("取消", (dialog, which) -> callback.onCancel())
                .setCancelable(false)
                .show();

        // === 真实SDK实现（获取AppID后启用） ===
        // Tencent mTencent = Tencent.createInstance(QQ_APP_ID, activity.getApplicationContext());
        // mTencent.login(activity, "get_user_info", new IUiListener() {
        //     @Override public void onComplete(Object response) {
        //         JSONObject jo = (JSONObject) response;
        //         String openId = jo.optString("openid");
        //         String accessToken = jo.optString("access_token");
        //         callback.onSuccess(openId, accessToken);
        //     }
        //     @Override public void onError(UiError e) { callback.onError(e.errorMessage); }
        //     @Override public void onCancel() { callback.onCancel(); }
        // });
    }

    public static void handleResultData(int requestCode, int resultCode, Object data) {
        // 真实SDK: Tencent.handleResultData(data, listener);
    }
}
