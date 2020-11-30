package com.qwwuyu.file.entity;

public enum NetType {
    /** wifi网络 */
    WIFI("Wifi"),
    /** 热点 */
    HOTSPOT("热点"),
    /** USB设备 */
    RNDIS("USB"),
    /** 移动网络 */
    RMNET("移动网络"),
    /** 有线网络 */
    ETH("网卡"),
    /** OTHER */
    OTHER("其他");
    String name;

    NetType(String name) {
        this.name = name;
    }
}
