package org.song.http.framework;

import com.alibaba.fastjson.JSON;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by song on 2016/9/18.
 * http请求配置及参数类
 */
public class RequestParams {

    private String url;
    private CustomContent customContent;//自定义的post内容
    private Map<String, String> params;//键值对参数
    private List<String> restfulParams;//restful参数
    private Map<String, Object> uploadContent;//上传内容参数
    private Map<String, String> headers;//头参数

    private Object tag;//标记

    private HttpEnum.RequestType requestType;//请求模式 GET/POST/...
    private HttpEnum.ResultType resultType;//返回类型
    private HttpEnum.ParserMode parserMode;//解析模式 返回类型为string有效
    private HttpEnum.CacheMode cacheMode;//服务器设置了缓存时的控制

    private String downloadPath;//下载文件路径
    private Class<?> _class;//自动解析需要的模型
    private Parser parser;//手动解析需要的实现类

    private int cacheTime;//手动缓存 设置有效时间 [非服务器给的缓存配置 功能待定

    private RequestParams() {
    }


    public String url() {
        return url;
    }

    public HttpEnum.RequestType requestType() {
        return requestType;
    }

    public HttpEnum.ResultType resultType() {
        return resultType;
    }

    public HttpEnum.CacheMode cacheMode() {
        return cacheMode;
    }

    public CustomContent customContent() {
        return customContent;
    }

    public Map<String, String> params() {
        return params;
    }

