package org.song.http.framework.ok;


import org.song.http.framework.AbsHttp;

/*
 * Created by song on 2016/9/21.
 */
public class OkHttp extends AbsHttp {

    private static OkHttp instance;

    public static OkHttp getInstance() {
        if (instance == null)
            instance = new OkHttp();
        return instance;
    }

    private OkHttp() {
        super(OkHttpTask.getInstance());
    }

}
