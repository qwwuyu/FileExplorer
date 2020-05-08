package com.qwwuyu.file.utils.permit;

import android.os.Bundle;
import android.util.SparseArray;

import com.qwwuyu.file.utils.LogUtils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

/**
 * be like https://github.com/tbruyelle/RxPermissions
 */
public class PermitFragment extends Fragment {
    private static final String TAG = "Permissions";
    private static int requestCode = 12345;
    private SparseArray<OnCallback> callbacks = new SparseArray<>();

    private synchronized static int getRequestCode() {
        return requestCode++;
    }

    /**
     * @return 获取权限工具PermitFragment
     */
    static PermitFragment getPermissionsFragment(FragmentManager fragmentManager) {
        PermitFragment permitFragment = (PermitFragment) fragmentManager.findFragmentByTag(TAG);
        if (permitFragment == null) {
            permitFragment = new PermitFragment();
            try {
                fragmentManager.beginTransaction().add(permitFragment, TAG).commitNow();
            } catch (IllegalStateException e) {
                return null;
            }
        }
        return permitFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    void request(@NonNull String[] permissions, OnCallback callback) {
        int requestCode = getRequestCode();
        callbacks.put(requestCode, callback);
        requestPermissions(permissions, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        OnCallback onCallback = callbacks.get(requestCode);
        if (onCallback != null) {
            LogUtils.i("onCallback!=null size=" + callbacks.size());
            callbacks.delete(requestCode);
            onCallback.onResult(permissions, grantResults);
        } else {
            LogUtils.i("onCallback==null size=" + callbacks.size());
        }
    }

    public interface OnCallback {
        /**
         * 请求权限结果
         */
        void onResult(String[] permissions, int[] grantResults);
    }
}