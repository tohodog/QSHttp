package org.song.http.framework.ability;

import org.song.http.framework.HttpException;
import org.song.http.framework.ResponseParams;

/**
 * Created by song on 2016/9/18.
 */
public interface HttpCallback {

    void onSuccess(ResponseParams response);

    void onFailure(HttpException e);

}
