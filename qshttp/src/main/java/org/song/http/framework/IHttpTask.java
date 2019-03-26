package org.song.http.framework;

/**
 * Created by song on 2016/9/14.
 */
public interface IHttpTask {
    /**
     * 普通get
     * application/x-www-form-urlencoded
     * 参数url编码
     *
     * @param hp 下载的进度
     */
    ResponseParams GET(RequestParams params, IHttpProgress hp) throws HttpException;

    /**
     * 普通post
     * application/x-www-form-urlencoded
     *
     * @param hp 下载的进度
     */
    ResponseParams POST_FORM(RequestParams params, IHttpProgress hp) throws HttpException;

    /**
     * 自定义body的post
     * contentType自主决定
     *
     * @param hp 上传的进度
     */
    ResponseParams POST_CUSTOM(RequestParams params, IHttpProgress hp) throws HttpException;

    /**
     * multipart/form-data 方式的post
     *
     * @param hp 上传的进度
     */
    ResponseParams POST_MULTIPART(RequestParams params, IHttpProgress hp) throws HttpException;

    ResponseParams PUT_FORM(RequestParams params, IHttpProgress hp) throws HttpException;

    ResponseParams PUT_CUSTOM(RequestParams params, IHttpProgress hp) throws HttpException;

    ResponseParams PUT_MULTIPART(RequestParams params, IHttpProgress hp) throws HttpException;

    ResponseParams HEAD(RequestParams params) throws HttpException;

    ResponseParams DELETE(RequestParams params) throws HttpException;

}
