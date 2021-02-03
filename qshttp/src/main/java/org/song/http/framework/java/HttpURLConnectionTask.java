package org.song.http.framework.java;

import android.os.Build;

import org.song.http.framework.HttpEnum;
import org.song.http.framework.HttpException;
import org.song.http.framework.QSHttpConfig;
import org.song.http.framework.RequestParams;
import org.song.http.framework.ResponseParams;
import org.song.http.framework.ability.IHttpProgress;
import org.song.http.framework.ability.IHttpTask;
import org.song.http.framework.util.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by song on 2017/1/17.
 * 使用java原生的http实现(安卓其实用了okhttp实现) 实现基本功能 适合一般使用/精简apk包
 */

public class HttpURLConnectionTask implements IHttpTask {

    private QSHttpConfig qsHttpConfig;

    public HttpURLConnectionTask(QSHttpConfig qsHttpConfig) {
        this.qsHttpConfig = qsHttpConfig;
    }

    @Override
    public ResponseParams GET(RequestParams params, IHttpProgress hp) throws HttpException {
        HttpURLConnection conn = getHttpURLConnection(params.urlEncode(), HttpEnum.RequestMethod.GET.name(), params.headers(), params.timeOut());
        return getResponse(conn, params, hp);
    }

    @Override
    public ResponseParams P_FORM(RequestParams params, IHttpProgress hp) throws HttpException {
        HttpURLConnection conn = getHttpURLConnection(params.urlAndPath(), params.requestMethod().name(), params.headers(), params.timeOut());
        writeFromBody(conn, params.params(), params.charset());
        return getResponse(conn, params, hp);
    }

    @Override
    public ResponseParams P_BODY(RequestParams params, IHttpProgress hp) throws HttpException {
        HttpURLConnection conn = getHttpURLConnection(params.urlAndPath(), params.requestMethod().name(), params.headers(), params.timeOut());
        writeMediaBody(conn, params.requestBody().getContentType(), params.requestBody().getContent(), hp);
        return getResponse(conn, params, hp);
    }

    @Override
    public ResponseParams P_MULTIPART(RequestParams params, IHttpProgress hp) throws HttpException {
        HttpURLConnection conn = getHttpURLConnection(params.urlAndPath(), params.requestMethod().name(), params.headers(), params.timeOut());
        writeMultipartBody(conn, params.multipartType(), params.multipartBody(), hp);
        return getResponse(conn, params, null);
    }


    @Override
    public ResponseParams HEAD(RequestParams params) throws HttpException {
        HttpURLConnection conn = getHttpURLConnection(params.urlEncode(), HttpEnum.RequestMethod.HEAD.name(), params.headers(), params.timeOut());
        return getResponse(conn, params, null);
    }

    @Override
    public ResponseParams DELETE(RequestParams params) throws HttpException {
        HttpURLConnection conn = getHttpURLConnection(params.urlEncode(), HttpEnum.RequestMethod.DELETE.name(), params.headers(), params.timeOut());
        return getResponse(conn, params, null);
    }

    @Override
    public ResponseParams OPTIONS(RequestParams params) throws HttpException {
        HttpURLConnection conn = getHttpURLConnection(params.urlEncode(), HttpEnum.RequestMethod.OPTIONS.name(), params.headers(), params.timeOut());
        return getResponse(conn, params, null);
    }

