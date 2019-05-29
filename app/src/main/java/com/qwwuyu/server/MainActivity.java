package com.qwwuyu.server;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.qwwuyu.server.bean.NetBean;
import com.qwwuyu.server.server.MyFileFactory;
import com.qwwuyu.server.server.MyServer;
import com.qwwuyu.server.utils.FileUtil;
import com.qwwuyu.server.utils.IPUtil;
import com.qwwuyu.server.utils.ToastUtil;

import java.util.ArrayList;

public class MainActivity extends Activity {
    private Context context = MainActivity.this;
    private MyServer server;
    private TextView txt_server;
    private TextView btn;
    private boolean isStart;
    private int port;
    private final int WHAT_CHANGES = 100;
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_CHANGES:
                    setIP();
                    break;
                case 101:
                    if (!isStart) start();
                    if (!isStart) handler.sendEmptyMessageDelayed(101, 1000L);
                    break;
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_main);
        txt_server = findViewById(R.id.txt_server);
        (btn = findViewById(R.id.btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStart) stop();
                else start();
            }
        });

        WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        this.handler.sendEmptyMessageDelayed(101, 2000L);

        IntentFilter filters = new IntentFilter();
        filters.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filters.addAction("android.hardware.usb.action.USB_STATE");
        registerReceiver(networkReceiver, filters);
        start();
    }

    private void start() {
        ArrayList<NetBean> ips = IPUtil.getCtrlIP();
        if (toast("请开启Wifi、热点、USB共享网络之一", ips.size() == 0)) return;
        if (toast("SD卡不存在或无法访问", !FileUtil.getInstance().isCreate())) return;
        int port = 1764;
        for (; port < 1767; port++) {
            try {
                server = new MyServer(context, port);
                server.start();
                server.setTempFileManagerFactory(new MyFileFactory());
                break;
            } catch (Exception ignored) {
            }
        }
        if (toast("服务开启失败", port == 1767)) return;
        isStart = true;
        this.port = port;
        setIP();
    }

    private void stop() {
        server.stop();
        isStart = false;
        txt_server.setText("服务已关闭");
        txt_server.setTextColor(0xff0000ff);
        btn.setText("Start");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isStart) server.stop();
        unregisterReceiver(networkReceiver);
    }

    public boolean toast(String text, boolean bl) {
        if (bl) ToastUtil.showToast(context, text);
        if (bl) txt_server.setText(text);
        if (bl) txt_server.setTextColor(0xffff0000);
        return bl;
    }

    private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handler.removeMessages(WHAT_CHANGES);
            handler.sendEmptyMessageDelayed(WHAT_CHANGES, 1000);
        }
    };

    private void setIP() {
        if (isStart) {
            ArrayList<NetBean> ips = IPUtil.getCtrlIP();
            String text = "";
            for (NetBean bean : ips) text = text + bean.toString(port) + "\n";
            if (!"".equals(text)) txt_server.setText(text.substring(0, text.length() - 1));
            else txt_server.setText("请开启Wifi、热点、USB共享网络之一");
            txt_server.setTextColor(0xff00ff00);
            btn.setText("Stop");
        }
    }
}
