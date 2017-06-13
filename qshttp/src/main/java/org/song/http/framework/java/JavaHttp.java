package org.song.http.framework.java;


import org.song.http.framework.AbsHttp;

/*
 * Created by song on 2016/9/21.
 */
public class JavaHttp extends AbsHttp {

    private static JavaHttp instance;

    public static JavaHttp getInstance() {
        if (instance == null)
            instance = new JavaHttp();
        return instance;
    }

    private JavaHttp() {
        super(HttpURLConnectionTask.getInstance());
    }

}
