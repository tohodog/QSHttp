package org.song.http.framework.ability;

/**
 * Created by song on 2016/9/18.
 */
public interface IHttpProgress {
    //当前进度 文件大小 文件名/标记
    void onProgress(long rwLen, long allLen, String mark);

}
