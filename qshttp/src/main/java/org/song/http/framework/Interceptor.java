package org.song.http.framework;

/**
 * Created by song on 2017/5/17.
 * 拦截器
 */

public interface Interceptor {
    ResponseParams intercept(Chain chain) throws HttpException;

    interface Chain {
        RequestParams request();

        ResponseParams proceed(RequestParams request) throws HttpException;
    }
}
