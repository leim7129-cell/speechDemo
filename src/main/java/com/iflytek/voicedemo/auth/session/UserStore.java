package com.iflytek.voicedemo.auth.session;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.iflytek.voicedemo.UserDbHelper;
import com.iflytek.voicedemo.auth.model.User;

public class UserStore {
    private final UserDbHelper dbHelper;

    public UserStore(Context context) {
        dbHelper = new UserDbHelper(context.getApplicationContext());
    }

    public void saveUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.insertWithOnConflict(UserDbHelper.TABLE_USERS, null, toValues(user),
                SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void updateUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.update(UserDbHelper.TABLE_USERS, toValues(user),
                UserDbHelper.COL_USER_ID + "=?", new String[]{user.getUserId()});
        db.close();
    }

    public User getUserByPhone(String phone) {
        return querySingle(UserDbHelper.COL_PHONE + "=?", new String[]{phone});
    }

    public User getUserByQqOpenId(String openId) {
        return querySingle(UserDbHelper.COL_QQ_OPEN_ID + "=?", new String[]{openId});
    }

    public User getUserByWechatOpenId(String openId) {
        return querySingle(UserDbHelper.COL_WECHAT_OPEN_ID + "=?", new String[]{openId});
    }

    public User getUserById(String userId) {
        return querySingle(UserDbHelper.COL_USER_ID + "=?", new String[]{userId});
    }

    public void deleteUser(String userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(UserDbHelper.TABLE_USERS, UserDbHelper.COL_USER_ID + "=?",
                new String[]{userId});
        db.close();
    }

    private User querySingle(String selection, String[] args) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(UserDbHelper.TABLE_USERS, null, selection, args,
                null, null, null);
        User user = null;
        if (c.moveToFirst()) {
            user = fromCursor(c);
        }
        c.close();
        db.close();
        return user;
    }

    private ContentValues toValues(User u) {
        ContentValues v = new ContentValues();
        v.put(UserDbHelper.COL_USER_ID, u.getUserId());
        v.put(UserDbHelper.COL_NICKNAME, u.getNickname());
        v.put(UserDbHelper.COL_AVATAR_URL, u.getAvatarUrl());
        v.put(UserDbHelper.COL_PHONE, u.getPhone());
        v.put(UserDbHelper.COL_PHONE_VERIFIED, u.isPhoneVerified() ? 1 : 0);
        v.put(UserDbHelper.COL_QQ_OPEN_ID, u.getQqOpenId());
        v.put(UserDbHelper.COL_QQ_BOUND, u.isQqBound() ? 1 : 0);
        v.put(UserDbHelper.COL_WECHAT_OPEN_ID, u.getWechatOpenId());
        v.put(UserDbHelper.COL_WECHAT_BOUND, u.isWechatBound() ? 1 : 0);
        v.put(UserDbHelper.COL_PASSWORD, u.getPassword());
        v.put(UserDbHelper.COL_CREATED_AT, u.getCreatedAt());
        v.put(UserDbHelper.COL_LAST_LOGIN_AT, u.getLastLoginAt());
        return v;
    }

    private User fromCursor(Cursor c) {
        String userId = c.getString(c.getColumnIndexOrThrow(UserDbHelper.COL_USER_ID));
        String nickname = c.getString(c.getColumnIndexOrThrow(UserDbHelper.COL_NICKNAME));
        User u = new User(userId, nickname);
        u.setAvatarUrl(c.getString(c.getColumnIndexOrThrow(UserDbHelper.COL_AVATAR_URL)));
        u.setPhone(c.getString(c.getColumnIndexOrThrow(UserDbHelper.COL_PHONE)));
        u.setPhoneVerified(c.getInt(c.getColumnIndexOrThrow(UserDbHelper.COL_PHONE_VERIFIED)) == 1);
        u.setQqOpenId(c.getString(c.getColumnIndexOrThrow(UserDbHelper.COL_QQ_OPEN_ID)));
        u.setQqBound(c.getInt(c.getColumnIndexOrThrow(UserDbHelper.COL_QQ_BOUND)) == 1);
        u.setWechatOpenId(c.getString(c.getColumnIndexOrThrow(UserDbHelper.COL_WECHAT_OPEN_ID)));
        u.setWechatBound(c.getInt(c.getColumnIndexOrThrow(UserDbHelper.COL_WECHAT_BOUND)) == 1);
        u.setPassword(c.getString(c.getColumnIndexOrThrow(UserDbHelper.COL_PASSWORD)));
        u.setCreatedAt(c.getLong(c.getColumnIndexOrThrow(UserDbHelper.COL_CREATED_AT)));
        u.setLastLoginAt(c.getLong(c.getColumnIndexOrThrow(UserDbHelper.COL_LAST_LOGIN_AT)));
        return u;
    }
}
