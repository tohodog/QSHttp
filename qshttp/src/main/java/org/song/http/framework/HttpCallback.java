package org.song.http.framework;

/**
 * Created by song on 2016/9/18.
 */
public interface HttpCallback {

    void onSuccess(ResponseParams response);

    void onFailure(HttpException e);

}
