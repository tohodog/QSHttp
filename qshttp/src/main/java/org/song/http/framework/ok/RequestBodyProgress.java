package org.song.http.framework.ok;

import org.song.http.framework.ability.IHttpProgress;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * Created by song on 2016/11/25
 * 添加了文件上传进度的RequestBody
 * 使用包装 唔...简洁多了 不用写实现
 */
public class RequestBodyProgress extends RequestBody {

    private IHttpProgress iHttpProgress;
    private RequestBody requestBody;
    private long len;
    private String mark;

    public RequestBodyProgress(RequestBody requestBody, IHttpProgress iHttpProgress, String key) {
        if (iHttpProgress == null)
            throw new IllegalArgumentException("IHttpProgress can not null");
        this.requestBody = requestBody;
        this.iHttpProgress = iHttpProgress;
        this.mark = key;
    }

    public RequestBodyProgress(RequestBody requestBody, IHttpProgress iHttpProgress) {
        this(requestBody, iHttpProgress, "up");
    }

    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        if (len != 0)
            return len;
        return len = requestBody.contentLength();
    }

    private BufferedSink bufferedSink;

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        if (bufferedSink == null) {
            bufferedSink = Okio.buffer(sink(sink));
        }
        requestBody.writeTo(bufferedSink);
        //必须调用flush，否则最后一部分数据可能不会被写入
        bufferedSink.flush();
    }

    //包装
    private Sink sink(Sink sink) {
        return new ForwardingSink(sink) {
            private long current;

            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                current += byteCount;
                iHttpProgress.onProgress(current, contentLength(), mark);
            }
        };
    }


}
