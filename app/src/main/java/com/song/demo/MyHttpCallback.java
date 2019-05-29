package com.song.demo;


import android.app.Activity;
import android.app.Fragment;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.song.http.framework.HttpCallbackEx;
import org.song.http.framework.HttpException;
import org.song.http.framework.ResponseParams;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

/*
 * Created by song on 2019/4/16.
 * 根据自己的项目对回调进行再包装
 */
public abstract class MyHttpCallback<T> implements HttpCallbackEx {


    public abstract void onComplete(T dataBean);

    protected ResponseParams response;
    protected JSONObject jsonObject;

    @Override
    public void onSuccess(ResponseParams response) {
        this.response = response;
        try {
            jsonObject = JSON.parseObject(response.string(), JSONObject.class);
            //服务器状态码不对
            if (jsonObject.getInteger("status") == null || jsonObject.getInteger("status") != 0) {
                onFailure(HttpException.Custom(jsonObject.getString("msg")));
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            onFailure(HttpException.Parser(e).responseParams(response));
            return;
        }

        //拿到泛型，解析json
        T dataBean = null;
        Exception exception = null;
        try {
            ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
            //T=List<xxx>
            if (parameterizedType.getActualTypeArguments()[0] instanceof ParameterizedType) {
                ParameterizedType parameterizedType1 = (ParameterizedType) parameterizedType.getActualTypeArguments()[0];
                dataBean = (T) ((JSON) jsonObject.get("data")).toJavaObject((Class<Object>) parameterizedType1.getRawType());
            } else {
                Class<T> clazz = (Class<T>) parameterizedType.getActualTypeArguments()[0];
                dataBean = jsonObject.getJSONObject("data").toJavaObject(clazz);
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
        return activity != null && activity.isFinishing();
    }

    protected Activity activity;

    public MyHttpCallback() {
        this.activity = findActivity();
    }

    public MyHttpCallback(Activity activity) {
        this.activity = activity;
    }

    private Activity findActivity() {
        //获取外部类
        Object ext = field(this, "this$0");
        if (ext != null) {
            if (ext instanceof Activity) {
                return ((Activity) ext);
            } else if (ext instanceof Fragment && ((Fragment) ext).getActivity() != null) {
                return ((Fragment) ext).getActivity();
            } else if (ext instanceof android.support.v4.app.Fragment && ((android.support.v4.app.Fragment) ext).getActivity() != null) {
                return ((android.support.v4.app.Fragment) ext).getActivity();
            } else if (ext instanceof View) {
                View view = (View) ext;
                if ((view.getContext() instanceof Activity)) {
                    return ((Activity) view.getContext());
                }
            }
        }
        return null;
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
