package com.qwwuyu.file

import android.app.Application
import android.content.Context
import com.qwwuyu.file.utils.DisplayUtils
import com.qwwuyu.file.utils.LogUtils

class WApplication : Application() {
    companion object {
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        LogUtils.Builder().setLogTag("qfm").enableLogHead(true)
        DisplayUtils.init(this)
    }
}