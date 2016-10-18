package com.qwwuyu.server.utils;

import com.qwwuyu.server.bean.NetBean;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;

public class IPUtil {
    public static ArrayList<NetBean> getCtrlIP() {
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
                    bean.setType(NetBean.NetType.RNDIS);
                } else if (name.contains("wlan") && bean.getIpv4() != null && bean.getIpv4().equals("192.168.43.1")) {
                    bean.setType(NetBean.NetType.HOTSPOT);
                } else {
                    bean.setType(NetBean.NetType.WIFI);
                }
                if (null != bean.getIpv4() && null != bean.getIpv6()) {
                    beans.add(bean);
                }
            }
        } catch (Exception ignored) {
        }
        return beans;
    }
}