    public Map<String, Object> uploadContent() {
        return uploadContent;
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

    public int cacheTime() {
        return cacheTime;
    }

    public String downloadPath() {
        return downloadPath;
    }

    /**
     * 提取参数数据 构建带参数url格式
     */
    public String urlFormat() {
        StringBuffer sbUrl = new StringBuffer();
        sbUrl.append(url);

        if (restfulParams != null) {
            for (String value : restfulParams) {
                value = Utils.URLEncoder(value);
                sbUrl.append("/").append(value);
            }
        }

        if (params != null) {
            sbUrl.append("?");
            for (String name : params.keySet()) {
                String value = String.valueOf(params.get(name));
                value = Utils.URLEncoder(value);
                sbUrl.append(name).append("=").append(value).append("&");
            }
            sbUrl.deleteCharAt(sbUrl.length() - 1);
        }

        return sbUrl.toString();
    }

    public Builder newBuild() {
        Builder builder = new Builder(url);
        builder.url = url;
        builder.customContent = customContent;
        builder.restfulParams = restfulParams;
        builder.params = params;
        builder.uploadContent = uploadContent;
        builder.headers = headers;
        builder.tag = tag;

        builder.requestType = requestType;
        builder.resultType = resultType;
        builder.parserMode = parserMode;
        builder.cacheMode = cacheMode;

        builder._class = _class;
        builder.parser = parser;
        builder.cacheTime = cacheTime;
        builder.downloadPath = downloadPath;
        return builder;
    }

    public static Builder Build(String url) {
        return new Builder(url);
    }

    public static final class Builder {
        private String url;
        private CustomContent customContent;
        private Map<String, String> headers;
        private Map<String, String> params;
        private List<String> restfulParams;
        private Map<String, Object> uploadContent;

        private Object tag;//标记

        private HttpEnum.RequestType requestType = HttpEnum.RequestType.GET;//请求模式 GET/POST/...
        private HttpEnum.ResultType resultType = HttpEnum.ResultType.STRING;//返回类型
        private HttpEnum.ParserMode parserMode = HttpEnum.ParserMode.NOTHING;//解析模式
        private HttpEnum.CacheMode cacheMode = HttpEnum.CacheMode.NO_STORE;//缓存控制

        private Class<?> _class;//自动解析需要的模型
        private Parser parser;
        private String downloadPath;

        private int cacheTime;//手动缓存 设置有效时间 [非服务器给的缓存配置 功能待定

        public Builder(String url) {
            this.url = url;
        }

        public RequestParams build() {
            if (customContent == null && requestType == HttpEnum.RequestType.POST_CUSTOM)
                postJson(params);
            RequestParams requestParams = new RequestParams();
            requestParams.url = url;
            requestParams.restfulParams = restfulParams;
            requestParams.customContent = customContent;
            requestParams.params = params;
            requestParams.uploadContent = uploadContent;
            requestParams.headers = headers;
            requestParams.tag = tag;

            requestParams.requestType = requestType;
            requestParams.resultType = resultType;
            requestParams.parserMode = parserMode;
            requestParams.cacheMode = cacheMode;

            requestParams._class = _class;
            requestParams.parser = parser;
            requestParams.cacheTime = cacheTime;
            requestParams.downloadPath = downloadPath;

            return requestParams;
        }


        /**
         * 请求方式
         * get/post/..
         */
        public RequestParams.Builder requestType(HttpEnum.RequestType requestType) {
            if (requestType != null)
                this.requestType = requestType;
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
        @Deprecated
        public RequestParams.Builder clientCache(int timeout) {
            cacheTime(timeout);
            return cacheMode(HttpEnum.CacheMode.CLIENT_CACHE);
        }

        /**
         * get/post请求键值对参数
         */
        public RequestParams.Builder param(String key, Object value) {
            if (params == null)
                params = new HashMap<>();
            params.put(key, String.valueOf(value));
            return this;
        }

        /**
         * get/post请求键值对参数
         */
        public RequestParams.Builder param(Map<String, String> params) {
            this.params = params;
            return this;
        }

        /**
         * get请求/xx/xxx参数
         */
        public RequestParams.Builder restful(Object... value) {
            if (restfulParams == null)
                restfulParams = new ArrayList<>();
            if (value != null)
                for (Object o : value)
                    restfulParams.add(String.valueOf(o));
            return this;
        }

        /**
         * 上传文件参数
         */
        public RequestParams.Builder uploadFile(String key, File value) {
            if (uploadContent == null)
                uploadContent = new HashMap<>();
            uploadContent.put(key, value);
            return this;
        }

        /**
         * 上传字节参数
         */
        public RequestParams.Builder uploadByte(String key, byte[] value) {
            if (uploadContent == null)
                uploadContent = new HashMap<>();
            uploadContent.put(key, value);
            return this;
        }

        /**
         * http头参数
         * 非Content-Type的头参数
         */
        public RequestParams.Builder header(String key, String value) {
            if (headers == null)
                headers = new HashMap<>();
            headers.put(key, value);
            return this;
        }

        /**
         * post 一个json参数
         */
        public RequestParams.Builder postJson(Object postJson) {
            customContent(HttpManage.CONTENT_TYPE_JSON, JSON.toJSONString(postJson));
            return this;
        }

        /**
         * post 一个自定义内容(file byte[] string）
         */
        public RequestParams.Builder customContent(String contentType, Object content) {
            customContent = new CustomContent();
            customContent.setContent(content);
            customContent.setContentType(contentType);
            return this;
        }

        /**
         * 下载路径 请求时有这个参数则写入文件
         */
        public RequestParams.Builder downloadPath(String downloadPath) {
            this.downloadPath = downloadPath;
            resultType(HttpEnum.ResultType.FILE);
            return this;
        }

        /**
         * 自动解析模型
         * 默认json
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
         * 标记此次请求
         */
        public RequestParams.Builder tag(Object tag) {
            this.tag = tag;
            return this;
        }


        /**
         * 缓存时间 秒
         * 暂时没用
         */
        public RequestParams.Builder cacheTime(int cacheTime) {
            this.cacheTime = cacheTime;
            return this;
        }

        //构建并执行
        public int buildAndExecute(HttpCallback cb) {
            return build().execute(cb);
        }

        //构建并执行
        public int buildAndExecute() {
            return build().execute(null);
        }
    }

    public static class CustomContent {
        private Object content;
        private String contentType;

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
    }

    //
    public int execute(HttpCallback cb) {
        return HttpManage.getHttpClient().execute(this, cb);
    }
}
