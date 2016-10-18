package com.qwwuyu.server.bean;

public class NetBean {
    private NetType type;
    private String ipv4;
    private String ipv6;

    public enum NetType {
        /** wifi网络 */
        WIFI("Wifi"),
        /** 热点 */
        HOTSPOT("热点"),
        /** USB设备 */
        RNDIS("USB"),
        /** 移动网络 */
        RMNET("移动网络"),
        /** OTHER */
        OTHER("其他");
        String name;

        NetType(String name) {
            this.name = name;
        }
    }

    public String toString(int port) {
        String ip = ipv4 == null ? ipv6 : ipv4;
        return type.name + "→" + "http://" + ip + ":" + port;
    }

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
}
