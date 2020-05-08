package com.qwwuyu.file.utils.permit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import com.qwwuyu.file.utils.LogUtils;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

/**
 * 权限请求工具
 */
public class PermitUtil {
    /**
     * @param fragment    fragment
     * @param permissions 请求的权限
     * @param ctrl        请求操作
     */
    public static void request(@NonNull Fragment fragment, String[] permissions, PermitCtrl ctrl) {
        FragmentActivity activity = fragment.getActivity();
        if (activity != null) {
            request(activity, permissions, ctrl);
        }
    }

    /**
     * @param activity    activity
     * @param permissions 请求的权限
     * @param ctrl        请求操作
     */
    public static void request(@NonNull final FragmentActivity activity, String[] permissions,
                               final PermitCtrl ctrl) {
        if (!isNeedRequestPermission() || permissions == null) {
            ctrl.onGranted();
            return;
        }
        final String[] requestPermissions = permissions;
        final ArrayList<String> request = new ArrayList<>();
        final ArrayList<String> showRationales = new ArrayList<>();
        for (String permission : requestPermissions) {
            if (checkSelfPermission(activity, permission)) {
                request.add(permission);
                if (shouldShowRequestPermissionRationale(activity, permission)) {
                    showRationales.add(permission);
                }
            }
        }
        if (request.isEmpty()) {
            ctrl.onGranted();
            return;
        }
        final PermitFragment fragment = PermitFragment.getPermissionsFragment(activity.getSupportFragmentManager());
        if (fragment == null) return;
        final PermitFragment.OnCallback onCallback = new PermitFragment.OnCallback() {
            @Override
            public void onResult(String[] permissions, int[] grantResults) {
                if (permissions == null || grantResults == null) {
                    //自由客 Z2121 grantResults = null
                    LogUtils.logError(new Exception("permissions=" + (permissions == null) + ",grantResults=" + (grantResults == null)));
                } else if (permissions.length != request.size()) {
                    //VIVO V1809A [],request=[android.permission.SEND_SMS]
                    LogUtils.logError(new Exception(Arrays.toString(permissions) + ",request=" + request));
                }
                permissions = requestPermissions;
                grantResults = new int[permissions.length];
                for (int i = 0; i < grantResults.length; i++) {
                    grantResults[i] = ContextCompat.checkSelfPermission(activity, permissions[i]);
                }
                final ArrayList<String> granted = new ArrayList<>();
                final ArrayList<String> onlyDenied = new ArrayList<>();
                final ArrayList<String> foreverDenied = new ArrayList<>();
                final ArrayList<String> denied = new ArrayList<>();
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        if (shouldShowRequestPermissionRationale(activity, permissions[i])) {
                            onlyDenied.add(permissions[i]);
                        } else {
                            foreverDenied.add(permissions[i]);
                        }
                        denied.add(permissions[i]);
                    } else {
                        granted.add(permissions[i]);
                    }
                }
                if (denied.isEmpty()) {
                    ctrl.onGranted();
                } else {
                    ctrl.onDenied(granted, onlyDenied, foreverDenied, denied);
                }
            }
        };
        final ProceedCallback proceedCallback = new ProceedCallback() {
            @Override
            public void proceed() {
                fragment.request(request.toArray(new String[0]), onCallback);
            }
        };
        if (!ctrl.beforeRequest(showRationales, request, proceedCallback)) {
            proceedCallback.proceed();
        }
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

    /**
     * check current sdk if >= 23
     * @return true is need requestPermission
     */
    public static boolean isNeedRequestPermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    private static boolean isRevoked(Context context, @NonNull String permName) {
        return isNeedRequestPermission() && context.getPackageManager().isPermissionRevokedByPolicy(permName, context.getPackageName());
    }

    /**
     * @return true need request Permission
     */
    public static boolean checkSelfPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED;
    }

    /**
     * @return true has request Permissions
     */
    public static boolean hasSelfPermissions(Context context, String... permissions) {
        if (!isNeedRequestPermission()) return true;
        for (String permission : permissions) {
            if (checkSelfPermission(context, permission)) return false;
        }
        return true;
    }

    /**
     * requestPermission
     */
    public static void requestPermission(Activity activity, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    /**
     * shouldShowRequestPermissionRationale
     */
    public static boolean shouldShowRequestPermissionRationale(Activity activity, String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }
}
