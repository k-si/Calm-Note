package com.lsk.calm.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class NoteDatabase extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "notes";
    public static final String CONTENT = "content";
    public static final String ID = "id";
    public static final String TIME = "time";
    public static final String TAG = "tag";

    public NoteDatabase(@Nullable Context context) {
        super(context, "notes", null, 1);
    }

    // 相当于执行sql语句，创建表
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql = "CREATE TABLE " + TABLE_NAME
                + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + CONTENT + " TEXT NOT NULL,"
                + TIME + " TEXT NOT NULL,"
                + TAG + " INTEGER DEFAULT 1)";
        sqLiteDatabase.execSQL(sql);
    }

    // 用于更换表的版本
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // ...
    }

}
