package com.qwwuyu.file.config

import com.qwwuyu.file.utils.SpUtils

class ManageConfig private constructor() {
    private var showPointFile: Boolean = SpUtils.getDefault().getValue(Constant.SP_SHOW_POINT_FILE, false);

    companion object {
        val instance: ManageConfig by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            ManageConfig()
        }
    }

    fun isShowPointFile(): Boolean {
        return showPointFile;
    }

    fun setShowPointFile(hide: Boolean) {
        SpUtils.getDefault().setValue(Constant.SP_SHOW_POINT_FILE, hide);
        showPointFile = hide;
    }
}