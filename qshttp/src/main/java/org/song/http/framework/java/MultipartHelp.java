package org.song.http.framework.java;

import org.song.http.framework.HttpManage;
import org.song.http.framework.IHttpProgress;
import org.song.http.framework.WriteHelp;

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
    HttpURLConnection conn;
    Map<String, String> params;
    Map<String, Object> upContent;
    IHttpProgress hp;

    final String charset = "utf-8";
    final String BOUNDARY = java.util.UUID.randomUUID().toString();
    final String PREFIX = "--", LINEND = "\r\n";
    final String MULTIPART_FROM_DATA = HttpManage.CONTENT_TYPE_DATA;


    MultipartHelp(HttpURLConnection conn,
                  Map<String, String> params,
                  Map<String, Object> upContent,
                  IHttpProgress hp) {
        this.conn = conn;
        this.params = params;
        this.upContent = upContent;
        this.hp = hp;

    }

    public void writeBody() throws IOException {

        conn.setRequestProperty("Charset", charset);
        conn.setRequestProperty(HttpManage.HEAD_KEY_CT, MULTIPART_FROM_DATA
                + ";boundary=" + BOUNDARY);
        OutputStream os = conn.getOutputStream();

        WriteHelp wh = new WriteHelp(os);

        // 首先组拼文本类型的参数
        StringBuilder sb = new StringBuilder();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append(LINEND);
                sb.append("Content-Disposition: form-data; name=\""
                        + entry.getKey() + "\"" + LINEND);
                sb.append("Content-Type: text/plain; charset=" + charset
                        + LINEND);
                sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
                sb.append(LINEND);
                sb.append(entry.getValue());
                sb.append(LINEND);
            }
            wh.writeBytes(sb.toString().getBytes(charset));
        }

        if (upContent != null)
            for (Map.Entry<String, Object> content : upContent.entrySet()) {
                StringBuilder sb1 = new StringBuilder();
                sb1.append(PREFIX);
                sb1.append(BOUNDARY);
                sb1.append(LINEND);
                sb1.append("Content-Disposition: form-data; name=\"" + content.getKey() + "\"; filename=\""
                        + System.currentTimeMillis() + "\"" + LINEND);
                sb1.append("Content-Type: multipart/form-data; charset="
                        + charset + LINEND);
                sb1.append(LINEND);
                os.write(sb1.toString().getBytes(charset));
                //写入上传内容
                writeObject(os, content.getValue());
                //结束
                os.write(LINEND.getBytes(charset));
            }

        // 请求结束标志
        byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
        os.write(end_data);
        os.flush();
    }


    private void writeObject(OutputStream os, Object content) throws IOException {
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
        if (file != null)
            wh.writeByFile(file);
        if (bytes != null)
            wh.writeBytes(bytes);
    }
}
