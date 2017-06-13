package org.song.http.framework;

import android.widget.Toast;

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
    public final static int TYPE_RUN = 0x07;//本框架内部运行异常【没捕捉到的错误

    public final static int TYPE_CUSTOM = 0x08;//自定义异常内容

    private int requestID;//标记此次请求的id
    private final int type;//异常类型
    private int httpStatusCode;//http错误状态码
    private String customErr;//自定义异常

    private HttpException(int type, Exception e) {
        super(e);
        this.type = type;
    }

    public static HttpException NetWork(Exception e) {
        return new HttpException(TYPE_NETWORK, e);
    }

    public static HttpException HttpCode(int code) {
        return new HttpException(TYPE_HTTP_STATUS_CODE, new Exception("http status code error:" + code)).setHttpStatusCode(code);
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
        return new HttpException(TYPE_CUSTOM, new Exception()).setCustomErr(e);
    }

    private HttpException setCustomErr(String customErr) {
        this.customErr = customErr;
        return this;
    }


    private HttpException setHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
        return this;
    }

    public String getPrompt() {
        switch (type) {
            case TYPE_NETWORK:
                if (Utils.checkNet())
                    return "无法连接服务器!";
                else
                    return "网络连接失败,请检查网络设置!";
            case TYPE_HTTP_STATUS_CODE:
                return "网络异常,错误码:" + httpStatusCode;
            case TYPE_HTTP_TIMEOUT:
                return "请求超时,请重试!";
            case TYPE_PARSER:
                return "数据解析异常";
            case TYPE_CUSTOM:
                return customErr;
            case TYPE_IO:
                return "IO异常";
            case TYPE_RUN:
                return "未知异常/传了空、错误参数?";
        }
        return "看到这个提示表示有毒";
    }

    public int requestID() {
        return requestID;
    }

    public HttpException requestID(int requestID) {
        this.requestID = requestID;
        return this;
    }

    public int getType() {
        return type;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public void show() {
        if (HttpManage.application != null)
            Toast.makeText(HttpManage.application, getPrompt(), Toast.LENGTH_LONG);
    }
}
