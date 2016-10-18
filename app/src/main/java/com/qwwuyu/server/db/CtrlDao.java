package com.qwwuyu.server.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.qwwuyu.server.bean.CtrlBean;

import java.util.ArrayList;
import java.util.List;

public class CtrlDao {
    private CtrlDBOpenHelper helper;

    public CtrlDao(Context context) {
        helper = new CtrlDBOpenHelper(context);
    }

    public ArrayList<CtrlBean> findAllShop() {
        ArrayList<CtrlBean> list = new ArrayList<>();
        SQLiteDatabase sd = helper.getReadableDatabase();
        Cursor cursor = sd.rawQuery("select store_id,store_nam,goods_id,goods_name,goods_price,goods_num,goods_stcid,goods_image from shops", null);
        while (cursor.moveToNext()) {
            CtrlBean shop = new CtrlBean();
            shop.setId(cursor.getInt(0));
            list.add(shop);
        }
        cursor.close();
        sd.close();
        return list;
    }

    public void insert(List<CtrlBean> list) {
        SQLiteDatabase sd = helper.getWritableDatabase();
        for (CtrlBean shop : list) {
            ContentValues values = new ContentValues();
            values.put("id", shop.getId());
            sd.insert("shops", null, values);
        }
        sd.close();
    }

    /**
     * 删除所有物品
     */
    public void deleteAll() {
        SQLiteDatabase sd = helper.getWritableDatabase();
        sd.delete("shops", null, null);
        sd.close();
    }
}
