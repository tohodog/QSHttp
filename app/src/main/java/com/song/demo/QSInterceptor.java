package com.song.demo;

import org.song.http.framework.HttpException;
import org.song.http.framework.ability.Interceptor;
import org.song.http.framework.RequestParams;
import org.song.http.framework.ResponseParams;

/**
 * Created by song
 * Contact github.com/tohodog
 * Date 2019/5/25
 * TODO 拦截器不要放在非全局外部类的匿名内部类使用,否则外部类将会内存泄露
 */
public class QSInterceptor implements Interceptor {
    @Override
    public ResponseParams intercept(Chain chain) throws HttpException {

        String url = chain.request().url();
        if (url != null && !url.startsWith("http")) {
            url = API.HOST + url;
        }

        RequestParams newRequestParams = chain.request()
                .newBuild(url)
                .header("Interceptor", "Interceptor")
                //继续添加修改其他
                .build();


        ResponseParams responseParams = chain.proceed(newRequestParams);
        //请求结果参数如有需要也可以进行修改
        return responseParams;
    }
}
