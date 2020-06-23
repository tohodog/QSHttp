package com.song.demo;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.song.http.QSHttp;
import org.song.http.framework.HttpCallback;
import org.song.http.framework.HttpException;
import org.song.http.framework.Parser;
import org.song.http.framework.HttpCallbackProgress;
import org.song.http.framework.QSHttpCallback;
import org.song.http.framework.QSHttpConfig;
import org.song.http.framework.ResponseParams;
import org.song.http.framework.TrustAllCerts;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView tv;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }

        tv = (TextView) findViewById(R.id.textview);
        imageView = (ImageView) findViewById(R.id.imageView);

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetManager.select4GNetwork();
            }
        });

        String url = "/api_test";
        normalGET(url);
        normalPost(url);
        jsonPost(url);
        downGET(url);
        upLoad(url);

        parserJson();

    }


    //普通带参数 get
    public void normalGET(String url) {

        QSHttp.get(url)
                .param("wd", "安卓http")
                .param("ie", "UTF-8")//自动构建url--https://www.baidu.com/s?ie=UTF-8&wd=安卓http
                //.path(123,11) 这个参数会构建这样的url--https://www.baidu.com/s/123/11
                .buildAndExecute(new HttpCallback() {
                    @Override
                    public void onSuccess(ResponseParams response) {
                        tv.append(response.requestParams().url() + "成功get\n");
                    }

                    @Override
                    public void onFailure(HttpException e) {
                        e.show();
                    }
                });
    }

    //普通键值对 post, application/x-www-form-urlencoded
    public void normalPost(String url) {
        QSHttp.post(url)
                .param("userid", 10086)
                .param("password", "qwe123456对")
                .buildAndExecute(new MyHttpCallback<JSONObject>() {
                    @Override
                    public void onComplete(JSONObject dataBean) {
                        tv.append(response.requestParams().url() + "成功post\n");
                    }
                });
    }


    //post一个json给服务器,并自动解析服务器返回信息,application/json
    public void jsonPost(String url) {
        QSHttp.postJSON(url)
                .param("userid", 10086)
                .param("password", "qwe123456")
                //.jsonBody(Object) 这个参数可以直接传一个实体类,fastjson会自动转化成json字符串
                //.jsonModel(User.class)//解析模型
                .buildAndExecute(new HttpCallback() {
                    @Override
                    public void onSuccess(ResponseParams response) {
                        tv.append(response.requestParams().url() + "成功postJSON\n");
//                        User b = response.parserObject();//解析好的模型
//                        b.getUserName();
                    }

                    @Override
                    public void onFailure(HttpException e) {
                        e.show();
                    }
                });

        QSHttp.putJSON(url)
                .param("userid", 10086)
                .param("password", "qwe123456")
                .buildAndExecute(new MyHttpCallback<JSONObject>() {
                    @Override
                    public void onComplete(JSONObject dataBean) {
                        tv.append(response.requestParams().url() + "成功postJSON\n");
                    }
                });
    }


    //文件下载
    public void downGET(String url) {
        QSHttp.download(url, getExternalCacheDir().getPath() + "/http.txt")
                .buildAndExecute(new HttpCallbackProgress() {
                    @Override
                    public void onProgress(long var1, long var2, String var3) {
                        Log.d("downGET:", var1 * 100 / var2 + "%\n");
                    }

                    @Override
                    public void onSuccess(ResponseParams response) {
                        tv.append(response.requestParams().url() + "成功down\n");
                    }

                    @Override
                    public void onFailure(HttpException e) {
                        e.show();
                    }
                });
    }


    //文件上传,multipart/form-data
    public void upLoad(String url) {
        QSHttp.upload(url)
                .param("userid", 10086)
                .param("password", "qwe123456")

                .param("bytes", new byte[1024])//multipart方式上传一个字节数组
                .param("file", new File(getExternalCacheDir(), "http.txt"))//multipart方式上传一个文件

                //IdentityHashMap支持重复key,需new
                .multipartBody(new String("img"), "image/*", "qs.jpg", new byte[1024])
                .multipartBody(new String("img"), "image/*", "qs.jpg", new byte[1024])

                .buildAndExecute(new HttpCallbackProgress() {
                    @Override
                    public void onProgress(long var1, long var2, String var3) {
                        Log.d("upLoad:", var1 * 100 / var2 + "%" + var3 + "\n");
                    }

                    @Override
                    public void onSuccess(ResponseParams response) {
                        tv.append(response.requestParams().url() + "成功upload\n");
                    }

                    @Override
                    public void onFailure(HttpException e) {
                        e.show();
                    }
                });


    }


    private void parserJson() {

        User dataUser = new User();
        User dataUser2 = new User();
        dataUser.setUserName("Yolanda");
        dataUser2.setUserName("Song");
        List<User> users = Arrays.asList(dataUser2);
        dataUser.setRows(users);


        QSHttp.postJSON("https://api.reol.top/test/json")
                .jsonBody(dataUser)
                .buildAndExecute(new MyHttpCallback<User<User>>() {
                    @Override
                    public void onComplete(User<User> dataUser) {
                        tv.append("MyHttpCallback.User<User>=" + JSON.toJSONString(dataUser) + dataUser.getRows().get(0).getClass() + "\n");
                    }
                });

        QSHttp.postJSON("https://api.reol.top/test/json")
                .jsonBody("3.666489")
                .buildAndExecute(new MyHttpCallback<Double>() {
                    @Override
                    public void onComplete(Double dataUser) {
                        tv.append("MyHttpCallback.Double=" + JSON.toJSONString(dataUser) + "\n");
                    }
                });

        QSHttp.postJSON("https://api.reol.top/test/json")
                .header("row", "row")
                .jsonBody(users)
                .buildAndExecute(new QSHttpCallback<List<User>>() {
                    @Override
                    public void onComplete(List<User> dataUser) {
                        tv.append("QSHttpCallback.List<User>=" + JSON.toJSONString(dataUser) + dataUser.get(0).getClass() + "\n");
                    }
                });

        QSHttp.postJSON("https://api.reol.top/test/json")
                .header("row", "row")
                .jsonBody(dataUser)
                .buildAndExecute(new QSHttpCallback<User>() {
                    @Override
                    public void onComplete(User dataUser) {
                        tv.append("QSHttpCallback.User=" + JSON.toJSONString(dataUser) + dataUser.getClass() + "\n");
                    }
                });

        QSHttp.postJSON("https://api.reol.top/test/json")
                .header("row", "row")
                .jsonBody("3.6")
                .buildAndExecute(new QSHttpCallback<String>() {
                    @Override
                    public void onComplete(String dataUser) {
                        tv.append("QSHttpCallback.String=" + dataUser + "\n");
                    }
                });

    }


    //基本所有api介绍
    public void allAPI() {
        String url = "https://www.baidu.com/s";
        QSHttp.post(url)//选择请求的类型
                .header("User-Agent", "QsHttp/Android")//添加请求头

                .path(2333, "video")//构建成这样的url https://www.baidu.com/s/2233/video

                .param("userid", 123456)//键值对参数
                .param("password", "asdfgh")//键值对参数
                .param(new User())//键值对参数

                .toJsonBody()//把 params 转为json;application/json
                .jsonBody(new User())//传入一个对象,会自动转化为json上传;application/json

                .requestBody("image/jpeg", new File("xx.jpg"))//直接上传自定义的内容 自定义contentType (postjson内部是调用这个实现)

                .param("bytes", new byte[1024])//传一个字节数组,multipart支持此参数
                .param("file", new File("xx.jpg"))//传一个文件,multipart支持此参数
                .toMultiBody()//把 params 转为multipartBody参数;multipart/form-data


                .parser(parser)//自定义解析,由自己写解析逻辑
                .jsonModel(User.class)//使用FastJson自动解析json,传一个实体类即可

                .resultByBytes()//请求结果返回一个字节组 默认是返回字符
                .resultByFile(".../1.txt")//本地路径 有此参数 请求的内容将被写入文件

                .errCache()//开启这个 [联网失败]会使用缓存,如果有的话
                .clientCache(24 * 3600)//开启缓存,有效时间一天
                .timeOut(10 * 1000)
                .openServerCache()//开启服务器缓存规则 基于okhttp支持

                //构建好参数和配置后调用执行联网
                .buildAndExecute(new HttpCallbackProgress() {

                    //-----回调均已在主线程

                    @Override
                    public void onProgress(long var1, long var2, String var3) {
                        //进度回调 不需要监听进度 buildAndExecute()传 new HttpCallback(){...}即可
                        long i = var1 * 100 / var2;//百分比
                        //var3 在传文件的时候为文件路径 其他无意义
                    }

                    @Override
                    public void onSuccess(ResponseParams response) {
                        response.string();//获得响应字符串 *默认
                        response.file();//设置了下载 获得路径
                        response.bytes();//设置了返回字节组 获得字节组

                        response.headers();//获得响应头

                        //获得自动解析/自定义解析的结果
                        User b = response.parserObject();
                        b.getUserName();
                    }

                    @Override
                    public void onFailure(HttpException e) {
                        e.show();//弹出错误提示 网络连接失败 超时 404 解析失败 ...等
                    }
                });

        //配置多个client
        QSHttp.addClient("server2", QSHttpConfig.Build(getApplication())
                .hostnameVerifier(new TrustAllCerts.TrustAllHostnameVerifier())//证书信任规则
                .cacheSize(128 * 1024 * 1024)
                .connectTimeout(18 * 1000)
                .debug(true)
                //拦截器 添加头参数 鉴权
                .interceptor(new QSInterceptor())
                .build());
        QSHttp.get("url").qsClient("server2").buildAndExecute();//将使用上述的配置

    }

    Parser parser = new Parser<User>() {
        @Override
        public User parser(String result) throws Exception {

            return null;
        }
    };
}
