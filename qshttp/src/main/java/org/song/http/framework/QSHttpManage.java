package org.song.http.framework;

import android.app.Application;
import android.content.Context;

import org.song.http.framework.java.JavaHttpClient;
import org.song.http.framework.ok.OkHttpClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by song on 2016/9/18.
 * http框架全局配置
 */
public class QSHttpManage {

    public static Application application;

    private static Map<String, QSHttpClient> mapQSHttpClient = new HashMap<>();

    public static void init(QSHttpConfig qsHttpConfig) {
        addClient(null, qsHttpConfig);
    }

    /**
     * 初始化多个客户端,使用不同配置
     * 使用.qsClient("key")进行选择
     */
    public static void addClient(String key, QSHttpConfig qsHttpConfig) {
        application = qsHttpConfig.application();

        QSHttpClient qsHttpClient;
        switch (qsHttpConfig.xxHttp()) {
            case JAVA_HTTP:
                qsHttpClient = new JavaHttpClient(qsHttpConfig);
                break;
            case OK_HTTP:
            default:
                qsHttpClient = new OkHttpClient(qsHttpConfig);

        }
        mapQSHttpClient.put(key, qsHttpClient);
    }

    //兼容下不初始化
    public static QSHttpClient getQSHttpClient() {
        if (application == null)
            init(QSHttpConfig.Build(null).build());
        return getQSHttpClient(null);
    }

    public static QSHttpClient getQSHttpClient(String key) {
        return mapQSHttpClient.get(key);
    }

    public static void cleanCache() {
        Utils.cleanCache();
    }

}
