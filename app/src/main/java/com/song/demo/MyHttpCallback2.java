package com.song.demo;


import org.song.http.framework.HttpException;
import org.song.http.framework.QSHttpCallback;

import java.lang.reflect.ParameterizedType;

/*
 * Created by song on 2019/4/16.
 * 根据自己的项目对回调进行再包装
 */
public abstract class MyHttpCallback2<M> extends QSHttpCallback<ResultModel<M>> {


    public abstract void onNext(M dataBean);


    public void onComplete(ResultModel<M> dataBean) {
        if (dataBean.isSuccess()) {
            M data = dataBean.getData();
            if (data == null) {

            } else {
                onNext(data);
            }
        } else {
            onFailure(HttpException.Custom(dataBean.getMsg()));
        }
    }

    @Override
    public void onFailure(HttpException e) {
        e.show();
    }


    @Override
    public void onStart() {
    }

    @Override
    public void onEnd() {

    }

    @Override
    public boolean isDestroy() {
        return activity != null && activity.isFinishing();
    }
}
