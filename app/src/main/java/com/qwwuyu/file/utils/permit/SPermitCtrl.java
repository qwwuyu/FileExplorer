package com.qwwuyu.file.utils.permit;

import java.util.List;

/**
 * 简单实现
 */
public abstract class SPermitCtrl implements PermitCtrl {
    @Override
    public boolean beforeRequest(List<String> showRationales, List<String> request, ProceedCallback proceedListener) {
        return false;
    }

    @Override
    public void onGranted() {
    }

    @Override
    public void onDenied(List<String> granted, List<String> onlyDenied, List<String> foreverDenied, List<String> denied) {
    }
}
