package org.song.http.framework.java;


import org.song.http.framework.QSHttpClient;
import org.song.http.framework.QSHttpConfig;

/*
 * Created by song on 2016/9/21.
 */
public class JavaHttpClient extends QSHttpClient {

    //开放实例
    public JavaHttpClient(QSHttpConfig qsHttpConfig) {
        super(new HttpURLConnectionTask(qsHttpConfig), qsHttpConfig);
    }

}
