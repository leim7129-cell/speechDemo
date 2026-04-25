package com.iflytek.voicedemo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "user_store.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "user_id";
    public static final String COL_NICKNAME = "nickname";
    public static final String COL_AVATAR_URL = "avatar_url";
    public static final String COL_PHONE = "phone";
    public static final String COL_PHONE_VERIFIED = "phone_verified";
    public static final String COL_QQ_OPEN_ID = "qq_open_id";
    public static final String COL_QQ_BOUND = "qq_bound";
    public static final String COL_WECHAT_OPEN_ID = "wechat_open_id";
    public static final String COL_WECHAT_BOUND = "wechat_bound";
    public static final String COL_PASSWORD = "password";
    public static final String COL_CREATED_AT = "created_at";
    public static final String COL_LAST_LOGIN_AT = "last_login_at";

    public UserDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " TEXT PRIMARY KEY, " +
                COL_NICKNAME + " TEXT, " +
                COL_AVATAR_URL + " TEXT, " +
                COL_PHONE + " TEXT, " +
                COL_PHONE_VERIFIED + " INTEGER DEFAULT 0, " +
                COL_QQ_OPEN_ID + " TEXT, " +
                COL_QQ_BOUND + " INTEGER DEFAULT 0, " +
                COL_WECHAT_OPEN_ID + " TEXT, " +
                COL_WECHAT_BOUND + " INTEGER DEFAULT 0, " +
                COL_PASSWORD + " TEXT, " +
                COL_CREATED_AT + " INTEGER, " +
                COL_LAST_LOGIN_AT + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
}
