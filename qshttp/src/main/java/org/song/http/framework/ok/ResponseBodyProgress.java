package org.song.http.framework.ok;

import org.song.http.framework.ability.IHttpProgress;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * Created by song on 2016/9/24.
 * 包装添加了文件下载进度的ResponseBody
 */
public class ResponseBodyProgress extends ResponseBody {

    private ResponseBody responseBody;
    private IHttpProgress iHttpProgress;
    private BufferedSource bufferedSource;

    public ResponseBodyProgress(ResponseBody responseBody, IHttpProgress iHttpProgress) {
        if (iHttpProgress == null)
            throw new IllegalArgumentException("IHttpProgress can not null");
        this.responseBody = responseBody;
        this.iHttpProgress = iHttpProgress;
    }


    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            //包装
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    /**
     * 读取，回调进度接口
     *
     * @param source Source
     * @return Source
     */
    private Source source(Source source) {

        return new ForwardingSource(source) {
            //当前读取字节数
            long totalBytesRead = 0L;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                //增加当前读取的字节数，如果读取完成了bytesRead会返回-1
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                //回调，如果contentLength()不知道长度，会返回-1
                iHttpProgress.onProgress(totalBytesRead, bytesRead == -1 ? totalBytesRead : responseBody.contentLength(), "down");
                return bytesRead;
            }
        };
    }
}
