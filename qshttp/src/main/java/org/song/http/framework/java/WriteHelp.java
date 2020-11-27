package org.song.http.framework.java;

import org.song.http.framework.ability.IHttpProgress;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by song on 2017/1/19.
 */

public class WriteHelp {

    private OutputStream os;
    private IHttpProgress hp;
    private int writeCount, allCount = -1;

    private String mark = "";

    public WriteHelp(OutputStream os, IHttpProgress hp, int allCount) {
        this.os = os;
        this.hp = hp;
        this.allCount = allCount;
    }

    public WriteHelp(OutputStream os) {
        this.os = os;
    }

    public WriteHelp(File f) throws FileNotFoundException {
        this.os = new FileOutputStream(f);
    }


    public void setMark(String mark) {
        this.mark = mark;
    }

    public void writeBytes(byte[] bytes) throws IOException {
        int offset = 0, all = bytes.length;
        while (offset < all) {
            int len = Math.min(1024, all - offset);
            write(bytes, offset, len);
            offset += len;
        }
    }

    //资源来自文件
    public void writeByFile(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        byte[] buf = new byte[1024];
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

    public void close() throws IOException {
        os.close();
    }

}
