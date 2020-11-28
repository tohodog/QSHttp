package org.song.http.framework.ability;

import org.song.http.framework.HttpException;
import org.song.http.framework.RequestParams;
import org.song.http.framework.ResponseParams;

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
     * 普通post/put/patch
     * application/x-www-form-urlencoded
     *
     * @param hp 下载的进度
     */
    ResponseParams P_FORM(RequestParams params, IHttpProgress hp) throws HttpException;

    /**
     * 自定义body的post/put/patch
     * contentType自主决定
     *
     * @param hp 上传的进度
     */
    ResponseParams P_BODY(RequestParams params, IHttpProgress hp) throws HttpException;

    /**
     * multipart/form-data 方式的post/put/patch
     *
     * @param hp 上传的进度
     */
    ResponseParams P_MULTIPART(RequestParams params, IHttpProgress hp) throws HttpException;

    ResponseParams HEAD(RequestParams params) throws HttpException;

    ResponseParams DELETE(RequestParams params) throws HttpException;

    ResponseParams OPTIONS(RequestParams params) throws HttpException;


}
