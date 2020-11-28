package org.song.http.framework.ability;

/**
 * Created by song on 2017/4/13.
 */
public interface HttpCallbackEx extends HttpCallback {

    void onStart();

    void onEnd();

    //返回true 则不会回调
    boolean isDestroy();
}
