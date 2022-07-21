package com.qwwuyu.file.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;

import com.qwwuyu.file.R;
import com.qwwuyu.file.WApplication;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * Created by qiwei on 2019/8/16.
 */
public class NotificationUtils {
    /** 默认通知类型 */
    private static final String CHANNEL = "应用通知";
    private static final String CHANNEL_MANAGE = "文件管理正在使用";
    /** 通知id 10000~10004 */
    private static final int NOTIFY_ID = 10000;
    private static final int NOTIFY_MANAGE = 10010;
    private static int currentId = 0;
    private static NotificationManager manager;
    private static Context context = WApplication.context;

    private static NotificationManager getManager() {
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        return manager;
    }

    //显示通知
    public static void showNotify(String title, String text, Intent intent) {
        int notifyId = NOTIFY_ID + ((currentId++) % 5);
        NotificationManager manager = getManager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL, CHANNEL, NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher))
                .setContentTitle(title)
                .setContentText(text)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setPriority(Notification.PRIORITY_MAX);
        if (intent != null) {
            builder.setContentIntent(PendingIntent.getActivity(context, notifyId, intent, PendingIntent.FLAG_CANCEL_CURRENT));
        }
        Notification notification = builder.build();
        manager.notify(notifyId, notification);
    }

    public static Notification getManageNotify(Intent intent) {
        NotificationManager manager = getManager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_MANAGE, CHANNEL_MANAGE, NotificationManager.IMPORTANCE_HIGH);
            channel.setShowBadge(false);
            manager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_MANAGE);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher));
        builder.setContentTitle("文件管理正在使用");
        builder.setContentText("点击关闭文件管理服务");
        builder.setPriority(Notification.PRIORITY_MAX);
        if (intent != null) {
            int flags;
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flags = PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_CANCEL_CURRENT;
            } else */if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags = PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT;
            } else {
                flags = PendingIntent.FLAG_CANCEL_CURRENT;
            }
            builder.setContentIntent(PendingIntent.getActivity(context, NOTIFY_MANAGE, intent, flags));
        }
        return builder.build();
    }

    public static boolean areNotificationsEnabled() {
        try {
            return NotificationManagerCompat.from(context).areNotificationsEnabled();
        } catch (Exception ignored) {
        }
        return true;
    }

    public static int getManageNotifyId() {
        return NOTIFY_MANAGE;
    }
}