package com.qwwuyu.file

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.qwwuyu.file.server.NanoServer
import com.qwwuyu.file.server.TempFileManagerImpl
import com.qwwuyu.file.utils.AppUtils
import com.qwwuyu.file.utils.FileUtils
import com.qwwuyu.file.utils.ToastUtil
import com.qwwuyu.file.utils.permit.PermitUtil
import com.qwwuyu.file.utils.permit.SPermitCtrl

class MainActivity : AppCompatActivity() {
    private lateinit var tvServer: TextView
    private lateinit var btn: TextView

    private var server: NanoServer? = null
    private var isStart = false
    private var port = 0

    private var networkReceiver: BroadcastReceiver? = null
    private val whatChange = 100
    private val whatAutoStart = 101

    @SuppressLint("HandlerLeak")
    private val handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                whatChange -> setIP()
                whatAutoStart -> {
                    if (!isStart) {
                        start()
                        sendEmptyMessageDelayed(whatAutoStart, 1000L)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_main)
        PermitUtil.request(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
            object : SPermitCtrl() {
                override fun onGranted() {
                    init()
                }

                override fun onDenied(granted: List<String>, onlyDenied: List<String>, foreverDenied: List<String>, denied: List<String>) {
                    ToastUtil.show("获取储存权限失败")
                    PermitUtil.openSetting(this@MainActivity, true)
                    finish()
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        stop()
        handler.removeCallbacksAndMessages(null)
        networkReceiver?.let {
            unregisterReceiver(it)
        }
    }

    private fun init() {
        tvServer = findViewById(R.id.txt_server)
        btn = findViewById(R.id.btn)
        btn.setOnClickListener {
            if (isStart) {
                stop()
            } else {
                start()
            }
        }

        val wifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = true
            handler.sendEmptyMessageDelayed(101, 2000L)
        }

        networkReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                handler.removeMessages(whatChange)
                handler.sendEmptyMessageDelayed(whatChange, 1000)
            }
        }
        val filters = IntentFilter()
        filters.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        filters.addAction("android.hardware.usb.action.USB_STATE")
        registerReceiver(networkReceiver, filters)
        start()
    }

    private fun start() {
        val ips = AppUtils.getCtrlIp()
        if (toast("请开启Wifi、热点、USB共享网络之一", ips.size == 0)) return
        if (toast("SD卡不存在或无法访问", !FileUtils.getInstance().isCreate)) return
        var port = 1764
        while (port < 1767) {
            try {
                server = NanoServer(this@MainActivity, port)
                server!!.start()
                server!!.tempFileManagerFactory = TempFileManagerImpl()
                break
            } catch (ignored: Exception) {
            }
            port++
        }
        if (toast("服务开启失败", port == 1767)) return
        isStart = true
        this.port = port
        setIP()
    }

    private fun stop() {
        server?.stop()
        isStart = false
        tvServer.text = "服务已关闭"
        tvServer.setTextColor(0xff0000ff.toInt())
        btn.text = "开启"
    }

    fun toast(text: String?, bl: Boolean): Boolean {
        if (bl) {
            tvServer.text = text
            tvServer.setTextColor(0xffff0000.toInt())
        }
        return bl
    }

    private fun setIP() {
        if (isStart) {
            val ips = AppUtils.getCtrlIp()
            var text = ""
            for (bean in ips) text = "$text${bean.toString(port)}" + "\n"
            if ("" != text) {
                tvServer.text = text.substring(0, text.length - 1)
            } else {
                tvServer.text = "请开启Wifi、热点、USB共享网络之一"
            }
            tvServer.setTextColor(0xff00ff00.toInt())
            btn.text = "关闭"
        }
    }
}