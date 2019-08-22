package com.song.demo;


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


}
