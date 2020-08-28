package com.qwwuyu.file.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.provider.Settings;
import android.view.inputmethod.InputMethodManager;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

/**
 * 通用的工具类
 */
public class CommUtils {
    public static boolean isEmpty(CharSequence s) {
        return s == null || s.length() == 0;
    }

    public static boolean isExist(CharSequence s) {
        return s != null && s.length() != 0;
    }


    @Nullable
    public static Activity getActivity(Context context) {
        if (context == null) {
            return null;
        } else if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return getActivity(((ContextWrapper) context).getBaseContext());
        }
        return null;
    }

    public static String getVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception ignored) {
        }
        return "";
    }

    public static int getVersionCode(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception ignored) {
        }
        return 0;
    }

    /** 是否安装packageName的包 */
    public static boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA);//GET_UNINSTALLED_PACKAGES
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        } catch (Exception e) {//TransactionTooLargeException
            return true;
        }
    }

    /** 是否是主线程 */
    public static boolean isInMainProcess(Context context) {
        String processName = getProcessName(context, Process.myPid());
        String packageName = context.getPackageName();
        return processName == null || processName.length() == 0 || processName.equals(packageName);
    }

    /** 获取进程号对应的进程名 */
    private static String getProcessName(Context context, int pid) {
        try {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (activityManager != null) {
                for (ActivityManager.RunningAppProcessInfo procInfo : activityManager.getRunningAppProcesses()) {
                    if (procInfo.pid == pid) return procInfo.processName;
                }
            }
        } catch (Throwable ignored) {
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            return reader.readLine().trim();
        } catch (Throwable ignored) {
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public static boolean isExternalEnable(Context context) {
        try {
            return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && context.getExternalCacheDir() != null;
        } catch (Exception e) {
            return false;
        }
    }

    /** 安装apk */
    public static void installApk(Context context, File file, String fileProvider) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri;
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && context.getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.Q) {
            uri = Uri.fromFile(file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, fileProvider, file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(file);
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /** Gzip解压 */
    public static byte[] gzipUnCompress(byte[] bt) {
        ByteArrayOutputStream byteAos = null;
        ByteArrayInputStream byteArrayIn = null;
        GZIPInputStream gzipIn = null;
        try {
            byteArrayIn = new ByteArrayInputStream(bt);
            gzipIn = new GZIPInputStream(byteArrayIn);
            byteAos = new ByteArrayOutputStream();
            byte[] b = new byte[4096];
            int temp;
            while ((temp = gzipIn.read(b)) > 0) {
                byteAos.write(b, 0, temp);
            }
        } catch (Exception e) {
            return null;
        } finally {
            closeStream(byteAos, gzipIn, byteArrayIn);
        }
        return byteAos.toByteArray();
    }

    /** close */
    public static void closeStream(Object... streams) {
        for (Object stream : streams) {
            try {
                if (stream instanceof Closeable) ((Closeable) stream).close();
            } catch (IOException ignored) {
            }
        }
    }

    /** 是否处于后台 */
    @Deprecated
    public static boolean isApplicationBroughtToBackground(final Context context) {
        ComponentName topComponentName = getTopComponentName(context);
        return topComponentName != null && !topComponentName.getPackageName().equals(context.getPackageName());
    }

    public static String getTopActivity(Context context) {
        ComponentName topComponentName = getTopComponentName(context);
        if (topComponentName != null) {
            return topComponentName.toString();
        }
        return "";
    }

    private static ComponentName getTopComponentName(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) return null;
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (tasks != null && !tasks.isEmpty()) {
            return tasks.get(0).topActivity;
        }
        return null;
    }

    /** 打开设置 */
    public static void openSetting(Context context, boolean newTask) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        if (newTask) intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void ctrlSoftKeyboard(Activity act, boolean show) {
        InputMethodManager imm = (InputMethodManager) act.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) return;
        if (show && imm.isActive()) {
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
        } else {
            imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    public static String getFileSize(long size) {
        if (size < 1024L) {
            return size + "B";
        } else if (size < 1024L * 1024) {
            return String.format(Locale.CHINA, "%.2fKB", size / 1024f);
        } else if (size < 1024L * 1024 * 1024) {
            return String.format(Locale.CHINA, "%.2fMB", size / 1024 / 1024f);
        } else {
            return String.format(Locale.CHINA, "%.2fGB", size / 1024 / 1024 / 1024f);
        }
    }
}
