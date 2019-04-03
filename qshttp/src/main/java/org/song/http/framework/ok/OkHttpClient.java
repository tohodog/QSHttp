package org.song.http.framework.ok;


import org.song.http.framework.QSHttpClient;

/*
 * Created by song on 2016/9/21.
 */
public class OkHttpClient extends QSHttpClient {

    private static OkHttpClient instance;

    public static OkHttpClient getInstance() {
        if (instance == null)
            instance = new OkHttpClient();
        return instance;
    }

    //开放实例
    public OkHttpClient() {
        super(new OkHttpTask());
    }

}