    private HttpURLConnection getHttpURLConnection(String url, String method, Map<String, String> head, int timeOut) throws HttpException {
        try {
            HttpURLConnection conn;
            URL uri = new URL(url);
            if (qsHttpConfig.network() != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                conn = (HttpURLConnection) qsHttpConfig.network().openConnection(uri);//这里可能预请求了一次了
            else
                conn = (HttpURLConnection) uri.openConnection();//这里可能预请求了一次了

            if (conn instanceof HttpsURLConnection) {//支持自签名https
                SSLSocketFactory sslSocketFactory = Utils.checkSSL(url, qsHttpConfig);//获取本地的自签名证书
                if (sslSocketFactory != null) {//设置了自己的证书之后访问其他信任的https网址 会访问失败 所以check一下
                    ((HttpsURLConnection) conn).setSSLSocketFactory(sslSocketFactory);
                    ((HttpsURLConnection) conn).setHostnameVerifier(qsHttpConfig.hostnameVerifier());
                }
            }

            conn.setConnectTimeout(timeOut > 0 ? timeOut : qsHttpConfig.connectTimeout());
            conn.setReadTimeout(timeOut > 0 ? timeOut : qsHttpConfig.readTimeout());
            conn.setDoInput(true);// 允许输入
            //conn.setDoOutput(true);// 允许输出 设置了强制POST
            conn.setUseCaches(false);

            conn.setRequestProperty("Connection", "keep-alive");
            if (head == null) head = new HashMap<>();
            for (Map.Entry<String, String> entry : head.entrySet())
                conn.setRequestProperty(entry.getKey(), entry.getValue());

            conn.setRequestMethod(method); // get/Post..方式
            return conn;
        } catch (MalformedURLException e) {
            throw HttpException.Custom("Url wrong format");
        } catch (ProtocolException e) {
            throw HttpException.Custom("Method wrong");
        } catch (SocketTimeoutException e) {
            throw HttpException.HttpTimeOut(e);
        } catch (IOException e) {
            throw HttpException.NetWork(e);
        }
    }

    /**
     * 键值对表单body
     */
    private void writeFromBody(HttpURLConnection conn, Map<String, Object> params, String charset) throws HttpException {
        conn.setDoOutput(true);// 允许输出

        conn.setRequestProperty(HttpEnum.HEAD_KEY_CT, HttpEnum.CONTENT_TYPE_URL_ + charset);

        if (params == null || params.size() == 0)
            return;
        StringBuilder sb = new StringBuilder();
        for (String name : params.keySet()) {
            String value = String.valueOf(params.get(name));
            value = Utils.URLEncoder(value, charset);
            sb.append(name).append("=").append(value).append("&");
        }
        sb.deleteCharAt(sb.length() - 1);

        try {
            OutputStream os = conn.getOutputStream();
            WriteHelp wh = new WriteHelp(os);
            wh.writeBytes(sb.toString().getBytes());
        } catch (SocketTimeoutException e) {
            conn.disconnect();
            throw HttpException.HttpTimeOut(e);
        } catch (IOException e) {
            conn.disconnect();
            throw HttpException.NetWork(e);
        }
    }

    /**
     * 自定义body
     */
    private void writeMediaBody(HttpURLConnection conn, String contentType, Object content, IHttpProgress hp) throws HttpException {
        conn.setDoOutput(true);// 允许输出
        conn.setRequestProperty(HttpEnum.HEAD_KEY_CT, contentType);
        Charset charset = Utils.charset(contentType);
        int len = 0;
        byte[] bytes = null;
        File file = null;
        if (content instanceof File) {
            file = (File) content;
            len = (int) file.length();
        } else if (content instanceof byte[]) {
            bytes = (byte[]) content;
            len = bytes.length;
        } else if (content != null) {
            bytes = content.toString().getBytes(charset);
            len = bytes.length;
        }
        try {
            OutputStream os = conn.getOutputStream();
            WriteHelp wh = new WriteHelp(os, hp, len);
            wh.setMark("up");
            if (file != null)
                wh.writeByFile(file);
            if (bytes != null)
                wh.writeBytes(bytes);

        } catch (SocketTimeoutException e) {
            conn.disconnect();
            throw HttpException.HttpTimeOut(e);
        } catch (IOException e) {
            conn.disconnect();
            throw HttpException.NetWork(e);
        }
    }

    /**
     * Multipart方式的body 上传多文件/参数
     */
    private void writeMultipartBody(HttpURLConnection conn, String multipartType,
                                    Map<String, RequestParams.RequestBody> content, IHttpProgress hp) throws HttpException {
        try {
            conn.setDoOutput(true);// 允许输出
            new MultipartHelp(conn, multipartType, content, hp).writeBody();
        } catch (SocketTimeoutException e) {
            conn.disconnect();
            throw HttpException.HttpTimeOut(e);
        } catch (IOException e) {
            conn.disconnect();
            throw HttpException.NetWork(e);
        }
    }

    /**
     * 读取响应结果
     */
    private ResponseParams getResponse(HttpURLConnection conn, RequestParams params, IHttpProgress hp) throws HttpException {
        try {
            final int code = conn.getResponseCode();
            final int contentLen = conn.getContentLength();
            InputStream in = conn.getInputStream();
            ReadHelp rh = new ReadHelp(in, hp, contentLen);
            //状态码出错
            if (code < 200 || code >= 300) {
                ResponseParams responseParams = new ResponseParams();
                responseParams.setHeaders(conn.getHeaderFields());
                byte[] bytes = rh.readBytes();
                String result = new String(bytes, Utils.charset(conn.getHeaderField(HttpEnum.HEAD_KEY_CT)));
                responseParams.setString(result);
                responseParams.setBytes(bytes);
                throw HttpException.HttpCode(code, result).responseParams(responseParams);
            }

            ResponseParams response = new ResponseParams();
            response.setHeaders(conn.getHeaderFields());
            HttpEnum.ResultType type = params.resultType();
            if (type == HttpEnum.ResultType.STRING) {
                response.setString(rh.readString(
                        Utils.charset(conn.getHeaderField(HttpEnum.HEAD_KEY_CT))));
            } else if (type == HttpEnum.ResultType.BYTES) {
                response.setBytes(rh.readBytes());
            } else if (type == HttpEnum.ResultType.FILE) {
                String filePath = params.downloadPath();
                File downFile = new File(filePath);
                //File tempFile = new File(filePath + ".temp");
                downFile.getParentFile().mkdirs();
                rh.readToFile(downFile);
                response.setFile(filePath);
            } else if (type == HttpEnum.ResultType.STREAM) {
                rh.readToStream(params.outputStream(), false);
                response.setOutputStream(params.outputStream());
            }
            return response;
        } catch (SocketTimeoutException e) {
            throw HttpException.HttpTimeOut(e);
        } catch (UnknownHostException e) {
            throw HttpException.NetWork(e);
        } catch (ConnectException e) {
            throw HttpException.NetWork(e);
        } catch (BindException e) {
            throw HttpException.NetWork(e);
        } catch (SocketException e) {
            throw HttpException.NetWork(e);
        } catch (SSLException e) {
            throw HttpException.NetWork(e);
        } catch (IOException e) {
            throw HttpException.IO(e);
        } finally {
            conn.disconnect();
        }

    }

//
//    //包装
//    private Sink sink(final Sink sink, final IHttpProgress hp, final int contentLen) {
//        return new ForwardingSink(sink) {
//            private long current;
//
//            @Override
//            public void write(Buffer source, long byteCount) throws IOException {
//                super.write(source, byteCount);
//                current += byteCount;
//                hp.onProgress(current, contentLen, "");
//            }
//        };
//    }
//
//    //包装
//    private Source source(Source source, final IHttpProgress hp, final int contentLen) {
//        return new ForwardingSource(source) {
//            //当前读取字节数
//            long totalBytesRead = 0L;
//
//            @Override
//            public long read(Buffer sink, long byteCount) throws IOException {
//                long bytesRead = super.read(sink, byteCount);
//                //增加当前读取的字节数，如果读取完成了bytesRead会返回-1
//                totalBytesRead += bytesRead != -1 ? bytesRead : 0;
//                //回调，如果contentLength()不知道长度，会返回-1
//                hp.onProgress(totalBytesRead, bytesRead == -1 ? totalBytesRead : contentLen, "download...");
//                return bytesRead;
//            }
//        };
//    }

}
