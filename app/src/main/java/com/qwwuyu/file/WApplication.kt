package com.qwwuyu.file

import android.app.Application
import android.content.Context
import com.qwwuyu.file.utils.LogUtils

class WApplication : Application() {
    companion object {
        lateinit var context: Context;
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        context = this;
    }

    override fun onCreate() {
        super.onCreate()
        LogUtils.Builder().setLogTag("qfm").enableLogHead(true)
    }
}