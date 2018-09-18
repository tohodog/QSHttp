package org.song.http.framework;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by song on 2016/11/30.
 */

public class ResponseParams {

    private boolean success;//响应结果
    private boolean isCache;//是否缓存
    private Object tag;


    private HttpEnum.ResultType resultType = HttpEnum.ResultType.STRING;//返回内容类型
    private int requestID;//标记此次请求的id

    private String string;
    private String file;
    private byte[] bytes;
    private Object parserObject;

    private Map<String, List<String>> headers;//头参数
    private RequestParams requestParams;

    private Exception exception;//出错时不为空

    //get
    public int requestID() {
        return requestID;
    }

    public Object tag() {
        return tag;
    }

    public HttpEnum.ResultType resultType() {
        return resultType;
    }

    public String string() {
        return string == null ? "" : string;
    }

    public String file() {
        return file;
    }

    public byte[] bytes() {
        return bytes;
    }

    public <T extends Object> T parserObject() {
        return (T) parserObject;
    }

    public Map<String, List<String>> headers() {
        return headers;
    }

    public Bitmap bitmap() {
        Bitmap b = null;
        if (bytes != null)
            b = BitmapFactory.decodeByteArray(bytes(), 0, bytes().length);
        if (file != null)
            b = BitmapFactory.decodeFile(file());
        return b;
    }

    public RequestParams requestParams() {
        return requestParams;
    }

    public Exception exception() {
        return exception;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isCache() {
        return isCache;
    }


    ///////////////////////set///////////////
    public ResponseParams setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public ResponseParams setCacheYes() {
        isCache = true;
        return this;
    }


    public ResponseParams setException(Exception exception) {
        this.exception = exception;
        return this;
    }

    public ResponseParams setResultType(HttpEnum.ResultType resultType) {
        this.resultType = resultType;
        return this;
    }

    public ResponseParams setRequestID(int requestID) {
        this.requestID = requestID;
        return this;
    }

    public ResponseParams setString(String string) {
        this.string = string;
        return this;
    }

    public ResponseParams setFile(String file) {
        this.file = file;
        return this;
    }

    public ResponseParams setBytes(byte[] bytes) {
        this.bytes = bytes;
        return this;
    }

    public ResponseParams setParserObject(Object parserObject) {
        this.parserObject = parserObject;
        return this;
    }

    public ResponseParams setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
        return this;
    }

    public ResponseParams setRequestParams(RequestParams requestParams) {
        this.requestParams = requestParams;
        tag = requestParams.tag();
        return this;
    }
}
