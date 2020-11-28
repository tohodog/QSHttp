package com.song.demo;

import org.song.http.framework.HttpException;
import org.song.http.framework.ability.Interceptor;
import org.song.http.framework.RequestParams;
import org.song.http.framework.ResponseParams;

/**
 * Created by song
 * Contact github.com/tohodog
 * Date 2019/12/18
 * 多拦截器
 */
public class QSInterceptor2 implements Interceptor {
    @Override
    public ResponseParams intercept(Chain chain) throws HttpException {

        String url = chain.request().url();
        if (url != null && !url.startsWith("http")) {
            url = API.HOST + url;
        }

        RequestParams newRequestParams = chain.request()
                .newBuild(url)
                .header("Interceptor2", "Interceptor2")
                //继续添加修改其他
                .build();


        ResponseParams responseParams = chain.proceed(newRequestParams);
        //请求结果参数如有需要也可以进行修改
        return responseParams;
    }
}
