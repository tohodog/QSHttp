package org.song.http.framework;

import org.song.http.framework.util.Utils;

import java.util.Locale;

/**
 * Created by song on 2016/9/18.
 */
public class HttpException extends Exception {

    /**
     * 定义异常类型
     */
    public final static int TYPE_NETWORK = 0x01;//断网/服务器挂了
    public final static int TYPE_HTTP_STATUS_CODE = 0x02;//http状态码
    public final static int TYPE_HTTP_TIMEOUT = 0x03;//超时
    public final static int TYPE_PARSER = 0x05;//解析异常
    public final static int TYPE_IO = 0x06;//第三方联网的其他异常 【可以继续细分
    public final static int TYPE_RUN = 0x07;//本框架内部运行异常【没明确捕捉到的错误
    public final static int TYPE_CUSTOM = 0x08;//自定义异常内容

    private final int type;//异常类型

    private ResponseParams responseParams;//此次请求的参数
    private int httpStatusCode;//http错误状态码
    private Object exObject;//额外参数

    private HttpException(int type, Exception e) {
        super(e);
        this.type = type;
    }

    private HttpException(int type, String e) {
        super(e);
        this.type = type;
    }

    public static HttpException NetWork(Exception e) {
        return new HttpException(TYPE_NETWORK, e);
    }

    public static HttpException HttpCode(int code, String result) {
        return new HttpException(TYPE_HTTP_STATUS_CODE, "http status code error:" + code + " -> " + result)
                .setHttpStatusCode(code).setExObject(result);
    }

    public static HttpException HttpTimeOut(Exception e) {
        return new HttpException(TYPE_HTTP_TIMEOUT, e);
    }

    public static HttpException Parser(Exception e) {
        return new HttpException(TYPE_PARSER, e);
    }

    public static HttpException IO(Exception e) {
        return new HttpException(TYPE_IO, e);
    }

    public static HttpException Run(Exception e) {
        return new HttpException(TYPE_RUN, e);
    }

    public static HttpException Custom(String e) {
        return new HttpException(TYPE_CUSTOM, e);
    }

    public static HttpException Custom(String e, Object o) {
        return Custom(e).setExObject(o);
    }


    private HttpException setExObject(Object exObject) {
        this.exObject = exObject;
        return this;
    }

    private HttpException setHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
        return this;
    }


    public String getPrompt() {

        if ("zh".equals(Locale.getDefault().getLanguage())) {
            switch (type) {
                case TYPE_NETWORK:
                    if (Utils.checkNet())
                        return "无法连接服务器!";
                    else
                        return "网络连接失败,请检查网络设置!";
                case TYPE_HTTP_STATUS_CODE:
                    return "请求异常,错误码:" + httpStatusCode;
                case TYPE_HTTP_TIMEOUT:
                    return "请求超时,请重试!";
                case TYPE_PARSER:
                    return "数据解析异常";
                case TYPE_CUSTOM:
                    return getMessage();
                case TYPE_IO:
                    return "IO异常";
                case TYPE_RUN:
                    return "请求结果运行异常";
            }
        } else {
            switch (type) {
                case TYPE_NETWORK:
                    if (Utils.checkNet())
                        return "Can not connect to the server!";
                    else
                        return "Connection failed, please check network settings!";
                case TYPE_HTTP_STATUS_CODE:
                    return "Connection exception, error code:" + httpStatusCode;
                case TYPE_HTTP_TIMEOUT:
                    return "Request timed out, please try again!";
                case TYPE_PARSER:
                    return "Data parsing exception";
                case TYPE_CUSTOM:
                    return getMessage();
                case TYPE_IO:
                    return "IO exception";
                case TYPE_RUN:
                    return "Run error";
            }
        }
        return "Power by github.com/tohodog";
    }

    public ResponseParams responseParams() {
        return responseParams;
    }

    public HttpException responseParams(ResponseParams responseParams) {
        this.responseParams = responseParams;
        return this;
    }

    public Object getExObject() {
        return exObject;
    }

    public int getType() {
        return type;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public void show() {
        Utils.showToast(getPrompt());
    }
}
