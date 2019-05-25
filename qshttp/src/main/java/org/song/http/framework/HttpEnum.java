package org.song.http.framework;

/**
 * Created by song on 2016/12/1.
 */

public class HttpEnum {

    //请求回调类型
    final static public int HTTP_SUCCESS = 0X200;// 访问成功
    final static public int HTTP_FAILURE = 0X201;// 访问出错
    final static public int HTTP_PROGRESS = 0X202;//进度

    public static final String CHARSET_UTF8 = "utf-8";
    public static final String CONTENT_TYPE_FORM = "multipart/form-data";
    public static final String CONTENT_TYPE_JSON_ = "application/json; charset=";
    public static final String CONTENT_TYPE_URL_ = "application/x-www-form-urlencoded; charset=";
    public static final String CONTENT_TYPE_TEXT_ = "text/plain; charset=";

    public static final String HEAD_KEY_CT = "Content-Type";
    public static final String HEAD_KEY_CR = "Content-Range";
    public static final String HEAD_KEY_UA = "User-Agent";

    //请求类型
    public enum RequestMethod {
        GET,
        POST,
        PUT,
        PATCH,
        HEAD,
        DELETE,
        OPTIONS
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
