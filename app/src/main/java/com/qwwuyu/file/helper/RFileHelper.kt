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
import com.qwwuyu.file.entity.FileBean
import com.qwwuyu.file.utils.CommUtils
import com.qwwuyu.file.utils.LogUtils
import java.text.SimpleDateFormat
import java.util.*

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
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

    @JvmStatic
    fun getDirectoryFile(path: String): List<FileBean> {
        val list = mutableListOf<FileBean>()
        var df: DocumentFile? = df ?: return list
        if (!path.startsWith("/Android/data")) return list
        val dirs = path.split("/").drop(3)
        for (dir in dirs) {
            df = df?.let { check(it, dir) }
            if (df == null) break
        }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        df?.listFiles()?.forEach { cf ->
            val name = cf.name ?: ""
            if (ManageConfig.instance.isShowPointFile() || !name.startsWith(".")) {
                val date: String = dateFormat.format(Date(cf.lastModified()))
                val directory = cf.isDirectory
                val child = if (directory) cf.listFiles().size else 0
                val info = if (directory) "${child}项" else CommUtils.getFileSize(cf.length())
                val isInfo = !directory || ManageConfig.instance.isDirInfo()
                val fileBean = FileBean(name, cf.isDirectory,
                    if (isInfo) date else null, if (isInfo) info else null)
                fileBean.apk = ManageConfig.instance.isShowApk() && name.endsWith(".apk")
                list.add(fileBean)
            }
        }
        list.sortWith { lhs, rhs ->
            if (lhs.dir && !rhs.dir) -1
            else if (!lhs.dir && rhs.dir) 1
            else lhs.name.compareTo(rhs.name)
        }
        return list
    }

    private fun check(df: DocumentFile, dir: String): DocumentFile? {
        for (listFile in df.listFiles()) {
            if (listFile.isDirectory && listFile.name == dir) {
                return listFile
            }
        }
        return null
    }
}