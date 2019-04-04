package org.song.http.framework.ok;


import org.song.http.framework.QSHttpClient;
import org.song.http.framework.QSHttpConfig;

/*
 * Created by song on 2016/9/21.
 */
public class OkHttpClient extends QSHttpClient {
    //开放实例
    public OkHttpClient(QSHttpConfig qsHttpConfig) {
        super(new OkHttpTask(qsHttpConfig), qsHttpConfig);
    }

}
