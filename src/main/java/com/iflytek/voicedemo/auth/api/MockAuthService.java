package com.iflytek.voicedemo.auth.api;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.iflytek.voicedemo.auth.model.LoginResult;
import com.iflytek.voicedemo.auth.model.SmsCodeResult;
import com.iflytek.voicedemo.auth.model.User;
import com.iflytek.voicedemo.auth.session.SessionManager;
import com.iflytek.voicedemo.auth.session.UserStore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class MockAuthService implements AuthService {

    private final UserStore userStore;
    private final Context context;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Map<String, String> smsCodeMap = new HashMap<>();
    private final Random random = new Random();

    public MockAuthService(Context context) {
        this.context = context.getApplicationContext();
        userStore = new UserStore(context);
    }

    private void simulateDelay(Runnable r) {
        new Thread(() -> {
            try {
                Thread.sleep(500 + random.nextInt(1000));
            } catch (InterruptedException ignored) {}
            mainHandler.post(r);
        }).start();
    }

    private String generateToken() {
        return "tk_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
    }

    private String generateSmsCode() {
        return String.format("%06d", random.nextInt(1000000));
    }

    @Override
    public void sendSmsCode(String phone, ApiCallback<SmsCodeResult> callback) {
        simulateDelay(() -> {
            String code = generateSmsCode();
            smsCodeMap.put(phone, code);
            callback.onSuccess(SmsCodeResult.success(code));
        });
    }

    @Override
    public void loginByPhone(String phone, String code, ApiCallback<LoginResult> callback) {
        simulateDelay(() -> {
            String savedCode = smsCodeMap.get(phone);
            if (savedCode == null || !savedCode.equals(code)) {
                callback.onError("验证码错误");
                return;
            }
            User user = userStore.getUserByPhone(phone);
            if (user == null) {
                callback.onError("该手机号未注册");
                return;
            }
            user.setLastLoginAt(System.currentTimeMillis());
            userStore.updateUser(user);
            smsCodeMap.remove(phone);
            callback.onSuccess(LoginResult.success(generateToken(), user));
        });
    }

    @Override
    public void loginByQq(String openId, String accessToken, ApiCallback<LoginResult> callback) {
        simulateDelay(() -> {
            User user = userStore.getUserByQqOpenId(openId);
            if (user == null) {
                user = new User(UUID.randomUUID().toString(), "QQ用户" + (1000 + random.nextInt(9000)));
                user.setQqOpenId(openId);
                user.setQqBound(true);
                userStore.saveUser(user);
            }
            user.setLastLoginAt(System.currentTimeMillis());
            userStore.updateUser(user);
            callback.onSuccess(LoginResult.success(generateToken(), user));
        });
    }

    @Override
    public void loginByWechat(String openId, String accessToken, ApiCallback<LoginResult> callback) {
        simulateDelay(() -> {
            User user = userStore.getUserByWechatOpenId(openId);
            if (user == null) {
                user = new User(UUID.randomUUID().toString(), "微信用户" + (1000 + random.nextInt(9000)));
                user.setWechatOpenId(openId);
                user.setWechatBound(true);
                userStore.saveUser(user);
            }
            user.setLastLoginAt(System.currentTimeMillis());
            userStore.updateUser(user);
            callback.onSuccess(LoginResult.success(generateToken(), user));
        });
    }

    @Override
    public void register(String phone, String code, String nickname, ApiCallback<LoginResult> callback) {
        simulateDelay(() -> {
            String savedCode = smsCodeMap.get(phone);
            if (savedCode == null || !savedCode.equals(code)) {
                callback.onError("验证码错误");
                return;
            }
            if (userStore.getUserByPhone(phone) != null) {
                callback.onError("该手机号已注册");
                return;
            }
            User user = new User(UUID.randomUUID().toString(), nickname);
            user.setPhone(phone);
            user.setPhoneVerified(true);
            userStore.saveUser(user);
            smsCodeMap.remove(phone);
            callback.onSuccess(LoginResult.success(generateToken(), user));
        });
    }

    @Override
    public void resetPassword(String phone, String code, String newPassword, ApiCallback<LoginResult> callback) {
        simulateDelay(() -> {
            String savedCode = smsCodeMap.get(phone);
            if (savedCode == null || !savedCode.equals(code)) {
                callback.onError("验证码错误");
                return;
            }
            User user = userStore.getUserByPhone(phone);
            if (user == null) {
                callback.onError("该手机号未注册");
                return;
            }
            user.setPassword(newPassword);
            userStore.updateUser(user);
            smsCodeMap.remove(phone);
            callback.onSuccess(LoginResult.success(generateToken(), user));
        });
    }

    @Override
    public void bindPhone(String phone, String code, ApiCallback<LoginResult> callback) {
        simulateDelay(() -> {
            String savedCode = smsCodeMap.get(phone);
            if (savedCode == null || !savedCode.equals(code)) {
                callback.onError("验证码错误");
                return;
            }
            User current = getCurrentUser();
            if (current == null) {
                callback.onError("未登录");
                return;
            }
            User existing = userStore.getUserByPhone(phone);
            if (existing != null && !existing.getUserId().equals(current.getUserId())) {
                callback.onError("该手机号已绑定其他账号");
                return;
            }
            current.setPhone(phone);
            current.setPhoneVerified(true);
            userStore.updateUser(current);
            smsCodeMap.remove(phone);
            callback.onSuccess(LoginResult.success(generateToken(), current));
        });
    }

    @Override
    public void bindQq(String openId, String accessToken, ApiCallback<LoginResult> callback) {
        simulateDelay(() -> {
            User current = getCurrentUser();
            if (current == null) {
                callback.onError("未登录");
                return;
            }
            User existing = userStore.getUserByQqOpenId(openId);
            if (existing != null && !existing.getUserId().equals(current.getUserId())) {
                callback.onError("该QQ已绑定其他账号");
                return;
            }
            current.setQqOpenId(openId);
            current.setQqBound(true);
            userStore.updateUser(current);
            callback.onSuccess(LoginResult.success(generateToken(), current));
        });
    }

    @Override
    public void bindWechat(String openId, String accessToken, ApiCallback<LoginResult> callback) {
        simulateDelay(() -> {
            User current = getCurrentUser();
            if (current == null) {
                callback.onError("未登录");
                return;
            }
            User existing = userStore.getUserByWechatOpenId(openId);
            if (existing != null && !existing.getUserId().equals(current.getUserId())) {
                callback.onError("该微信已绑定其他账号");
                return;
            }
            current.setWechatOpenId(openId);
            current.setWechatBound(true);
            userStore.updateUser(current);
            callback.onSuccess(LoginResult.success(generateToken(), current));
        });
    }

    @Override
    public void unbind(int type, ApiCallback<LoginResult> callback) {
        simulateDelay(() -> {
            User current = getCurrentUser();
            if (current == null) {
                callback.onError("未登录");
                return;
            }
            if (current.getBindCount() <= 1) {
                callback.onError("至少保留一种登录方式");
                return;
            }
            switch (type) {
                case 0:
                    current.setPhone(null);
                    current.setPhoneVerified(false);
                    break;
                case 1:
                    current.setQqOpenId(null);
                    current.setQqBound(false);
                    break;
                case 2:
                    current.setWechatOpenId(null);
                    current.setWechatBound(false);
                    break;
            }
            userStore.updateUser(current);
            callback.onSuccess(LoginResult.success(generateToken(), current));
        });
    }

    @Override
    public void getUserProfile(ApiCallback<User> callback) {
        simulateDelay(() -> {
            User current = getCurrentUser();
            if (current == null) {
                callback.onError("未登录");
            } else {
                callback.onSuccess(current);
            }
        });
    }

    @Override
    public void logout(ApiCallback<Void> callback) {
        callback.onSuccess(null);
    }

    private User getCurrentUser() {
        return SessionManager.getInstance(context).getCurrentUser();
    }
}
