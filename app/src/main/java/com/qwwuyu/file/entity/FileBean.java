package com.qwwuyu.file.entity;

public class FileBean {
    public String name;
    public boolean dir;
    public String time;

    public FileBean(String name, boolean dir, String time) {
        this.name = name;
        this.dir = dir;
        this.time = time;
    }
}
