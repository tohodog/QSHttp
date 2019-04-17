package com.song.demo;

import android.app.Application;

import org.song.http.framework.HttpException;
import org.song.http.framework.Interceptor;
import org.song.http.framework.RequestParams;
import org.song.http.framework.ResponseParams;

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
    }

    private void init() {

//        QSHttp.init(QSHttpConfig.Build(AppContext.getInstance()).interceptor(interceptor).build());

    }

    public static final Interceptor interceptor = new Interceptor() {
        @Override
        public ResponseParams intercept(Chain chain) throws HttpException {
            RequestParams requestParams = chain.request();

            return chain.proceed(chain.request()
                    .newBuild()
                    .header("User-Agent", "Android/OkHttpClient/Smacircle")
                    .header("Content-MD5", "MD5")
                    .build());

        }
    };
}
