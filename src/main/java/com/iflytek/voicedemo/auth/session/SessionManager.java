package com.iflytek.voicedemo.auth.session;

import android.content.Context;
import android.content.SharedPreferences;

import com.iflytek.voicedemo.auth.model.User;
import com.google.gson.Gson;

public class SessionManager {
    private static final String PREF_NAME = "xingwai_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER_JSON = "user_json";
    private static final String KEY_USER_ID = "user_id";

    private static SessionManager instance;
    private SharedPreferences prefs;
    private final Gson gson = new Gson();

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null && context != null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }

    private SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void createSession(String token, User user) {
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_TOKEN, token)
                .putString(KEY_USER_JSON, gson.toJson(user))
                .putString(KEY_USER_ID, user.getUserId())
                .apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public User getCurrentUser() {
        String json = prefs.getString(KEY_USER_JSON, null);
        if (json == null) return null;
        return gson.fromJson(json, User.class);
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public void updateUser(User user) {
        prefs.edit()
                .putString(KEY_USER_JSON, gson.toJson(user))
                .apply();
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}
