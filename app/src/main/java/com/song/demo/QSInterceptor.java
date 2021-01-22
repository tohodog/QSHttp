package com.song.demo;

import org.song.http.framework.HttpException;
import org.song.http.framework.RequestParams;
import org.song.http.framework.ResponseParams;
import org.song.http.framework.ability.Interceptor;

/**
 * Created by song
 * Contact github.com/tohodog
 * Date 2019/5/25
 */
public class QSInterceptor implements Interceptor {
    @Override
    public ResponseParams intercept(Chain chain) throws HttpException {

        RequestParams newRequestParams = chain.request()
                .newBuild()
                .param("hhhh", "哼哼哈嘿")
                .header("QSInterceptor", "QSInterceptor")
                //继续添加修改其他
                .build();

        ResponseParams responseParams = chain.proceed(newRequestParams);
        //请求结果参数如有需要也可以进行修改
        return responseParams;
    }
}
