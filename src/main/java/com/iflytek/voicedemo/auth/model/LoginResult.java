package com.iflytek.voicedemo.auth.model;

public class LoginResult {
    private boolean success;
    private String token;
    private User user;
    private String errorCode;
    private String errorMessage;

    public static LoginResult success(String token, User user) {
        LoginResult r = new LoginResult();
        r.success = true;
        r.token = token;
        r.user = user;
        return r;
    }

    public static LoginResult fail(String errorCode, String errorMessage) {
        LoginResult r = new LoginResult();
        r.success = false;
        r.errorCode = errorCode;
        r.errorMessage = errorMessage;
        return r;
    }

    public boolean isSuccess() { return success; }
    public String getToken() { return token; }
    public User getUser() { return user; }
    public String getErrorCode() { return errorCode; }
    public String getErrorMessage() { return errorMessage; }
}
