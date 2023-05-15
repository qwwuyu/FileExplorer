package com.qwwuyu.file

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.qwwuyu.file.config.ManageConfig
import com.qwwuyu.file.helper.PermitHelper
import com.qwwuyu.file.helper.RFileHelper
import com.qwwuyu.file.utils.AppUtils
import com.qwwuyu.file.utils.SystemBarUtil
import com.qwwuyu.file.utils.ToastUtil
import kotlinx.android.synthetic.main.a_setting.*

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_setting)
        SystemBarUtil.setStatusBarColor(this, AppUtils.getColor(R.color.white))
        SystemBarUtil.setStatusBarDarkMode(this, true)

        tvEncoding.text = "txt预览编码方式：" + ManageConfig.instance.getTxtEncoding()
        btnEncoding.setOnClickListener {
            ManageConfig.instance.checkTxtEncoding()
            tvEncoding.text = "txt预览编码方式：" + ManageConfig.instance.getTxtEncoding()
        }

        btnBattery.setOnClickListener { PermitHelper.batteryOptimizations(this) }

        if (RFileHelper.init()) {
            btnRData.visibility = View.GONE
        } else {
            btnRData.setOnClickListener { RFileHelper.requestAndroidData(this) }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = RFileHelper.onActivityResult(this, requestCode, resultCode, data)
        if (result != null) {
            if (result) {
                btnRData.visibility = View.GONE
            } else {
                ToastUtil.show("请选择Android/data目录")
            }
        }
    }
}