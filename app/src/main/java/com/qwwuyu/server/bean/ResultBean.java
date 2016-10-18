package com.qwwuyu.server.bean;

public class ResultBean {
    private int result;
    private String data;

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public void setResult(boolean result) {
        this.result = result ? 1 : 0;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
