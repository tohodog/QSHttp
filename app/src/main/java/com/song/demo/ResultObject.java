package com.song.demo;


import java.io.Serializable;

public class ResultObject<T> implements Serializable {

    private static final long serialVersionUID = 4845950831108359928L;

    private int status = -1;
    private String msg;
    public T data;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isSuccess() {
        return status == 0;
    }

}
