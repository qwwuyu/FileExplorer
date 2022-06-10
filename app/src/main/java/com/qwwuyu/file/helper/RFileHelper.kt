package com.qwwuyu.file.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.storage.StorageManager
import androidx.documentfile.provider.DocumentFile
import com.qwwuyu.file.WApplication
import com.qwwuyu.file.utils.LogUtils

/**
 * Created by qiwei on 2022/6/8.
 */
object RFileHelper {
    const val CODE_STORAGE_MANAGER = 12345
    var df: DocumentFile? = null

    fun isR(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    }

    fun requestAndroidData(activity: Activity) {
        if (isR()) {
            val sm = activity.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            val intent: Intent = sm.primaryStorageVolume.createOpenDocumentTreeIntent()
            //String startDir = "Android";
            //String startDir = "Download"; // Not choosable on an Android 11 device
            //String startDir = "DCIM";
            //String startDir = "DCIM/Camera";  // replace "/", "%2F"
            //String startDir = "DCIM%2FCamera";
            //String startDir = "Documents";

            var uri = intent.getParcelableExtra<Uri>("android.provider.extra.INITIAL_URI")
            var scheme = uri.toString()
            LogUtils.i("scheme: $scheme")
            scheme = scheme.replace("/root/", "/document/")

            var startDir = "Android/data"
            startDir = startDir.replace("/", "%2F")

            scheme += "%3A$startDir"
            uri = Uri.parse(scheme)
            LogUtils.i("uri: $uri")

            intent.putExtra("android.provider.extra.INITIAL_URI", uri)
            activity.startActivityForResult(intent, CODE_STORAGE_MANAGER)
            return
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (CODE_STORAGE_MANAGER == requestCode && Activity.RESULT_OK == resultCode) {
            data?.data?.let { df = DocumentFile.fromTreeUri(WApplication.context, it) }
        }
    }
}