package com.qwwuyu.file.helper;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import com.qwwuyu.file.MainActivity;
import com.qwwuyu.file.WApplication;
import com.qwwuyu.file.config.Constant;
import com.qwwuyu.file.utils.LogUtils;
import com.qwwuyu.file.utils.NotificationUtils;

import androidx.annotation.Nullable;

public class KeepServer extends Service {
    private boolean foreground = false;

    @Override
    public void onCreate() {
        super.onCreate();
        PowerHelper.getInstance().acquire();
        if (!foreground) {
            foreground = true;
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(Constant.INTENT_CLOSE, true);
            startForeground(NotificationUtils.getManageNotifyId(), NotificationUtils.getManageNotify(intent));
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        PowerHelper.getInstance().release();
    }

    public static void start() {
        try {
            Intent intent = new Intent(WApplication.context, KeepServer.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WApplication.context.startForegroundService(intent);
            } else {
                WApplication.context.startService(intent);
            }
        } catch (Exception e) {
            LogUtils.logError(e);
        }
    }

    public static void stop() {
        try {
            WApplication.context.stopService(new Intent(WApplication.context, KeepServer.class));
        } catch (Exception e) {
            LogUtils.logError(e);
        }
    }
}
