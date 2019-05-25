package org.song.http.framework;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by song on 2016/9/18.
 * http请求配置及参数类
 */
public class RequestParams {

    private String charset;

    private String url;
    private List<String> pathParams;//path参数
    private Map<String, String> headers;//头参数

    private Map<String, Object> params;//键值对参数
    private RequestBody requestBody;//自定义post/put内容
    private Map<String, RequestBody> multipartBody;//上传内容参数
    private String multipartType;

    private Object tag;//标记
    private String qsClient;

    private HttpEnum.RequestMethod requestMethod;//请求模式 GET/P_FORM/...
    private HttpEnum.ResultType resultType;//返回数据类型
    private HttpEnum.ParserMode parserMode;//解析模式 返回类型为string有效
    private HttpEnum.CacheMode cacheMode;//服务器设置了缓存时的控制

    private String downloadPath;//下载文件路径
    private Class<?> _class;//自动解析需要的模型
    private Parser parser;//手动解析需要的实现类

    private int cacheTime;//手动缓存 设置有效时间 [非服务器给的缓存配置,有缓存时不会联网
    private int timeOut;// 超时ms

    private RequestParams() {
    }

    public String charset() {
        return charset;
    }

    public String url() {
        return url;
    }

    public HttpEnum.RequestMethod requestMethod() {
        return requestMethod;
    }

    public HttpEnum.ResultType resultType() {
        return resultType;
    }

    public HttpEnum.CacheMode cacheMode() {
        return cacheMode;
    }

    public RequestBody requestBody() {
        return requestBody;
    }

    public Map<String, Object> params() {
        return params;
    }

    public Map<String, RequestBody> multipartBody() {
        return multipartBody;
    }

