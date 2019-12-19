package com.song.demo;

import android.app.Application;

import org.song.http.QSHttp;
import org.song.http.framework.QSHttpConfig;

/**
 * appcontext
 *
 * @author Administrator
 */
public class AppContext extends Application {

    private static AppContext instance;

    public static AppContext getInstance() {
        if (instance == null)
            throw new RuntimeException("AppContext is null");
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        init();
    }

    private void init() {

        QSHttp.init(QSHttpConfig.Build(this)
                .interceptor(new QSInterceptor())
                .interceptor(new QSInterceptor2())
                .build());

    }

}
