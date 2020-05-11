package com.qwwuyu.file.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import androidx.core.view.ViewCompat;

/**
 * Created by qiwei on 2017/6/30
 */
public class SystemBarUtil {
    private static ITint tint = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? new LollipopTint() :
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? new KitKatTint() : null);

    public static void setStatusBarColor(Activity activity, int color) {
        if (tint != null) tint.setStatusBarColor(activity, color);
    }

    public static void translucentStatusBar(Activity activity, boolean hideShelter) {
        if (tint != null) tint.translucentStatusBar(activity, hideShelter);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static class LollipopTint implements ITint {
        @Override
        public void setStatusBarColor(Activity activity, int color) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(color);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            keepChildView(window);
        }

        @Override
        public void translucentStatusBar(Activity activity, boolean hideShelter) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            if (hideShelter) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor(Color.TRANSPARENT);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            } else {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
            keepChildView(window);
        }

        private void keepChildView(Window window) {
            View childView = ((ViewGroup) window.findViewById(Window.ID_ANDROID_CONTENT)).getChildAt(0);
            if (childView != null) {
                childView.setFitsSystemWindows(false);
                ViewCompat.requestApplyInsets(childView);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static class KitKatTint implements ITint {
        private static final String TAG_FAKE_STATUS_BAR_VIEW = "StatusBarView";

        @Override
        public void setStatusBarColor(Activity activity, int color) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            addFakeStatusBarView(activity, color, DisplayUtils.getStatusBarHeight());
        }

        @Override
        public void translucentStatusBar(Activity activity, boolean hideShelter) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            rmFakeStatusBarView(activity);
        }

        private void addFakeStatusBarView(Activity activity, int statusBarColor, int statusBarHeight) {
            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            View contentChild = ((ViewGroup) decorView.findViewById(Window.ID_ANDROID_CONTENT)).getChildAt(0);
            if (contentChild == null) return;
            View fakeView = decorView.findViewWithTag(TAG_FAKE_STATUS_BAR_VIEW);
            if (fakeView != null) {
                fakeView.setBackgroundColor(statusBarColor);
            } else {
                View statusBarView = new View(activity);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, statusBarHeight);
                layoutParams.gravity = Gravity.TOP;
                statusBarView.setLayoutParams(layoutParams);
                statusBarView.setBackgroundColor(statusBarColor);
                statusBarView.setTag(TAG_FAKE_STATUS_BAR_VIEW);
                decorView.addView(statusBarView);
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) contentChild.getLayoutParams();
                lp.topMargin = statusBarHeight;
                contentChild.setLayoutParams(lp);
            }
            contentChild.setFitsSystemWindows(false);
            ((ViewGroup) contentChild).setClipToPadding(true);
        }

        private void rmFakeStatusBarView(Activity activity) {
            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            View contentChild = ((ViewGroup) decorView.findViewById(Window.ID_ANDROID_CONTENT)).getChildAt(0);
            if (contentChild == null) return;
            View fakeView = decorView.findViewWithTag(TAG_FAKE_STATUS_BAR_VIEW);
            if (fakeView != null) decorView.removeView(fakeView);
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) contentChild.getLayoutParams();
            lp.topMargin = 0;
            contentChild.setLayoutParams(lp);
            contentChild.setFitsSystemWindows(false);
            ((ViewGroup) contentChild).setClipToPadding(true);
        }
    }

    public interface ITint {
        void setStatusBarColor(Activity activity, int color);

        void translucentStatusBar(Activity activity, boolean hideShelter);
    }

    /** 设置字体图标样式 */
    public static boolean setStatusBarDarkMode(Activity activity, boolean darkMode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decorView = activity.getWindow().getDecorView();
                if (darkMode) decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                else decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                return true;
            } else if (setMIUIStatusBarDarkMode(activity, darkMode)) {
                return true;
            } else if (setFlymeStatusBarDarkIcon(activity, darkMode)) {
                return true;
            }
        }
        return false;
    }

    /** 小米修改MIUI */
    private static boolean setMIUIStatusBarDarkMode(Activity activity, boolean darkMode) {
        Class<? extends Window> clazz = activity.getWindow().getClass();
        try {
            int darkModeFlag;
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(activity.getWindow(), darkMode ? darkModeFlag : 0, darkModeFlag);
            return true;
        } catch (Exception ignored) {
        }
        return false;
    }

    /** 魅族修改Flyme */
    private static boolean setFlymeStatusBarDarkIcon(Activity activity, boolean darkMode) {
        try {
            WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
            Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
            Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
            darkFlag.setAccessible(true);
            meizuFlags.setAccessible(true);
            int bit = darkFlag.getInt(null);
            int value = meizuFlags.getInt(lp);
            if (darkMode) value |= bit;
            else value &= ~bit;
            meizuFlags.setInt(lp, value);
            activity.getWindow().setAttributes(lp);
            return true;
        } catch (Exception ignored) {
        }
        return false;
    }

    public static class SystemBarConfig {
        private final boolean mTranslucentStatusBar;
        private final boolean mTranslucentNavBar;
        private final boolean mInPortrait;
        private final float mSmallestWidthDp;
        private final int mStatusBarHeight;
        private final int mNavigationBarHeight;
        private final int mNavigationBarWidth;
        private final boolean mHasNavigationBar;

        private SystemBarConfig(Activity activity, boolean translucentStatusBar, boolean translucentNavBar) {
            mTranslucentStatusBar = translucentStatusBar;
            mTranslucentNavBar = translucentNavBar;
            mInPortrait = activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
            mSmallestWidthDp = Math.min(DisplayUtils.widthDp(), DisplayUtils.heightDp());
            mStatusBarHeight = DisplayUtils.getStatusBarHeight();
            mNavigationBarHeight = DisplayUtils.hasNavBar() ? mInPortrait ? DisplayUtils.getNavBarHeight() : DisplayUtils.getNavBarHeightLand() : 0;
            mNavigationBarWidth = DisplayUtils.hasNavBar() ? DisplayUtils.getNavBarWidth() : 0;
            mHasNavigationBar = mNavigationBarHeight > 0;
        }

        /** 是否导航栏在底部 */
        public boolean isNavigationAtBottom() {
            return (mSmallestWidthDp >= 600 || mInPortrait);
        }

        /** 状态栏高度 */
        public int getStatusBarHeight() {
            return mStatusBarHeight;
        }

        /** 是否有导航栏 */
        public boolean hasNavigtionBar() {
            return mHasNavigationBar;
        }

        /** 当前界面导航栏高度 */
        public int getNavigationBarHeight() {
            return mNavigationBarHeight;
        }

        /** 当前界面导航栏宽度 */
        public int getNavigationBarWidth() {
            return mNavigationBarWidth;
        }

        /** 上内边距 */
        public int getPixelInsetTop() {
            return mTranslucentStatusBar ? mStatusBarHeight : 0;
        }

        /** 下内边距 */
        public int getPixelInsetBottom() {
            return (mTranslucentNavBar && isNavigationAtBottom()) ? mNavigationBarHeight : 0;
        }

        /** 右内边距 */
        public int getPixelInsetRight() {
            return (mTranslucentNavBar && !isNavigationAtBottom()) ? mNavigationBarWidth : 0;
        }
    }
}