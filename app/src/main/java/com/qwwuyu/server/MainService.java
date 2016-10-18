package com.qwwuyu.server;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.qwwuyu.server.server.MyFileFactory;
import com.qwwuyu.server.server.MyServer;

public class MainService extends Service {
    private Handler handler;
    private Context context;
    private WifiManager.WifiLock wifiLock;
    private ServiceBinder binder = new ServiceBinder();
    private MyServer server;

    @Override
    public void onCreate() {
        handler = new Handler();
        context = this;
        int wifiLockType = WifiManager.WIFI_MODE_FULL;
        try {
            wifiLockType = WifiManager.class.getField("WIFI_MODE_FULL_HIGH_PERF").getInt(null);
        } catch (Exception ignored) {
        }
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        wifiLock = wifiManager.createWifiLock(wifiLockType, "qw");
        wifiLock.acquire();
        IntentFilter filters = new IntentFilter();
        filters.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filters.addAction("android.net.wifi.STATE_CHANGE");
        registerReceiver(networkReceiver, filters);
        new Thread(new Runnable() {
            @Override
            public void run() {
                start();
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    private void start() {
        int port = 1764;
        for (; port < 1770; port++) {
            try {
                server = new MyServer(context, port);
                server.start();
                server.setTempFileManagerFactory(new MyFileFactory());
                break;
            } catch (Exception ignored) {
            }
        }
        if (port == 1770) {

        }
//        Intent notificationIntent = new Intent(context, MainService.class);
//        notificationIntent.setAction(SHUTDOWN);
//        PendingIntent pIntent = PendingIntent.getService(this, 0, notificationIntent, 0);
//        Notification.Builder mBuilder = new Notification.Builder(this);
//        mBuilder.setContentTitle(intent.getStringExtra(EXTRA_NOTIFICATION_TITLE))
//                .setContentText("Tap to shutdown.")
//                .setContentIntent(pIntent)
//                .setTicker("Tap to shutdown.")
//                .setWhen(System.currentTimeMillis())
//                .setSmallIcon(R.mipmap.icon)
//                .setDefaults(0);
//        Notification notify = mBuilder.build();
//        startForeground(ONGOING_NOTIFICATION, notify);
    }

    @Override
    public void onDestroy() {
        if (wifiLock.isHeld()) wifiLock.release();
        super.onDestroy();
        unregisterReceiver(networkReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class ServiceBinder extends Binder {

    }

    private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };
}
