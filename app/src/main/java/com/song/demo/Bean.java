package com.song.demo;

import java.io.Serializable;
import java.util.List;

/**
 * Created by song on 2017/2/16.
 */

public class Bean<M> implements Serializable {

    private String userName;

    private List<M> rows;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    public List<M> getRows() {
        return rows;
    }

    public void setRows(List<M> rows) {
        this.rows = rows;
    }
}
