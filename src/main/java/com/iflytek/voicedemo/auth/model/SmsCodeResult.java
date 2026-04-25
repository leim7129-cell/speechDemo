package com.iflytek.voicedemo.auth.model;

public class SmsCodeResult {
    private boolean success;
    private String mockCode;
    private String errorMessage;

    public static SmsCodeResult success(String mockCode) {
        SmsCodeResult r = new SmsCodeResult();
        r.success = true;
        r.mockCode = mockCode;
        return r;
    }

    public static SmsCodeResult fail(String errorMessage) {
        SmsCodeResult r = new SmsCodeResult();
        r.success = false;
        r.errorMessage = errorMessage;
        return r;
    }

    public boolean isSuccess() { return success; }
    public String getMockCode() { return mockCode; }
    public String getErrorMessage() { return errorMessage; }
}
