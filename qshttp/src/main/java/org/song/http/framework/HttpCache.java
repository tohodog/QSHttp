package org.song.http.framework;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.song.http.framework.HttpEnum.CacheMode;

/**
 * Created by song on 2016/9/18.
 * 缓存
 * 目前实现 ERR_CACHE
 * <p>
 * 后期会把okhttp的缓存移到这里
 */
public class HttpCache {

    private static HttpCache instance;
    private final String TAG = Utils.TAG;

    public static HttpCache instance() {
        if (instance == null)
            instance = new HttpCache();
        return instance;
    }

    private HttpCache() {
    }

    //检测使用缓存-联网前
    public ResponseParams checkAndGetCache(RequestParams result) {
        return null;
    }


    //检测使用缓存-联网失败后
    public boolean checkAndGetErrCache(ResponseParams response) {
        if (response.requestParams().cacheMode() != CacheMode.ERR_CACHE)
            return false;
        String MD5 = getRequestMD5(response.requestParams());
        switch (response.requestParams().resultType()) {
            case STRING:
                String s = Utils.readString(MD5);
                if (s == null)
                    return false;
                Log.e(TAG, "getErrCache->" + s);
                response.setString(s);
                return true;
            case FILE:

                break;
            case BYTES:
                byte[] b = Utils.readBytes(MD5);
                if (b == null || b.length == 0)
                    return false;
                Log.e(TAG, "getErrCache->" + b.length);
                response.setBytes(b);
                return true;
        }

        return false;
    }

    //检测持久化缓存-联网成功后
    public boolean checkAndSaveCache(ResponseParams response) {

        if (response.requestParams().cacheMode() == CacheMode.ERR_CACHE) {
            String MD5 = getRequestMD5(response.requestParams());
            Log.e(TAG, "saveErrCache->" + MD5);
            switch (response.requestParams().resultType()) {
                case STRING:
                    return Utils.writerString(MD5, response.string());
                case FILE:

                    break;
                case BYTES:
                    return Utils.writerBytes(MD5, response.bytes());
            }
        }

        //...

        return false;
    }


    private String getRequestMD5(RequestParams params) {
        if (params.customContent() != null)
            return StringToMD5(params.url() + params.customContent().getContent());
        else
            return StringToMD5(params.urlFormat());
    }

    private static String StringToMD5(String string) {
        byte[] hash;

        try {
            hash = MessageDigest.getInstance("MD5").digest(
                    string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }

        return hex.toString();
    }
}
