package com.iflytek.voicedemo.auth.model;

import com.google.gson.Gson;

public class User {
    private String userId;
    private String nickname;
    private String avatarUrl;
    private String phone;
    private boolean phoneVerified;
    private String qqOpenId;
    private boolean qqBound;
    private String wechatOpenId;
    private boolean wechatBound;
    private String password;
    private long createdAt;
    private long lastLoginAt;

    public User(String userId, String nickname) {
        this.userId = userId;
        this.nickname = nickname;
        this.createdAt = System.currentTimeMillis();
        this.lastLoginAt = this.createdAt;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public boolean isPhoneVerified() { return phoneVerified; }
    public void setPhoneVerified(boolean phoneVerified) { this.phoneVerified = phoneVerified; }

    public String getQqOpenId() { return qqOpenId; }
    public void setQqOpenId(String qqOpenId) { this.qqOpenId = qqOpenId; }

    public boolean isQqBound() { return qqBound; }
    public void setQqBound(boolean qqBound) { this.qqBound = qqBound; }

    public String getWechatOpenId() { return wechatOpenId; }
    public void setWechatOpenId(String wechatOpenId) { this.wechatOpenId = wechatOpenId; }

    public boolean isWechatBound() { return wechatBound; }
    public void setWechatBound(boolean wechatBound) { this.wechatBound = wechatBound; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(long lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public int getBindCount() {
        int count = 0;
        if (phoneVerified) count++;
        if (qqBound) count++;
        if (wechatBound) count++;
        return count;
    }

    public String getMaskedPhone() {
        if (phone == null || phone.length() < 11) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    public String toJSON() {
        return new Gson().toJson(this);
    }

    public static User fromJSON(String json) {
        return new Gson().fromJson(json, User.class);
    }
}
