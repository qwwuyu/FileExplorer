package com.qwwuyu.server.utils;

import android.util.Log;

public class LogUtil {
    public static void i(Object obj) {
        if (obj != null) {
            Log.i("qw", obj.toString());
        }
    }
}
