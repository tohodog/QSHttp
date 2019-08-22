package org.song.http.framework;

import android.app.Activity;
import android.app.Fragment;
import android.os.Build;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

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
        activity = findActivity(this);
    }

    public QSHttpCallback(Activity activity) {
        this.activity = activity;
    }


    public abstract void onComplete(T dataBean);


    @Override
    public void onSuccess(ResponseParams response) {
        this.response = response;
        try {
            onComplete(parserT(response.string()));
        } catch (JSONException e) {
            e.printStackTrace();
            onFailure(HttpException.Parser(e).responseParams(response));
        }
    }

    protected T parserT(Object json) throws JSONException {
//        return TypeUtils.cast(json, findT(), ParserConfig.global);//解析不到
        return parserT(String.valueOf(json));
    }

    protected T parserT(String json) throws JSONException {
        T dataBean = null;
        Type type = findT();
        if (type == String.class) {
            dataBean = (T) json;
        } else {
            dataBean = JSON.parseObject(json, type);
        }
        return dataBean;
    }

    protected Type findT() {
        Type type = getClass().getGenericSuperclass();

        if (!(type instanceof ParameterizedType)) {
            type = String.class;
        } else {
            ParameterizedType parameterizedType = (ParameterizedType) type;

//            if (parameterizedType.getActualTypeArguments()[0] instanceof ParameterizedType) {
//                ParameterizedType parameterizedType1 = (ParameterizedType) parameterizedType.getActualTypeArguments()[0];
//                type = parameterizedType1.getRawType(); //T=List<xxx>
//                Class<?> clazz = (Class<?>) parameterizedType1.getActualTypeArguments()[0];
//            } else {
            type = parameterizedType.getActualTypeArguments()[0];
//            }
        }
        return type;
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

    public static <T> List<T> parserList(JSONArray jsonArray, Class<T> _class) {
        List<T> list = new ArrayList<>();
        if (jsonArray != null)
            for (int i = 0; i < jsonArray.size(); i++) {
                list.add(jsonArray.getJSONObject(i).toJavaObject(_class));
            }
        return list;
    }

    public static Activity findActivity(Object o) {
        //获取外部类
        Object ext = field(o, "this$0");
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
            try {
                Class<?> exClass = Class.forName("android.support.v4.app.Fragment");
                if (exClass.isAssignableFrom(ext.getClass())) {
                    Method method = exClass.getMethod("getActivity");
                    method.setAccessible(true);
                    return (Activity) method.invoke(ext, new Object[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Class<?> exClass = Class.forName("androidx.support.v4.app.Fragment");
                if (exClass.isAssignableFrom(ext.getClass())) {
                    Method method = exClass.getMethod("getActivity");
                    method.setAccessible(true);
                    return (Activity) method.invoke(ext, new Object[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Object field(Object base, String fieldName) {
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
        }
        return null;
    }
}
