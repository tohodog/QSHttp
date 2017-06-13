package com.song.demo;


import org.song.http.framework.HttpCallbackEx;
import org.song.http.framework.HttpException;
import org.song.http.framework.ResponseParams;

/*
 * Created by song on 2016/9/21.
 * 根据自己的项目对回调进行再包装
 */
public abstract class QSHttpCallback implements HttpCallbackEx {
    @Override
    public final void onSuccess(ResponseParams object) {
        if (object.parserObject() instanceof ResultObject) {
            ResultObject resultObject = object.parserObject();
            if (!resultObject.isSuccess()) {
                onFailure(HttpException.Custom(resultObject.getMsg()));
                return;
            }
        }
        success(object);
    }

    @Override
    public final void onFailure(HttpException e) {
        failure(e);
    }

    protected abstract void success(ResponseParams object);

    protected abstract void failure(HttpException e);


    @Override
    public void onStart() {

    }

    @Override
    public void onEnd() {

    }
}
