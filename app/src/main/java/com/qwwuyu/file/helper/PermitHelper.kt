package com.qwwuyu.file.helper

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.storage.StorageManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.qwwuyu.file.utils.LogUtils

object PermitHelper {
    fun checkStorage(activity: Activity, requestCode: Int): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) return true
            AlertDialog.Builder(activity)
                .setTitle("文件管理需要储存权限,请设置.")
                .setCancelable(false)
                .setPositiveButton("去设置") { _, _ ->
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    activity.startActivity(intent)
                    activity.finish()
                }
                .setNegativeButton("取消") { _, _ -> activity.finish() }
                .show()
            return false
        } else {
            val write = Manifest.permission.WRITE_EXTERNAL_STORAGE
            val read = Manifest.permission.READ_EXTERNAL_STORAGE
            if (hasSelfPermissions(activity, write, read)) return true
            ActivityCompat.requestPermissions(activity, arrayOf(write, read), requestCode)
            return false
        }
    }

    fun checkStorageResult(activity: Activity): Boolean {
        val write = Manifest.permission.WRITE_EXTERNAL_STORAGE
        val read = Manifest.permission.READ_EXTERNAL_STORAGE
        if (hasSelfPermissions(activity, write, read)) return true
        val txt = if (foreverDenied(activity, write)) "永久" else ""
        AlertDialog.Builder(activity)
            .setTitle("文件管理需要储存权限,权限已被${txt}拒绝.")
            .setCancelable(false)
            .setPositiveButton("去设置") { _, _ ->
                openSetting(activity, true)
                activity.finish()
            }
            .setNegativeButton("取消") { _, _ -> activity.finish() }
            .show()
        return false
    }

    fun hasSelfPermissions(context: Context, vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    fun foreverDenied(activity: Activity, permission: String): Boolean {
        return !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    /** 打开设置  */
    fun openSetting(context: Context, newTask: Boolean) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri: Uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        if (newTask) intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    @SuppressLint("BatteryLife", "ObsoleteSdkInt")
    fun batteryOptimizations(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent()
                val packageName: String = context.packageName
                val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                    intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    intent.data = Uri.parse("package:$packageName")
                    context.startActivity(intent)
                }
            }
        } catch (_: Exception) {
        }
    }

    fun androidRData(activity: Activity, code: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val sm = activity.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            val intent: Intent = sm.primaryStorageVolume.createOpenDocumentTreeIntent()
            //String startDir = "Android";
            //String startDir = "Download"; // Not choosable on an Android 11 device
            //String startDir = "DCIM";
            //String startDir = "DCIM/Camera";  // replace "/", "%2F"
            //String startDir = "DCIM%2FCamera";
            // String startDir = "Documents";
            var startDir = "Android/data"
            var uri = intent.getParcelableExtra<Uri>("android.provider.extra.INITIAL_URI")
            var scheme = uri.toString()
            LogUtils.i("INITIAL_URI scheme: $scheme")
            scheme = scheme.replace("/root/", "/document/")
            startDir = startDir.replace("/", "%2F")
            scheme += "%3A$startDir"
            uri = Uri.parse(scheme)
            intent.putExtra("android.provider.extra.INITIAL_URI", uri)
            LogUtils.i("uri: $uri")
            activity.startActivityForResult(intent, code)
            return
        }
    }
}