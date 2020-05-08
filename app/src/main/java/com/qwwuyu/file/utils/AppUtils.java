package com.qwwuyu.file.utils;

import com.qwwuyu.file.entity.FileBean;
import com.qwwuyu.file.entity.NetBean;
import com.qwwuyu.file.entity.NetType;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppUtils {
    public static ArrayList<NetBean> getCtrlIp() {
        ArrayList<NetBean> beans = new ArrayList<>();
        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                String name = networkInterface.getName();
                if (!name.contains("wlan") && !name.contains("rndis")) continue;
                NetBean bean = new NetBean();
                for (InetAddress inetAddress : Collections.list(networkInterface.getInetAddresses())) {
                    if (!inetAddress.isLoopbackAddress()) {
                        String hostAddress = inetAddress.getHostAddress();
                        boolean isIPv4 = hostAddress.indexOf(':') < 0;
                        if (isIPv4 && null == bean.getIpv4()) {
                            bean.setIpv4(hostAddress);
                        } else if (null == bean.getIpv6() && !isIPv4) {
                            int suffix = hostAddress.indexOf('%');// drop ip6 zone suffix
                            bean.setIpv6(suffix < 0 ? hostAddress.toUpperCase() : hostAddress.substring(0, suffix).toUpperCase());
                        }
                    }
                }
                if (name.contains("rndis")) {
                    bean.setType(NetType.RNDIS);
                } else if (name.contains("wlan") && bean.getIpv4() != null && bean.getIpv4().equals("192.168.43.1")) {
                    bean.setType(NetType.HOTSPOT);
                } else {
                    bean.setType(NetType.WIFI);
                }
                if (null != bean.getIpv4() && null != bean.getIpv6()) {
                    beans.add(bean);
                }
            }
        } catch (Exception e) {
            LogUtils.logError(e);
        }
        return beans;
    }

    public static void sortFileBean(List<? extends FileBean> list) {
        Collections.sort(list, new Comparator<FileBean>() {
            private int i = -1;
            private int j = 1;

            @Override
            public int compare(FileBean lhs, FileBean rhs) {
                if (lhs.isDirectory() && !rhs.isDirectory()) return i;
                if (!lhs.isDirectory() && rhs.isDirectory()) return j;
                return lhs.getName().compareTo(rhs.getName());
            }
        });
    }
}
