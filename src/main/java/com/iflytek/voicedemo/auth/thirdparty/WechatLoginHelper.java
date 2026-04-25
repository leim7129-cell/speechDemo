package com.iflytek.voicedemo.auth.thirdparty;

import android.app.Activity;
import android.app.AlertDialog;

import java.util.UUID;

public class WechatLoginHelper {

    // 真实SDK需填写: private static final String WECHAT_APP_ID = "xxxxxx";
    // private static IWXAPI wxApi;
    // private static ThirdPartyLoginCallback pendingCallback;

    public static void login(Activity activity, ThirdPartyLoginCallback callback) {
        // 模拟微信登录：弹出对话框模拟授权
        new AlertDialog.Builder(activity)
                .setTitle("微信登录 (模拟)")
                .setMessage("这是微信登录的模拟界面。\n真实环境下将调用微信SDK进行授权。")
                .setPositiveButton("授权登录", (dialog, which) -> {
                    String openId = "wx_" + UUID.randomUUID().toString().substring(0, 8);
                    String accessToken = "wx_token_" + System.currentTimeMillis();
                    callback.onSuccess(openId, accessToken);
                })
                .setNegativeButton("取消", (dialog, which) -> callback.onCancel())
                .setCancelable(false)
                .show();

        // === 真实SDK实现（获取AppID后启用） ===
        // wxApi = WXAPIFactory.createWXAPI(activity, WECHAT_APP_ID, true);
        // wxApi.registerApp(WECHAT_APP_ID);
        // pendingCallback = callback;
        // SendAuth.Req req = new SendAuth.Req();
        // req.scope = "snsapi_userinfo";
        // req.state = "wechat_login_" + System.currentTimeMillis();
        // wxApi.sendReq(req);
    }

    public static void onWeChatAuthCode(String code) {
        // 真实SDK: 用code换取accessToken + openId
    }
}
