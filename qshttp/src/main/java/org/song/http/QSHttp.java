package org.song.http;


import android.app.Application;

import org.song.http.framework.HttpEnum;
import org.song.http.framework.QSHttpConfig;
import org.song.http.framework.QSHttpManage;
import org.song.http.framework.RequestParams;

/*
 * Created by song on 2016/9/26.
 * 辅助构建类,简化调用代码
 */
public class QSHttp {


    /**
     * 使用前进行初始化
     * 才能支持缓存 cookie 网络状态判断
     */
    public static void init(Application application) {
        QSHttpManage.init(QSHttpConfig.Build(application).build());
    }

    /**
     * 使用全局配置进行初始化
     */
    public static void init(QSHttpConfig qsHttpConfig) {
        QSHttpManage.init(qsHttpConfig);
    }

    /**
     * 初始化多个客户端,使用不同配置
     * 使用.qsClient("key")进行选择
     */
    public static void addClient(String key, QSHttpConfig qsHttpConfig) {
        QSHttpManage.addClient(key, qsHttpConfig);
    }

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

    public static RequestParams.Builder patch(String url) {
        return build(url, HttpEnum.RequestMethod.PATCH);
    }

    public static RequestParams.Builder patchJSON(String url) {
        return patch(url).toJsonBody();
    }

    public static RequestParams.Builder patchMulti(String url) {
        return patch(url).toMultiBody();
    }

    public static RequestParams.Builder head(String url) {
        return build(url, HttpEnum.RequestMethod.HEAD);
    }

    public static RequestParams.Builder delete(String url) {
        return build(url, HttpEnum.RequestMethod.DELETE);
    }

    public static RequestParams.Builder options(String url) {
        return build(url, HttpEnum.RequestMethod.OPTIONS);
    }


    public static RequestParams.Builder download(String url, String path) {
        return get(url).resultByFile(path);
    }

    public static RequestParams.Builder upload(String url) {
        return postMulti(url);
    }


    private static RequestParams.Builder build(String url, HttpEnum.RequestMethod requestMethod) {
        return RequestParams.Build(url)
                .requestMethod(requestMethod);
    }
}
