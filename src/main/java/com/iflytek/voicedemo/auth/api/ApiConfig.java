package com.iflytek.voicedemo.auth.api;

public class ApiConfig {
    public static boolean USE_MOCK = true;

    public static final String BASE_URL = "https://api.xingwaifluyu.com/v1";

    public static final String PATH_SMS_SEND = "/auth/sms/send";
    public static final String PATH_SMS_VERIFY = "/auth/sms/verify";
    public static final String PATH_LOGIN_PHONE = "/auth/login/phone";
    public static final String PATH_LOGIN_QQ = "/auth/login/qq";
    public static final String PATH_LOGIN_WECHAT = "/auth/login/wechat";
    public static final String PATH_REGISTER = "/auth/register";
    public static final String PATH_RESET_PASSWORD = "/auth/reset-password";
    public static final String PATH_BIND_PHONE = "/auth/bind/phone";
    public static final String PATH_BIND_QQ = "/auth/bind/qq";
    public static final String PATH_BIND_WECHAT = "/auth/bind/wechat";
    public static final String PATH_UNBIND = "/auth/unbind";
    public static final String PATH_USER_PROFILE = "/user/profile";
    public static final String PATH_LOGOUT = "/auth/logout";
}