    public String multipartType() {
        return multipartType;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public HttpEnum.ParserMode parserMode() {
        return parserMode;
    }

    public Class<?> get_class() {
        return _class;
    }

    public Parser parser() {
        return parser;
    }

    public Object tag() {
        return tag;
    }

    public int timeOut() {
        return timeOut;
    }

    public int cacheTime() {
        return cacheTime;
    }

    public String downloadPath() {
        return downloadPath;
    }


    /**
     * 提取参数数据 构建带参数url格式
     */
    public String urlEncode() {
        StringBuilder sbUrl = new StringBuilder();
        sbUrl.append(urlAndPath());

        if (params != null) {
            sbUrl.append('?');
            for (String name : params.keySet()) {
                String value = String.valueOf(params.get(name));
                sbUrl.append(Utils.URLEncoder(name, charset))
                        .append('=')
                        .append(Utils.URLEncoder(value, charset))
                        .append('&');
            }
            sbUrl.deleteCharAt(sbUrl.length() - 1);
        }

        return sbUrl.toString();
    }

    /**
     * 提取参数数据 构建带参数url格式
     */
    public String urlAndPath() {
        StringBuilder sbUrl = new StringBuilder(url);

        if (pathParams != null) {
            for (String value : pathParams) {
                value = Utils.URLEncoder(value, charset);
                sbUrl.append("/").append(value);
            }
        }

        return sbUrl.toString();
    }

    public Builder newBuild() {
        return newBuild(url);
    }

    public Builder newBuild(String url) {
        Builder builder = new Builder(url);
        builder.charset = charset;
        builder.url = url;
        builder.requestBody = requestBody;
        builder.pathParams = pathParams;
        builder.params = params;
        builder.multipartBody = multipartBody;
        builder.multipartType = multipartType;
        builder.headers = headers;
        builder.tag = tag;
        builder.qsClient = qsClient;

        builder.requestMethod = requestMethod;
        builder.resultType = resultType;
        builder.parserMode = parserMode;
        builder.cacheMode = cacheMode;

        builder._class = _class;
        builder.parser = parser;
        builder.downloadPath = downloadPath;
        builder.cacheTime = cacheTime;
        builder.timeOut = timeOut;

        return builder;
    }

    public static Builder Build(String url) {
        return new Builder(url);
    }

    public static final class Builder {

        private String charset = HttpEnum.CHARSET_UTF8;

        private String url;
        private RequestBody requestBody;
        private Map<String, String> headers;
        private Map<String, Object> params;
        private List<String> pathParams;
        private Map<String, RequestBody> multipartBody;
        private String multipartType;

        private Object tag;//标记

        private HttpEnum.RequestMethod requestMethod = HttpEnum.RequestMethod.GET;//请求模式 GET/P_FORM/...
        private HttpEnum.ResultType resultType = HttpEnum.ResultType.STRING;//返回类型
        private HttpEnum.ParserMode parserMode = HttpEnum.ParserMode.NOTHING;//解析模式
        private HttpEnum.CacheMode cacheMode = HttpEnum.CacheMode.NO_STORE;//缓存控制

        private Class<?> _class;//自动解析需要的模型
        private Parser parser;
        private String downloadPath;
        private String qsClient;

        private int cacheTime;//手动缓存 设置有效时间 [非服务器给的缓存配置 功能待定
        private int timeOut;// 超时ms

        private boolean toJsonBodyFlag;
        private boolean toMultiBodyFlag;

        public Builder(String url) {
            this.url = url;
        }

        public RequestParams build() {
            if (toJsonBodyFlag & requestBody == null) {
                jsonBody(params);
            } else if (toMultiBodyFlag) {
                multipartBody(params);
            }
            RequestParams requestParams = new RequestParams();
            requestParams.charset = charset;
            requestParams.url = url;
            requestParams.pathParams = pathParams;
            requestParams.requestBody = requestBody;
            requestParams.params = params;
            requestParams.multipartBody = multipartBody;
            requestParams.multipartType = multipartType;
            requestParams.headers = headers;
            requestParams.tag = tag;
            requestParams.qsClient = qsClient;

            requestParams.requestMethod = requestMethod;
            requestParams.resultType = resultType;
            requestParams.parserMode = parserMode;
            requestParams.cacheMode = cacheMode;

            requestParams._class = _class;
            requestParams.parser = parser;
            requestParams.cacheTime = cacheTime;
            requestParams.downloadPath = downloadPath;
            requestParams.timeOut = timeOut;

            return requestParams;
        }

        /**
         * 设置编码
         * 需第一个调用
         */
        public RequestParams.Builder charset(String charset) {
            if (Charset.isSupported(charset))
                this.charset = charset;
            else
                Log.e("RequestParams.Builder", charset + "is not support");
            return this;
        }

        /**
         * 请求方式
         * get/post/..
         */
        public RequestParams.Builder requestMethod(HttpEnum.RequestMethod requestMethod) {
            if (requestMethod != null)
                this.requestMethod = requestMethod;
            return this;
        }

        /**
         * 返回数据类型 string byte file
         */
        public RequestParams.Builder resultType(HttpEnum.ResultType resultType) {
            if (resultType != null)
                this.resultType = resultType;
            return this;
        }

        public RequestParams.Builder resultByBytes() {
            return resultType(HttpEnum.ResultType.BYTES);
        }

        public RequestParams.Builder resultByFile(String downloadPath) {
            this.downloadPath = downloadPath;
            return resultType(HttpEnum.ResultType.FILE);
        }

        /**
         * 解析类型 json/自定义/不解析
         * 前提resultType为string
         */
        public RequestParams.Builder parserMode(HttpEnum.ParserMode parserMode) {
            if (parserMode != null)
                this.parserMode = parserMode;
            return this;
        }

        /**
         * 请求的时候缓存规则控制
         */
        public RequestParams.Builder cacheMode(HttpEnum.CacheMode cacheMode) {
            if (cacheMode != null)
                this.cacheMode = cacheMode;
            return this;
        }

        //不使用缓存，全部走网络
        public RequestParams.Builder noCache() {
            return cacheMode(HttpEnum.CacheMode.NO_CACHE);
        }

        //默认配置! 不使用缓存，也不存储缓存
        public RequestParams.Builder noStore() {
            return cacheMode(HttpEnum.CacheMode.NO_STORE);
        }

        //只使用缓存 没发现有啥用
        public RequestParams.Builder onlyIfCached() {
            return cacheMode(HttpEnum.CacheMode.ONLY_CACHE);
        }

        //开启由服务器配置的请求头的缓存 服务器没配置缓存规则并不起作用
        public RequestParams.Builder openServerCache() {
            return cacheMode(HttpEnum.CacheMode.SERVER_CACHE);
        }

        //联网失败使用缓存
        public RequestParams.Builder errCache() {
            return cacheMode(HttpEnum.CacheMode.ERR_CACHE);
        }

        //开启由客户端决定的缓存配置
        public RequestParams.Builder clientCache(int timeout) {
            cacheTime(timeout);
            return cacheMode(HttpEnum.CacheMode.CLIENT_CACHE);
        }


        /**
         * http头参数
         * 非Content-Type的头参数
         */
        public RequestParams.Builder header(String key, String value) {
            if (headers == null)
                headers = new HashMap<>();
            if (value != null)
                headers.put(key, value);
            return this;
        }

        /**
         * get请求/xx/xxx参数
         */
        public RequestParams.Builder path(Object... value) {
            if (pathParams == null)
                pathParams = new ArrayList<>();
            if (value != null)
                for (Object o : value)
                    if (o != null)
                        pathParams.add(String.valueOf(o));
            return this;
        }

        /**
         * 请求键值对参数(string file byte[])
         */
        public RequestParams.Builder param(String key, Object value) {
            if (params == null)
                params = new HashMap<>();
            if (value != null)
                params.put(key, value);
            else
                params.remove(key);
            return this;
        }

        /**
         * 请求键值对参数
         */
        public RequestParams.Builder param(Object object) {
            if (object != null) {
                if (object.getClass().isArray() || object instanceof Collection)
                    throw new IllegalArgumentException("param can not array");
                if (object instanceof org.json.JSONObject) {
                    object = object.toString();
                }
                JSONObject jsonObject = (JSONObject) JSON.toJSON(object);
                for (String key : jsonObject.keySet())
                    param(key, jsonObject.get(key));
            }
            return this;
        }

        /**
         * 请求键值对参数
         */
        public RequestParams.Builder param(Map<String, ?> params) {
            if (params != null) {
                for (String key : params.keySet())
                    param(key, params.get(key));
            }
            return this;
        }


        /**
         * 把 params 转为一个json参数
         */
        public RequestParams.Builder toJsonBody() {
            toJsonBodyFlag = true;
            return this;
        }

        /**
         * post/put 一个json body
         */
        public RequestParams.Builder jsonBody(Object object) {
            if (object instanceof org.json.JSONObject) {
                object = object.toString();
            }
            requestBody(HttpEnum.CONTENT_TYPE_JSON_ + charset, JSON.toJSONString(object));
            return this;
        }


        /**
         * post/put 一个自定义内容(file byte[] string）
         */
        public RequestParams.Builder requestBody(String contentType, Object content) {
            requestBody = new RequestBody(contentType, content);
            return this;
        }


        /**
         * 把 params 转为multipartBody参数
         */
        public RequestParams.Builder toMultiBody() {
            return toMultiBody(HttpEnum.CONTENT_TYPE_FORM);
        }


        /**
         * 把 params 转为multipartBody参数
         */
        public RequestParams.Builder toMultiBody(String multipartType) {
            if (toMultiBodyFlag)
                return this;
            toMultiBodyFlag = true;
            this.multipartType = multipartType;
            if (multipartBody == null)
                multipartBody = new HashMap<>();
            return this;
        }

        /**
         * 上传
         */
        public RequestParams.Builder multipartBody(String key, String contentType, String filename, Object value) {
            toMultiBody();
            multipartBody.put(key, new RequestBody(contentType, filename, value));
            return this;
        }

        /**
         * 上传
         */
        private RequestParams.Builder multipartBody(Map<String, ?> params) {
            if (params != null) {
                for (String key : params.keySet()) {
                    Object value = params.get(key);
                    if (value instanceof File) {
                        multipartBody(key, HttpEnum.CONTENT_TYPE_FORM, ((File) value).getName(), value);
                    } else if (value instanceof byte[]) {
                        multipartBody(key, HttpEnum.CONTENT_TYPE_FORM, "bytes", value);
                    } else if (value != null) {
                        multipartBody(key, HttpEnum.CONTENT_TYPE_TEXT_ + charset, null, value.toString());
                    }
                }
            }
            return this;
        }


        /**
         * 自动解析模型
         * 返回json解析成对象
         */
        public RequestParams.Builder jsonModel(Class<?> _class) {
            this._class = _class;
            parserMode(HttpEnum.ParserMode.JSON);
            return this;
        }

        /**
         * 手动解析的实现类
         */
        public RequestParams.Builder parser(Parser parser) {
            this.parser = parser;
            parserMode(HttpEnum.ParserMode.COSTOM);
            return this;
        }

        /**
         * 选择其他自定义客户端
         * QSHttpManage.addClient()
         */
        public RequestParams.Builder qsClient(String qsClient) {
            this.qsClient = qsClient;
            return this;
        }

        /**
         * 标记此次请求
         */
        public RequestParams.Builder tag(Object tag) {
            this.tag = tag;
            return this;
        }

        /**
         * 超时时间
         */
        public RequestParams.Builder timeOut(int timeOut) {
            this.timeOut = timeOut;
            return this;
        }

        /**
         * 缓存时间 秒
         * 暂时没用
         */
        private RequestParams.Builder cacheTime(int cacheTime) {
            this.cacheTime = cacheTime;
            return this;
        }

        //构建并执行
        public int buildAndExecute(HttpCallback cb) {
            return build().execute(cb);
        }

        //构建并执行
        public int buildAndExecute() {
            return buildAndExecute(null);
        }
    }

    public static class RequestBody {

        private String contentType;
        private Object content;//byte file string ouputstream
        private String filename;

        public RequestBody(String contentType, Object content) {
            this(contentType, Long.toString(System.currentTimeMillis()), content);
        }

        public RequestBody(String contentType, String filename, Object content) {
            this.contentType = contentType;
            this.content = content;
            this.filename = filename;
        }

        public Object getContent() {
            return content;
        }

        public void setContent(Object content) {
            this.content = content;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public String getCharset() {
            return Utils.charsetName(contentType);
        }


        @Override
        public String toString() {
            return "{ " + "ContentType:" + contentType + "; filename:" + filename + "; Content:" + content + " }";
        }
    }

    //
    public int execute(HttpCallback cb) {
        if (qsClient == null)
            return QSHttpManage.getQSHttpClient().execute(this, cb);
        else
            return QSHttpManage.getQSHttpClient(qsClient).execute(this, cb);
    }
}
