package com.qwwuyu.file.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.storage.StorageManager
import androidx.documentfile.provider.DocumentFile
import com.qwwuyu.file.WApplication
import com.qwwuyu.file.config.ManageConfig
import com.qwwuyu.file.utils.LogUtils
import java.util.*


/**
 * Created by qiwei on 2022/6/8.
 */
object RFileHelper {
    const val CODE_STORAGE_MANAGER = 12345
    var df: DocumentFile? = null

    private fun isR(): Boolean {
        return Build.VERSION.SDK_INT >= 30
    }

    fun init(): Boolean {
        if (!isR()) return true
        try {
            val androidRUri = ManageConfig.instance.getAndroidRUri()
            if (androidRUri.isNotEmpty()) {
                val cacheDf: DocumentFile? = DocumentFile.fromTreeUri(WApplication.context, Uri.parse(androidRUri))
                if (cacheDf != null && cacheDf.canRead() && cacheDf.canWrite()) {
                    df = cacheDf
                    return true
                }
            }
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
        }
        return false
    }

    fun requestAndroidData(activity: Activity) {
        if (!isR()) return
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

    fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?): Boolean? {
        if (!isR()) return null
        if (CODE_STORAGE_MANAGER == requestCode && Activity.RESULT_OK == resultCode) {
            data?.data?.let { uri ->
                val path = uri.toString().let { it.substring(it.indexOf("%3A") + 3).replace("%2F", "/").lowercase(Locale.getDefault()) }
                if ("android/data" != path && "android/data/" != path && "/android/data" != path) return false
                df = DocumentFile.fromTreeUri(WApplication.context, uri)
                activity.grantUriPermission(activity.packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                WApplication.context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                ManageConfig.instance.setAndroidRData(uri.toString())
                return true
            }
        }
        return null
    }
}