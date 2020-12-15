package com.song.demo;

import java.io.Serializable;

/**
 * Created by song on 2017/2/16.
 */

public class BeanModel<M> implements Serializable {

    private int status;

    private String msg;

    private M data;

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

    public M getData() {
        return data;
    }

    public void setData(M data) {
        this.data = data;
    }
}
