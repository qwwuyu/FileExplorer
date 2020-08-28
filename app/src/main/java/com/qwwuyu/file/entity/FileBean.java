package com.qwwuyu.file.entity;

public class FileBean {
    public String name;
    public boolean dir;
    public String date;
    public String info;
    public boolean apk;

    public FileBean(String name, boolean dir, String date, String info) {
        this.name = name;
        this.dir = dir;
        this.date = date;
        this.info = info;
    }
}
