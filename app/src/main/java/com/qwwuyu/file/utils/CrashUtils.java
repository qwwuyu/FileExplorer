package com.qwwuyu.file.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import androidx.annotation.NonNull;


/**
 * <pre>
 *     author: Blankj
 *     blog  : http://blankj.com
 *     time  : 2016/09/27
 *     desc  : utils about crash
 * </pre>
 */
public final class CrashUtils {
    private static String defaultDir;
    private static String dir;
    private static String versionName;
    private static int versionCode;

    public static final String FILE_SEP = File.separator;
    @SuppressLint("SimpleDateFormat")
    private static final Format FORMAT = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");

    private static final UncaughtExceptionHandler DEFAULT_UNCAUGHT_EXCEPTION_HANDLER;
    private static final UncaughtExceptionHandler UNCAUGHT_EXCEPTION_HANDLER;

    private static OnCrashListener sOnCrashListener;

    static {
        DEFAULT_UNCAUGHT_EXCEPTION_HANDLER = Thread.getDefaultUncaughtExceptionHandler();

        UNCAUGHT_EXCEPTION_HANDLER = new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull final Thread t, @NonNull final Throwable e) {
                final String time = FORMAT.format(new Date(System.currentTimeMillis()));
                final StringBuilder sb = new StringBuilder();
                final String head = "************* Log Head ****************" +
                        "\nTime Of Crash      : " + time +
                        "\nDevice Manufacturer: " + Build.MANUFACTURER +
                        "\nDevice Model       : " + Build.MODEL +
                        "\nAndroid Version    : " + Build.VERSION.RELEASE +
                        "\nAndroid SDK        : " + Build.VERSION.SDK_INT +
                        "\nApp VersionName    : " + versionName +
                        "\nApp VersionCode    : " + versionCode +
                        "\n************* Log Head ****************\n\n";
                sb.append(head).append(Log.getStackTraceString(e));
                if (Boolean.parseBoolean("false")) sb.append("");
                final String crashInfo = sb.toString();
                final String fullPath = (dir == null ? defaultDir : dir) + time + ".txt";
                if (createOrExistsFile(fullPath)) {
                    input2File(crashInfo, fullPath);
                } else {
                    Log.e("CrashUtils", "create " + fullPath + " failed!");
                }
                e.printStackTrace();

                if (sOnCrashListener != null) {
                    sOnCrashListener.onCrash(crashInfo, fullPath, e, DEFAULT_UNCAUGHT_EXCEPTION_HANDLER);
                } else if (DEFAULT_UNCAUGHT_EXCEPTION_HANDLER != null) {
                    DEFAULT_UNCAUGHT_EXCEPTION_HANDLER.uncaughtException(t, e);
                }
            }
        };
    }

    private CrashUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public static void init(Context context) {
        init(context, null, null);
    }

    public static void init(Context context, final String crashDirPath) {
        init(context, crashDirPath, null);
    }

    /**
     * Initialization
     * @param crashDirPath    The directory's path of saving crash information.
     * @param onCrashListener The crash listener.
     */
    public static void init(Context context, final String crashDirPath, final OnCrashListener onCrashListener) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            if (pi != null) {
                versionName = pi.versionName;
                versionCode = pi.versionCode;
            }
        } catch (Exception ignored) {
        }
        if (isSpace(crashDirPath)) {
            dir = null;
        } else {
            dir = crashDirPath.endsWith(FILE_SEP) ? crashDirPath : crashDirPath + FILE_SEP;
        }
        try {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && context.getExternalCacheDir() != null) {
                defaultDir = context.getExternalCacheDir() + FILE_SEP + "logs" + FILE_SEP + "crash" + FILE_SEP;
            }
        } catch (Exception ignored) {
        }
        if (defaultDir == null) {
            defaultDir = context.getCacheDir() + FILE_SEP + "logs" + FILE_SEP + "crash" + FILE_SEP;
        }
        sOnCrashListener = onCrashListener;
        Thread.setDefaultUncaughtExceptionHandler(UNCAUGHT_EXCEPTION_HANDLER);
    }

    ///////////////////////////////////////////////////////////////////////////
    // interface
    ///////////////////////////////////////////////////////////////////////////

    public interface OnCrashListener {
        void onCrash(@NonNull String crashInfo, @NonNull String fullPath,@NonNull Throwable e, UncaughtExceptionHandler uncaughtExceptionHandler);
    }

    ///////////////////////////////////////////////////////////////////////////
    // other utils methods
    ///////////////////////////////////////////////////////////////////////////

    private static void input2File(final String input, final String filePath) {
        Future<Boolean> submit = Executors.newSingleThreadExecutor().submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                BufferedWriter bw = null;
                try {
                    bw = new BufferedWriter(new FileWriter(filePath, true));
                    bw.write(input);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                } finally {
                    try {
                        if (bw != null) {
                            bw.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        try {
            if (submit.get()) return;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        Log.e("CrashUtils", "write crash info to " + filePath + " failed!");
    }

    private static boolean createOrExistsFile(final String filePath) {
        File file = new File(filePath);
        if (file.exists()) return file.isFile();
        if (!createOrExistsDir(file.getParentFile())) return false;
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean createOrExistsDir(final File file) {
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    private static boolean isSpace(final String s) {
        if (s == null) return true;
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
