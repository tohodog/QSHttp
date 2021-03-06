package com.song.demo;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.annotation.CallSuper;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.song.http.framework.HttpException;
import org.song.http.framework.util.QSHttpCallback;

/*
 * Created by song on 2019/4/16.
 * 根据自己的项目对回调进行再包装
 */
public abstract class MyHttpCallback<T> extends QSHttpCallback<T> {

    protected boolean isShow = true;

    public MyHttpCallback() {
        super();
    }

    public MyHttpCallback(boolean isShow) {
        this.isShow = isShow;
    }

    public MyHttpCallback(Activity activity, boolean isShow) {
        super(activity);
        this.isShow = isShow;
    }

    @Override
    public T map(String response) throws HttpException {
        JSONObject jsonObject = JSON.parseObject(response);
        //服务器状态码不对
        if (jsonObject.getIntValue("status") != 0) {
            throw HttpException.Custom(jsonObject.getString("msg"), response);
        }
        return parserT(jsonObject.getString("data"));
    }


    @Override
    public void onFailure(HttpException e) {
        String response = (String) e.getExObject();//可获取非200异常的参数
        if (activity != null) Toast.makeText(activity, e.getPrompt(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStart() {
        showProgressDialog(false);
    }

    @CallSuper
    @Override
    public void onEnd() {
        dismissProgressDialog();
    }


    protected ProgressDialog mDialog;

    /**
     * 用于显示Dialog
     */
    protected void showProgressDialog(boolean mCancelable) {
        if (isShow && mDialog == null && activity != null && !activity.isFinishing()) {
            mDialog = new ProgressDialog(activity);
            mDialog.setCancelable(mCancelable);
            if (mCancelable) {
                mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                    }
                });
            }
            mDialog.show();
        }
    }

    protected void dismissProgressDialog() {
        if (isShow && mDialog != null && activity != null && !activity.isFinishing()) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

}
