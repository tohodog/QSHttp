package org.song.http;

/*
 * Created by song on 2016/9/26.
 * 辅助构建类 简化调用代码
 */

import org.song.http.framework.HttpEnum;
import org.song.http.framework.RequestParams;

public class QSHttp {

    public static RequestParams.Builder get(String url) {
        return build(url, HttpEnum.RequestType.GET);
    }

    //键值对post
    public static RequestParams.Builder post(String url) {
        return build(url, HttpEnum.RequestType.POST);
    }

    //自定义post内容 一般拿来post一个json
    public static RequestParams.Builder postCustom(String url) {
        return build(url, HttpEnum.RequestType.POST_CUSTOM);
    }

    public static RequestParams.Builder put(String url) {
        return build(url, HttpEnum.RequestType.PUT);
    }

    public static RequestParams.Builder putCustom(String url) {
        return build(url, HttpEnum.RequestType.PUT);
    }

    public static RequestParams.Builder putUpload(String url) {
        return build(url, HttpEnum.RequestType.PUT_MULTIPART);
    }

    public static RequestParams.Builder head(String url) {
        return build(url, HttpEnum.RequestType.HEAD);
    }

    public static RequestParams.Builder delete(String url) {
        return build(url, HttpEnum.RequestType.DELETE);
    }

    //multipart/form-data 多文件多参数上传
    public static RequestParams.Builder upload(String url) {
        return build(url, HttpEnum.RequestType.POST_MULTIPART);
    }

    //基于get的下载(任意请求都可以写入文件
    public static RequestParams.Builder download(String url, String localPath) {
        return build(url, HttpEnum.RequestType.GET).resultByFile(localPath);
    }

    //这里可以添加公共参数鉴权
    private static RequestParams.Builder build(String url, HttpEnum.RequestType requestType) {
        return RequestParams.Build(url)
                .requestType(requestType);
        //.header("sessionKey", "sessionKey");
    }
}
