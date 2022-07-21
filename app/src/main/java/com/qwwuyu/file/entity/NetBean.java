package com.qwwuyu.file.entity;

public class NetBean {
    private NetType type;
    private String ipv4;
    private String ipv6;

    public NetType getType() {
        return type;
    }

    public void setType(NetType type) {
        this.type = type;
    }

    public String getIpv4() {
        return ipv4;
    }

    public void setIpv4(String ipv4) {
        this.ipv4 = ipv4;
    }

    public String getIpv6() {
        return ipv6;
    }

    public void setIpv6(String ipv6) {
        this.ipv6 = ipv6;
    }

    public String toString(int port) {
        String ip = ipv4 == null ? ipv6 : ipv4;
        return type.name + "→" + "http://" + ip + ":" + port;
    }

    public String toLocation(int port) {
        String ip = ipv4 == null ? ipv6 : ipv4;
        return "http://" + ip + ":" + port;
    }
}
