package com.qwwuyu.file

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.qwwuyu.file.config.Constant
import com.qwwuyu.file.config.ManageConfig
import com.qwwuyu.file.helper.*
import com.qwwuyu.file.nano.NanoServer
import com.qwwuyu.file.utils.*
import kotlinx.android.synthetic.main.a_main.*

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    companion object {
        const val CODE_STORAGE = 100
    }

    private var server: NanoServer? = null
    private var isStart = false
    private var openPort = 0

    private var networkReceiver: BroadcastReceiver? = null
    private val whatChange = 100
    private val whatAutoStart = 101

    private val player: MediaPlayerHelper by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { MediaPlayerHelper() }

    @SuppressLint("HandlerLeak")
    private val handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                whatChange -> setIP()
                whatAutoStart -> if (!isStart) start()
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
        SystemBarUtil.setStatusBarColor(this, AppUtils.getColor(R.color.white))
        SystemBarUtil.setStatusBarDarkMode(this, true)

        cbShowPoint.isChecked = ManageConfig.instance.isShowPointFile()
        cbShowPoint.setOnCheckedChangeListener { _, isChecked -> ManageConfig.instance.setShowPointFile(isChecked) }
        cbAutoWifi.isChecked = ManageConfig.instance.isAutoWifi()
        cbAutoWifi.setOnCheckedChangeListener { _, isChecked -> ManageConfig.instance.setAutoWifi(isChecked) }
        cbDirInfo.isChecked = ManageConfig.instance.isDirInfo()
        cbDirInfo.setOnCheckedChangeListener { _, isChecked -> ManageConfig.instance.setDirInfo(isChecked) }
        cbApk.isChecked = ManageConfig.instance.isShowApk()
        cbApk.setOnCheckedChangeListener { _, isChecked -> ManageConfig.instance.setShowApk(isChecked) }
        cbMedia.isChecked = ManageConfig.instance.isMedia()
        cbMedia.setOnCheckedChangeListener { _, isChecked ->
            ManageConfig.instance.setMedia(isChecked)
            checkMedia()
        }

        btnNote.setOnClickListener { startActivity(Intent(this, NoteActivity::class.java)) }
        btnSetting.setOnClickListener { startActivity(Intent(this, SettingActivity::class.java)) }

        tvVersion.text = "v${BuildConfig.VERSION_NAME}"

        btnCtrl.setOnClickListener {
            requestStorage()
        }
        requestStorage()
    }

    private fun requestStorage() {
        if (PermitHelper.checkStorage(this, CODE_STORAGE)) {
            init()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stop()
        player.destroy()
        handler.removeCallbacksAndMessages(null)
        networkReceiver?.let { unregisterReceiver(it) }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CODE_STORAGE) {
            if (PermitHelper.checkStorageResult(this)) {
                init()
            }
        }
    }

    private fun init() {
        btnCtrl.setOnClickListener {
            if (isStart) stop() else start()
        }

        if (ManageConfig.instance.isAutoWifi()) {
            try {
                val wifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                if (!wifiManager.isWifiEnabled) {
                    wifiManager.isWifiEnabled = true
                    handler.sendEmptyMessageDelayed(whatAutoStart, 2000L)
                    handler.sendEmptyMessageDelayed(whatAutoStart, 4000L)
                    handler.sendEmptyMessageDelayed(whatAutoStart, 6000L)
                }
            } catch (e: Exception) {
            }
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
        if (error("创建文件传输缓存目录失败", !FileHelper.instance.checkCreate())) return
        handler.removeMessages(whatChange)
        server?.stop()
        var port = 1764
        while (port < 1767) {
            try {
                server = NanoServer(this@MainActivity, port)
                server!!.start()
                LogUtils.i("start")
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
        checkMedia()
    }

    private fun stop() {
        server?.stop()
        server = null
        isStart = false
        tvMessage.text = "管理服务已关闭"
        tvMessage.setTextColor(AppUtils.getColor(R.color.message_normal))
        btnCtrl.text = "开启"
        KeepServer.stop()
        checkMedia()
    }

    private fun setIP() {
        if (!isStart) return
        val ips = AppUtils.getCtrlIp()
        var text = ""
        for (bean in ips) text = "$text${bean.toString(openPort)}" + "\n"
        if ("" != text) {
            tvMessage.text = text.substring(0, text.length - 1)
            tvMessage.setTextColor(AppUtils.getColor(R.color.message_normal))
            HttpUtils.redirect(BuildConfig.REDIRECT_URL, BuildConfig.REDIRECT_KEY, ips[0].toLocation(openPort))
        } else {
            tvMessage.text = "请开启Wifi、热点、USB共享网络之一"
            tvMessage.setTextColor(AppUtils.getColor(R.color.message_error))
        }
        btnCtrl.text = "关闭"
    }

    private fun error(text: String?, show: Boolean): Boolean {
        if (show) {
            tvMessage.text = text
            tvMessage.setTextColor(AppUtils.getColor(R.color.message_error))
        }
        return show
    }

    private fun checkMedia() {
        if (isStart && ManageConfig.instance.isMedia()) {
            player.play()
        } else {
            player.stop()
        }
    }
}