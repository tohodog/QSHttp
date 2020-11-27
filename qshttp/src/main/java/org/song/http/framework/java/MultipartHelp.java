package org.song.http.framework.java;

import org.song.http.framework.HttpEnum;
import org.song.http.framework.ability.IHttpProgress;
import org.song.http.framework.RequestParams;
import org.song.http.framework.util.Utils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Map;

/**
 * Created by song on 2017/1/19.
 * Multipart方式上传内容构造
 */

public class MultipartHelp {

    private final HttpURLConnection conn;
    private final String multipartType;
    private final Map<String, RequestParams.RequestBody> upContent;
    private final IHttpProgress hp;

    private final String charset;
    private final String BOUNDARY = java.util.UUID.randomUUID().toString();
    private final String PREFIX = "--", LINEND = "\r\n";


    MultipartHelp(HttpURLConnection conn,
                  String multipartType,
                  Map<String, RequestParams.RequestBody> upContent,
                  IHttpProgress hp) {
        this.conn = conn;
        this.charset = Utils.charsetName(multipartType);
        this.multipartType = multipartType;
        this.upContent = upContent;
        this.hp = hp;

    }

    public void writeBody() throws IOException {

        conn.setRequestProperty("Charset", charset);
        conn.setRequestProperty(HttpEnum.HEAD_KEY_CT, multipartType
                + ";boundary=" + BOUNDARY);
        OutputStream os = conn.getOutputStream();

//        WriteHelp wh = new WriteHelp(os);

        // 首先组拼文本类型的参数
//        StringBuilder sb = new StringBuilder();
//        if (params != null) {
//            for (Map.Entry<String, Object> entry : params.entrySet()) {
//                sb.append(PREFIX);
//                sb.append(BOUNDARY);
//                sb.append(LINEND);
//                sb.append("Content-Disposition: form-data; name=\""
//                        + entry.getKey() + "\"" + LINEND);
//                sb.append("Content-Type: text/plain; charset=" + charset
//                        + LINEND);
//                sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
//                sb.append(LINEND);
//                sb.append(entry.getValue());
//                sb.append(LINEND);
//            }
//            wh.writeBytes(sb.toString().getBytes(charset));
//        }

        if (upContent != null)
            for (Map.Entry<String, RequestParams.RequestBody> content : upContent.entrySet()) {
                RequestParams.RequestBody body = content.getValue();
                StringBuilder sb1 = new StringBuilder();
                sb1.append(PREFIX);
                sb1.append(BOUNDARY);
                sb1.append(LINEND);
                sb1.append("Content-Disposition: form-data; name=\"" + content.getKey() + "\"");
                if (body.getFilename() != null) {
                    sb1.append("; filename=\"" + body.getFilename() + "\"");
                }
                sb1.append(LINEND);
                sb1.append("Content-Type: " + body.getContentType() + LINEND);
                sb1.append(LINEND);
                os.write(sb1.toString().getBytes(charset));
                //写入上传内容
                writeObject(os, body.getContent(), body.getCharset(), content.getKey());
                //结束
                os.write(LINEND.getBytes(charset));
            }

        // 请求结束标志
        byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
        os.write(end_data);
        os.flush();
    }


    private void writeObject(OutputStream os, Object content, String charset, String key) throws IOException {
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

        WriteHelp wh = new WriteHelp(os, hp, len);
        wh.setMark(key);
        if (file != null)
            wh.writeByFile(file);
        if (bytes != null)
            wh.writeBytes(bytes);
    }
}
