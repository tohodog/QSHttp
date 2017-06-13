package org.song.http.framework;

import org.song.http.framework.IHttpProgress;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by song on 2017/1/19.
 */

public class WriteHelp {

    OutputStream os;
    IHttpProgress hp;
    int writeCount, allCount=-1;
    String mark = "";

    public WriteHelp(OutputStream os, IHttpProgress hp, int allCount) {
        this.os = os;
        this.hp = hp;
        this.allCount = allCount;
    }

    public WriteHelp(OutputStream os) {
        this.os = os;
    }

    public void writeBytes(byte[] bytes) throws IOException {
        mark = "up byte[]";

        int offset = 0, all = bytes.length, buf = 4 * 1024;
        while (offset < all) {
            int len = Math.min(buf, all - offset);
            write(bytes, offset, len);
            offset += len;
        }
    }

    //资源来自文件
    public void writeByFile(File file) throws IOException {
        mark = file.getName();
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        byte[] buf = new byte[4 * 1024];
        int len;
        while ((len = bis.read(buf)) > 0) {
            write(buf, 0, len);
        }
        fis.close();
        bis.close();
    }

    private void write(byte[] bytes, int offset, int count) throws IOException {
        os.write(bytes, offset, count);
        writeCount += count;
        if (hp != null)
            hp.onProgress(writeCount, allCount, mark);
    }

}
