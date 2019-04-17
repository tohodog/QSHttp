package org.song.http.framework;

import android.app.Activity;
import android.app.Fragment;
import android.os.Build;

import com.alibaba.fastjson.JSON;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

/**
 * Created by song
 * Contact github.com/tohodog
 * Date 2019/4/11
 * 回调封装,支持解析,fu
 * 生命周期销毁(外部类是activity销毁了不会回调),不需要覆盖isDestroy方法
 */
public abstract class QSHttpCallback<T> implements HttpCallbackEx {


    public abstract void onComplete(T dataBean);

    protected ResponseParams response;

    @Override
    public void onSuccess(ResponseParams response) {
        this.response = response;
        T dataBean = null;
        Exception exception = null;
        try {
            ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
            //T=List<xxx>
            if (parameterizedType.getActualTypeArguments()[0] instanceof ParameterizedType) {
                ParameterizedType parameterizedType1 = (ParameterizedType) parameterizedType.getActualTypeArguments()[0];
                //parameterizedType1.getRawType() instanceof List
                Class<?> clazz = (Class<?>) parameterizedType1.getActualTypeArguments()[0];
                dataBean = (T) JSON.parseArray(response.string(), clazz);
            } else {
                Class<T> clazz = (Class<T>) parameterizedType.getActualTypeArguments()[0];
                dataBean = JSON.parseObject(response.string(), clazz);
            }
        } catch (Exception e) {
            e.printStackTrace();
            exception = e;
        }
        if (dataBean != null)
            onComplete(dataBean);
        else
            onFailure(HttpException.Parser(exception).responseParams(response));
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
        //获取外部类
        Object ext = field(this, "this$0");
        if (ext != null) {
            if (ext instanceof Activity) {
                return ((Activity) ext).isFinishing();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                if (ext instanceof Fragment && ((Fragment) ext).getActivity() != null) {
                    return ((Fragment) ext).getActivity().isFinishing();
                }
            }
        }
        return false;
    }


    private static Object field(Object base, String fieldName) {
        try {
            Class<?> clazz;
            if (base instanceof String) {
                clazz = Class.forName(base.toString());
                base = clazz;
            } else if (base instanceof Class)
                clazz = (Class<?>) base;
            else
                clazz = base.getClass();

            Field f = clazz.getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(base);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
