package com.qwwuyu.file.helper;

import android.content.Context;
import android.os.PowerManager;
import android.provider.Settings;

import com.qwwuyu.file.WApplication;
import com.qwwuyu.file.utils.LogUtils;

/**
 * Power管理器
 */
public class PowerHelper {
    private static class Holder {
        public static PowerHelper instance = new PowerHelper();
    }

    public static PowerHelper getInstance() {
        return Holder.instance;
    }

    private PowerManager pm;
    private PowerManager.WakeLock pmLock = null;

    private PowerHelper() {
        try {
            pm = (PowerManager) WApplication.context.getSystemService(Context.POWER_SERVICE);
        } catch (Error e) {
            LogUtils.e("PowerHelper init error:" + e.getMessage());
        }
    }

    public void acquire() {
        release();
        try {
            pmLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "file:wake_lock");
            pmLock.acquire();
        } catch (Error e) {
            LogUtils.e("PowerHelper acquire acquire:" + e.getMessage());
        }
    }

    public void release() {
        try {
            if (pmLock != null) {
                pmLock.release();
                pmLock = null;
            }
        } catch (Error e) {
            LogUtils.e("PowerHelper release acquire:" + e.getMessage());
        }
    }
}
