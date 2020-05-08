package com.qwwuyu.file.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.qwwuyu.file.WApplication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


/**
 * sp数据操作工具类
 */
public class SpUtils {
    private static final String SP_NAME = "default";
    private static SpUtils spUtils;
    private SharedPreferences sp;

    public static SpUtils getDefault() {
        if (spUtils == null) {
            spUtils = new SpUtils(getContext(), SP_NAME, Context.MODE_PRIVATE);
        }
        return spUtils;
    }

    private static Context getContext() {
        return WApplication.context;
    }

    private SpUtils(Context context, String name, int mode) {
        sp = context.getApplicationContext().getSharedPreferences(name, mode);
    }

    /* ======================== apply ======================== */
    public void setValue(String valueKey, int value) {
        sp.edit().putInt(valueKey, value).apply();
    }

    public void setValue(String valueKey, float value) {
        sp.edit().putFloat(valueKey, value).apply();
    }

    public void setValue(String valueKey, String value) {
        sp.edit().putString(valueKey, value).apply();
    }

    public void setValue(String valueKey, boolean value) {
        sp.edit().putBoolean(valueKey, value).apply();
    }

    public void setValue(String valueKey, long value) {
        sp.edit().putLong(valueKey, value).apply();
    }

    public void setValue(String valueKey, Serializable value) {
        String objectString = objectToString(value);
        if (objectString != null) sp.edit().putString(valueKey, objectString).apply();
    }

    /* ======================== commit ======================== */
    public boolean commitValue(String valueKey, String value) {
        return sp.edit().putString(valueKey, value).commit();
    }

    /* ======================== get ======================== */
    public int getValue(String valueKey, int value) {
        return sp.getInt(valueKey, value);
    }

    public float getValue(String valueKey, float value) {
        return sp.getFloat(valueKey, value);
    }

    public String getValue(String valueKey, String value) {
        return sp.getString(valueKey, value);
    }

    public boolean getValue(String valueKey, boolean value) {
        return sp.getBoolean(valueKey, value);
    }

    public long getValue(String valueKey, long value) {
        return sp.getLong(valueKey, value);
    }

    public <T extends Serializable> T getValue(String key) {
        String objectString = sp.getString(key, null);
        try {
            return (T) stringToObject(objectString);
        } catch (Exception e) {
            return null;
        }
    }

    /* ======================== remove ======================== */
    public void clearKey(String valueKey) {
        sp.edit().remove(valueKey).apply();
    }

    public void clear() {
        sp.edit().clear().apply();
    }

    /* ========================  ======================== */
    private static Object stringToObject(String string) {
        ObjectInputStream objectInputStream = null;
        try {
            byte[] bytes = Base64.decode(string, Base64.NO_WRAP);
            objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
            return objectInputStream.readObject();
        } catch (Exception ignored) {
            return null;
        } finally {
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static String objectToString(Serializable object) {
        String encoded = null;
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.close();
            encoded = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.NO_WRAP);
        } catch (IOException ignored) {
        }
        return encoded;
    }
}
