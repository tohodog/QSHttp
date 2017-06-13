package org.song.http.framework;

/**
 * Created by song on 2016/12/1.
 */

public class HttpEnum {

    //请求类型
    public enum RequestType {
        GET,
        POST,
        POST_CUSTOM,
        POST_MULTIPART,
        PUT,
        PUT_CUSTOM,
        PUT_MULTIPART,
        HEAD,
        DELETE
    }

    //返回数据类型
    public enum ResultType {
        STRING,
        BYTES,
        FILE
    }

    //解析模式
    public enum ParserMode {
        JSON,//json解析
        XML,//暂不支持
        COSTOM,//自定义解析
        NOTHING//不解析
    }

    //请求的时候缓存控制
    public enum CacheMode {
        NO_CACHE,//不使用缓存，全部走网络
        NO_STORE,//*默认配置 不使用缓存，也不存储缓存
        ONLY_CACHE,//只使用缓存 没有出错
        SERVER_CACHE,//由服务器返回头参数缓存配置决定

        CLIENT_CACHE,//使用缓存 由客户端决定缓存配置

        ERR_CACHE,//强制缓存 并只在联网失败时才使用
    }

    //
    public enum XX_Http {
        JAVA_HTTP,
        OK_HTTP
    }
}
