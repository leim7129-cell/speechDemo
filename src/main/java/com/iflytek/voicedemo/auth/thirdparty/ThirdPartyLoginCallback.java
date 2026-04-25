package com.iflytek.voicedemo.auth.thirdparty;

public interface ThirdPartyLoginCallback {
    void onSuccess(String openId, String accessToken);
    void onCancel();
    void onError(String message);
}
