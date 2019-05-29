package org.song.http.framework;

import android.app.Activity;
import android.app.Fragment;
import android.os.Build;
import android.view.View;

import com.alibaba.fastjson.JSON;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

/**
 * Created by song
 * Contact github.com/tohodog
 * Date 2019/4/11
 * 回调封装,支持解析,泛型
 * 生命周期销毁(外部类是activity销毁了不会回调),不想销毁覆盖isDestroy方法
 */
public abstract class QSHttpCallback<T> implements HttpCallbackEx {

    protected Activity activity;
    protected ResponseParams response;

    public QSHttpCallback() {
        activity = findActivity();
    }

    public QSHttpCallback(Activity activity) {
        this.activity = activity;
    }


    public abstract void onComplete(T dataBean);


    @Override
    public final void onSuccess(ResponseParams response) {
        this.response = response;
        T dataBean = null;
        Exception exception = null;
        try {
            ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
            //T=List<xxx>
            if (parameterizedType.getActualTypeArguments()[0] instanceof ParameterizedType) {
                ParameterizedType parameterizedType1 = (ParameterizedType) parameterizedType.getActualTypeArguments()[0];
//                if (List.class.getName().equals(parameterizedType1.getRawType().getTypeName())) {
//                    dataBean = (T) JSON.toJavaObject(JSON.parseArray(response.string()), (Class<Object>) parameterizedType1.getRawType());
//                } else {
//                    dataBean = (T) JSON.parseObject(response.string(), parameterizedType1.getRawType());
//                }
                dataBean = JSON.parseObject(response.string(), parameterizedType1.getRawType());
            } else {
                dataBean = JSON.parseObject(response.string(), parameterizedType.getActualTypeArguments()[0]);
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


    private Activity findActivity() {
        //获取外部类
        Object ext = field(this, "this$0");
        if (ext != null) {
            if (ext instanceof Activity) {
                return ((Activity) ext);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                if (ext instanceof Fragment && ((Fragment) ext).getActivity() != null) {
                    return ((Fragment) ext).getActivity();
                }
            }
            if (ext instanceof View) {
                View view = (View) ext;
                if ((view.getContext() instanceof Activity)) {
                    return ((Activity) view.getContext());
                }
            }
            if (ext.getClass().getName().equals("android.support.v4.app.Fragment")) {
                try {
                    Class<?> exClass = Class.forName("android.support.v4.app.Fragment");
                    Method method = exClass.getMethod("getActivity");
                    method.setAccessible(true);
                    return (Activity) method.invoke(ext, new Object[0]);
                } catch (Exception e) {
                    e.printStackTrace();
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
