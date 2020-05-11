package com.qwwuyu.file.utils;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.ViewConfiguration;

import java.lang.reflect.Method;

/**
 * Created by qiwei on 2017/6/30
 */
public class DisplayUtils {
    /** ApplicationContext */
    private static Context appContext;
    private static Resources res;
    private static DisplayMetrics dm;

    private DisplayUtils() {
        throw new UnsupportedOperationException("can't instantiate");
    }

    public static synchronized void init(Context context) {
        if (appContext == null) {
            appContext = context.getApplicationContext();
            res = appContext.getResources();
            dm = res.getDisplayMetrics();
        }
    }

    public static int widthPixels() {
        return dm.widthPixels;
    }

    public static int heightPixels() {
        return dm.heightPixels;
    }

    public static float widthDp() {
        return dm.widthPixels / dm.density;
    }

    public static float heightDp() {
        return dm.heightPixels / dm.density;
    }

    public static int dp2px(float dpValue) {
        return (int) (dpValue * dm.density + .5f);
    }

    public static float px2dp(int pxValue) {
        return pxValue / dm.density;
    }

    private static int getInternalDimensionSize(String key) {
        int resourceId = res.getIdentifier(key, "dimen", "android");
        if (resourceId > 0) return res.getDimensionPixelSize(resourceId);
        return 0;
    }

    private final static int UNINITIALIZED = -1;
    private static int[] sizes = {UNINITIALIZED, UNINITIALIZED, UNINITIALIZED, UNINITIALIZED};

    private static int getSize(int index, String key) {
        if (UNINITIALIZED != sizes[index]) return sizes[index];
        return sizes[index] = getInternalDimensionSize(key);
    }

    public static int getStatusBarHeight() {
        return getSize(0, "status_bar_height");
    }

    public static int getNavBarHeight() {
        return getSize(1, "navigation_bar_height");
    }

    public static int getNavBarHeightLand() {
        return getSize(2, "navigation_bar_height_landscape");
    }

    public static int getNavBarWidth() {
        return getSize(3, "navigation_bar_width");
    }

    private static Boolean hasNavBar;

    public static boolean hasNavBar() {
        if (hasNavBar != null) return hasNavBar;
        int resourceId = res.getIdentifier("config_showNavigationBar", "bool", "android");
        if (resourceId != 0) {
            boolean hasNav = res.getBoolean(resourceId);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                try {
                    Method m = Class.forName("android.os.SystemProperties").getDeclaredMethod("get", String.class);
                    m.setAccessible(true);
                    String sNavBarOverride = (String) m.invoke(null, "qemu.hw.mainkeys");
                    if ("1".equals(sNavBarOverride)) hasNav = false;
                    else if ("0".equals(sNavBarOverride)) hasNav = true;
                } catch (Throwable ignored) {
                }
            }
            return hasNavBar = hasNav;
        } else {
            return hasNavBar = !ViewConfiguration.get(appContext).hasPermanentMenuKey();
        }
    }
}