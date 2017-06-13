package com.song.demo;

import java.io.Serializable;

/**
 * Created by song on 2017/2/16.
 */

public class Bean implements Serializable {
    private String userid;
    private String password;

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
