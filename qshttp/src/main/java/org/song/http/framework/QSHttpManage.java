package org.song.http.framework;

import android.app.Application;
import android.content.Context;

import org.song.http.framework.java.JavaHttpClient;
import org.song.http.framework.ok.OkHttpClient;

/**
 * Created by song on 2016/9/18.
 * http框架全局配置
 */
public class QSHttpManage {


    public static Application application;

    private static QSHttpConfig qsHttpConfig;

    private static QSHttpClient qsHttpClient;

    public static void init(QSHttpConfig qsHttpConfig) {
        QSHttpManage.qsHttpConfig = qsHttpConfig;
        application = qsHttpConfig.application();

        switch (qsHttpConfig.xxHttp()) {
            case OK_HTTP:
                qsHttpClient = new OkHttpClient();
                break;
            case JAVA_HTTP:
            default:
                qsHttpClient = new JavaHttpClient();
        }
        setInterceptor(interceptor);
    }

    private static Interceptor interceptor;

    public static void setInterceptor(Interceptor interceptor) {
        QSHttpManage.interceptor = interceptor;
        getQSHttpClient().setInterceptor(interceptor);
    }

    public static QSHttpConfig getQsHttpConfig() {
        if (qsHttpConfig == null)
            qsHttpConfig = QSHttpConfig.Build(null).build();
        return qsHttpConfig;
    }

    public static QSHttpClient getQSHttpClient() {
        if (qsHttpClient == null)
            qsHttpClient = new OkHttpClient();
        return qsHttpClient;
    }

    public static void cleanCache() {
        Utils.cleanCache();
    }


}
