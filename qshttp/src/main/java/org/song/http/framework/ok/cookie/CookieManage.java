package org.song.http.framework.ok.cookie;

import android.app.Application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * Created by song on 2016/11/16.
 */

public class CookieManage implements CookieJar {


    private PersistentCookieStore cookieStore;
    private HashMap<HttpUrl, List<Cookie>> cookieMap = new HashMap<>();

    public CookieManage(Application application) {
        if (application != null)
            cookieStore = new PersistentCookieStore(application);
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        if (cookieStore != null) {
            if (cookies != null && cookies.size() > 0)
                for (Cookie item : cookies)
                    cookieStore.add(url, item);
        } else
            cookieMap.put(url, cookies);
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> cookies;
        if (cookieStore != null)
            cookies = cookieStore.get(url);
        else
            cookies = cookieMap.get(url);
        return cookies != null ? cookies : new ArrayList<Cookie>();
    }
}
