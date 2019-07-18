package com.song.demo;


import org.song.http.framework.HttpException;
import org.song.http.framework.QSHttpCallback;

/*
 * Created by song on 2019/5/16.
 * 根据自己的项目对回调进行再包装
 * todo 目前用不了 QSHttpCallback无法获取到ResultModel<M>,只能获取到M
 */
@Deprecated
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

    public Class<?> getModel() {
        return ResultModel.class;
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
