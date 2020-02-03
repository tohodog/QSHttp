package com.song.demo;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.annotation.CallSuper;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import org.song.http.framework.HttpException;
import org.song.http.framework.QSHttpCallback;
import org.song.http.framework.ResponseParams;

/*
 * Created by song on 2019/4/16.
 * 根据自己的项目对回调进行再包装
 */
public abstract class MyHttpCallback<T> extends QSHttpCallback<T> {

    @Override
    public void onSuccess(ResponseParams response) {
        this.response = response;

        try {
            JSONObject jsonObject = JSON.parseObject(response.string(), JSONObject.class);
            //服务器状态码不对
            if (jsonObject.getInteger("status") == null || jsonObject.getInteger("status") != 0) {
                onFailure(HttpException.Custom(jsonObject.getString("msg")));
                return;
            }

            onComplete(parserT(jsonObject.get("data")));
        } catch (JSONException e) {
            e.printStackTrace();
            onFailure(HttpException.Parser(e).responseParams(response));
        }
    }


    @Override
    public void onFailure(HttpException e) {
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
        if (mDialog == null && activity != null) {
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
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

}
