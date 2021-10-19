package com.qwwuyu.file

import android.app.Application
import android.content.Context
import com.qwwuyu.file.utils.CommUtils
import com.qwwuyu.file.utils.CrashUtils
import com.qwwuyu.file.utils.DisplayUtils
import com.qwwuyu.file.utils.LogUtils
import java.io.File

class WApplication : Application() {
    companion object {
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        val parent = if (CommUtils.isExternalCacheEnable(context)) externalCacheDir else cacheDir
        val logDir = File(parent, "logs")
        LogUtils.Builder().setLogTag("qfm").enableLogHead(true)/*.setLogDir(logDir.absolutePath)*/
        CrashUtils.init(context, logDir.absolutePath + CrashUtils.FILE_SEP + "crash" + CrashUtils.FILE_SEP)
        DisplayUtils.init(this)
    }
}