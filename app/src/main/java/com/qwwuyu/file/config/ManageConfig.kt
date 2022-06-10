package com.qwwuyu.file.config

import com.qwwuyu.file.utils.SpUtils

class ManageConfig private constructor() {
    private var showPointFile: Boolean = SpUtils.getDefault().getValue(Constant.SP_SHOW_POINT_FILE, false)
    private var autoWifi: Boolean = SpUtils.getDefault().getValue(Constant.SP_AUTO_WIFI, false)
    private var dirInfo: Boolean = SpUtils.getDefault().getValue(Constant.SP_DIR_INFO, false)
    private var showApk: Boolean = SpUtils.getDefault().getValue(Constant.SP_SHOW_APK, false)
    private var isMedia: Boolean = SpUtils.getDefault().getValue(Constant.SP_MEDIA, false)
    private var txtEncoding: String = SpUtils.getDefault().getValue(Constant.SP_TXT_ENCODING, "utf-8")

    companion object {
        @JvmStatic
        val instance: ManageConfig by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            ManageConfig()
        }
    }

    fun isShowPointFile(): Boolean {
        return showPointFile
    }

    fun setShowPointFile(hide: Boolean) {
        showPointFile = hide
        SpUtils.getDefault().setValue(Constant.SP_SHOW_POINT_FILE, hide)
    }

    fun isAutoWifi(): Boolean {
        return autoWifi
    }

    fun setAutoWifi(auto: Boolean) {
        autoWifi = auto
        SpUtils.getDefault().setValue(Constant.SP_AUTO_WIFI, auto)
    }

    fun isDirInfo(): Boolean {
        return dirInfo
    }

    fun setDirInfo(dirInfo: Boolean) {
        this.dirInfo = dirInfo
        SpUtils.getDefault().setValue(Constant.SP_DIR_INFO, dirInfo)
    }

    fun isShowApk(): Boolean {
        return showApk
    }

    fun setShowApk(apk: Boolean) {
        showApk = apk
        SpUtils.getDefault().setValue(Constant.SP_SHOW_APK, apk)
    }

    fun isMedia(): Boolean {
        return isMedia
    }

    fun setMedia(media: Boolean) {
        isMedia = media
        SpUtils.getDefault().setValue(Constant.SP_MEDIA, isMedia)
    }

    fun getTxtEncoding(): String {
        return txtEncoding
    }

    fun checkTxtEncoding() {
        txtEncoding = if ("utf-8" == txtEncoding) "gbk" else "utf-8"
        SpUtils.getDefault().setValue(Constant.SP_TXT_ENCODING, txtEncoding)
    }
}