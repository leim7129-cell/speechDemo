package com.iflytek.voicedemo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogSQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "evaluation_logs.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "logs";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_SCORE = "score";
    public static final String COLUMN_ERROR_INFO = "error_info";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TIME + " TEXT, " +
                    COLUMN_SCORE + " TEXT, " +
                    COLUMN_ERROR_INFO + " TEXT);";

    public LogSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertLog(String time, String score, String errorInfo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TIME, time);
        values.put(COLUMN_SCORE, score);
        values.put(COLUMN_ERROR_INFO, errorInfo);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public List<Map<String, String>> getAllLogs() {
        List<Map<String, String>> logList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " ORDER BY id DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Map<String, String> log = new HashMap<>();
                log.put("time", cursor.getString(cursor.getColumnIndex(COLUMN_TIME)));
                log.put("score", cursor.getString(cursor.getColumnIndex(COLUMN_SCORE)));
                log.put("error", cursor.getString(cursor.getColumnIndex(COLUMN_ERROR_INFO)));
                logList.add(log);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return logList;
    }
}
