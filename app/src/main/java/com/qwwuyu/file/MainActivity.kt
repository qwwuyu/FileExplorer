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
import androidx.appcompat.app.AppCompatActivity
import com.qwwuyu.file.config.Constant
import com.qwwuyu.file.config.ManageConfig
import com.qwwuyu.file.helper.FileHelper
import com.qwwuyu.file.helper.KeepServer
import com.qwwuyu.file.nano.NanoServer
import com.qwwuyu.file.nano.TempFileManagerImpl
import com.qwwuyu.file.utils.AppUtils
import com.qwwuyu.file.utils.LogUtils
import com.qwwuyu.file.utils.SystemBarUtil
import com.qwwuyu.file.utils.ToastUtil
import com.qwwuyu.file.utils.permit.PermitUtil
import com.qwwuyu.file.utils.permit.SPermitCtrl
import kotlinx.android.synthetic.main.a_main.*

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    private var server: NanoServer? = null
    private var isStart = false
    private var openPort = 0

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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            if (intent.getBooleanExtra(Constant.INTENT_CLOSE, false)) {
                stop()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_main)
        tvVersion.text = "v${BuildConfig.VERSION_NAME}"
        SystemBarUtil.setStatusBarColor(this, AppUtils.getColor(R.color.white))
        SystemBarUtil.setStatusBarDarkMode(this, true)
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
        btnCtrl.setOnClickListener {
            if (isStart) {
                stop()
            } else {
                start()
            }
        }
        cbCtrl1.isChecked = ManageConfig.instance.isShowPointFile()
        cbCtrl1.setOnCheckedChangeListener { _, isChecked -> ManageConfig.instance.setShowPointFile(isChecked) }

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
        if (error("请开启Wifi、热点、USB共享网络之一", ips.isEmpty())) return
        if (error("储存卡不存在或无法访问", !FileHelper.getInstance().checkCreate())) return
        server?.stop()
        var port = 1764
        while (port < 1767) {
            try {
                server = NanoServer(this@MainActivity, port)
                server!!.tempFileManagerFactory = TempFileManagerImpl()
                server!!.start()
                break
            } catch (e: Exception) {
                LogUtils.logError(e)
            }
            port++
        }
        if (error("管理服务开启失败，端口被占用", port == 1767)) return
        isStart = true
        openPort = port
        setIP()
        KeepServer.start()
    }

    private fun stop() {
        server?.stop()
        server = null
        isStart = false
        tvMessage.text = "管理服务已关闭"
        tvMessage.setTextColor(AppUtils.getColor(R.color.message_normal))
        btnCtrl.text = "开启"
        KeepServer.stop()
    }

    private fun setIP() {
        if (!isStart) return
        val ips = AppUtils.getCtrlIp()
        var text = ""
        for (bean in ips) text = "$text${bean.toString(openPort)}" + "\n"
        if ("" != text) {
            tvMessage.text = text.substring(0, text.length - 1)
            tvMessage.setTextColor(AppUtils.getColor(R.color.message_normal))
        } else {
            tvMessage.text = "请开启Wifi、热点、USB共享网络之一"
            tvMessage.setTextColor(AppUtils.getColor(R.color.message_error))
        }
        btnCtrl.text = "关闭"
    }

    fun error(text: String?, show: Boolean): Boolean {
        if (show) {
            tvMessage.text = text
            tvMessage.setTextColor(AppUtils.getColor(R.color.message_error))
        }
        return show
    }
}