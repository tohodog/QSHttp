package org.song.http;


import org.song.http.framework.HttpEnum;
import org.song.http.framework.RequestParams;

/*
 * Created by song on 2016/9/26.
 * 辅助构建类 简化调用代码
 */
public class QSHttp {

    public static RequestParams.Builder get(String url) {
        return build(url, HttpEnum.RequestMethod.GET);
    }

    public static RequestParams.Builder post(String url) {
        return build(url, HttpEnum.RequestMethod.POST);
    }

    public static RequestParams.Builder postJSON(String url) {
        return post(url).toJsonBody();
    }

    public static RequestParams.Builder postMulti(String url) {
        return post(url).toMultiBody();
    }

    public static RequestParams.Builder put(String url) {
        return build(url, HttpEnum.RequestMethod.PUT);
    }

    public static RequestParams.Builder putJSON(String url) {
        return put(url).toJsonBody();
    }

    public static RequestParams.Builder putMulti(String url) {
        return put(url).toMultiBody();
    }

    public static RequestParams.Builder head(String url) {
        return build(url, HttpEnum.RequestMethod.HEAD);
    }

    public static RequestParams.Builder delete(String url) {
        return build(url, HttpEnum.RequestMethod.DELETE);
    }


    //这里可以添加公共参数鉴权
    private static RequestParams.Builder build(String url, HttpEnum.RequestMethod requestMethod) {
        return RequestParams.Build(url)
                .requestMethod(requestMethod);
        //.header("sessionKey", "sessionKey");
    }
}
