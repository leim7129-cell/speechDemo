package com.iflytek.voicedemo.auth.api;

import com.iflytek.voicedemo.auth.model.LoginResult;
import com.iflytek.voicedemo.auth.model.SmsCodeResult;
import com.iflytek.voicedemo.auth.model.User;

public interface AuthService {

    void sendSmsCode(String phone, ApiCallback<SmsCodeResult> callback);

    void loginByPhone(String phone, String code, ApiCallback<LoginResult> callback);

    void loginByQq(String openId, String accessToken, ApiCallback<LoginResult> callback);

    void loginByWechat(String openId, String accessToken, ApiCallback<LoginResult> callback);

    void register(String phone, String code, String nickname, ApiCallback<LoginResult> callback);

    void resetPassword(String phone, String code, String newPassword, ApiCallback<LoginResult> callback);

    void bindPhone(String phone, String code, ApiCallback<LoginResult> callback);

    void bindQq(String openId, String accessToken, ApiCallback<LoginResult> callback);

    void bindWechat(String openId, String accessToken, ApiCallback<LoginResult> callback);

    void unbind(int type, ApiCallback<LoginResult> callback);

    void getUserProfile(ApiCallback<User> callback);

    void logout(ApiCallback<Void> callback);

    interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    static AuthService getInstance(android.content.Context context) {
        if (ApiConfig.USE_MOCK) {
            return new MockAuthService(context);
        }
        throw new UnsupportedOperationException("真实API尚未配置，请先使用Mock模式");
    }
}
