package com.qwwuyu.file.config

object Constant {
    /* ========================  ======================== */
    const val INTENT_CLOSE = "INTENT_CLOSE"

    /* ======================== response code ======================== */
    /** 处理请求成功  */
    const val HTTP_SUC = 1

    /** 处理请求失败  */
    const val HTTP_ERR = -1

    /* ======================== 接口 ======================== */
    const val URL_QUERY = "/i/query"
    const val URL_DEL = "/i/del"
    const val URL_DEL_DIR = "/i/delDir"
    const val URL_DOWNLOAD = "/i/download"
    const val URL_OPEN = "/i/open"
    const val URL_APK = "/i/apk"
    const val URL_UPLOAD = "/i/upload"
    const val URL_CREATE_DIR = "/i/createDir"

    const val URL_NOTE_QUERY = "/i/note/query"
    const val URL_NOTE_ADD = "/i/note/add"
    const val URL_NOTE_DEL = "/i/note/del"
    const val URL_NOTE_EDIT = "/i/note/edit"
    const val URL_NOTE_CLEAR = "/i/note/clear"
    const val URL_NOTE_COPY2PHONE = "/i/note/copy2phone"
    const val URL_NOTE_COPY2WEB = "/i/note/copy2web"

    /* ======================== SP ======================== */
    const val SP_SHOW_POINT_FILE = "SP_SHOW_POINT_FILE"
    const val SP_TXT_ENCODING = "SP_TXT_ENCODING"
    const val SP_AUTO_WIFI = "SP_AUTO_WIFI"
    const val SP_DIR_INFO = "SP_DIR_INFO"
    const val SP_SHOW_APK = "SP_SHOW_APK"
    const val SP_MEDIA = "SP_MEDIA"
    const val SP_ANDROID_R_URI = "SP_ANDROID_R_URI"
}