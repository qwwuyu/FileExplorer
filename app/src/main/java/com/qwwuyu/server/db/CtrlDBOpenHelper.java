package com.qwwuyu.server.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CtrlDBOpenHelper extends SQLiteOpenHelper {
    public CtrlDBOpenHelper(Context context) {
        super(context, "ctrl.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table if not exists " + Field.ctrl_tb + "("
                + Field.id + " integer PRIMARY KEY autoincrement, "
                + Field.name + " varchar, "
                + ");";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}