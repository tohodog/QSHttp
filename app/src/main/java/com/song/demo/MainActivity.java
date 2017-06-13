package com.song.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONObject;
import org.song.http.QSHttp;
import org.song.http.framework.HttpCallback;
import org.song.http.framework.HttpManage;
import org.song.http.framework.HttpEnum;
import org.song.http.framework.HttpException;
import org.song.http.framework.Interceptor;
import org.song.http.framework.Parser;
import org.song.http.framework.ProgressCallback;
import org.song.http.framework.RequestParams;
import org.song.http.framework.ResponseParams;
import org.song.http.framework.Utils;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化框架 调用一次即可
        HttpManage.init(getApplication());
        //配置需要自签名的网站 读取assets/cers文件夹里的证书
        HttpManage.setSSL(Utils.getAssetsSocketFactory(this, "cers"), "kyfw.12306.cn","...");
        HttpManage.xx_http = HttpEnum.XX_Http.JAVA_HTTP;
        //...


        //拦截器 添加头参数 鉴权
        HttpManage .setInterceptor(new Interceptor() {
            @Override
            public ResponseParams intercept(Chain chain) throws HttpException {
                RequestParams r = chain.request().newBuild().header("username", "23333").build();
                return chain.proceed(r);
            }
        });

        tv = (TextView) findViewById(R.id.textview);

        httpsTest("https://www.baidu.com/s");
        httpsTest("https://kyfw.12306.cn/otn/");
    }

    //基本所有api介绍
    public void allAPI() {
        String url = "https://www.baidu.com/s";
        QSHttp.post(url)//选择请求的类型
                .param("userid", 123456)//键值对参数,get post postjson upload(multipart)支持此参数
                .restful("video", 2333)//构建成这样的url https://www.baidu.com/s/video/233
                .postJson(new Bean())//传入一个对象 会自动转化为json上传
                .header("User-Agent", "QsHttp/Android")//添加请求头
                .uploadByte("bytes", new byte[1024])//multipart方式上传一个字节数组
                .uploadFile("file", new File("xx.jpg"))//multipart方式上传一个文件

                .parser(parser)//自定义解析,由自己写解析逻辑
                .jsonModel(Bean.class)//使用FastJson自动解析json,传一个实体类即可

                .resultByBytes()//请求结果返回一个字节组 默认是返回字符
                .resultByFile(".../1.txt")//本地路径 有此参数 请求的内容将被写入文件

                .customContent("image/jpeg", new File("xx.jpg"))//直接上传自定义的内容 自定义contentType (postjson内部是调用这个实现)
                .errCache()//开启这个 [联网失败]会使用缓存,如果有的话

                .openServerCache()//开启服务器缓存规则 基于okhttp支持
                //构建好参数和配置后调用执行联网
                .buildAndExecute(new ProgressCallback() {

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
                        Bean b = response.parserObject();
                        b.getUserid();
                    }

                    @Override
                    public void onFailure(HttpException e) {
                        e.show();//弹出错误提示 网络连接失败 超时 404 解析失败 ...等
                    }
                });


    }

    //自签名https网址测试 证书放在 assets里
    public void httpsTest(String url) {

        tv.append("请求 " + url + "\n");
        QSHttp.get(url)
                .param("wd", "安卓http")
                .param("ie", "UTF-8").buildAndExecute(new HttpCallback() {
            @Override
            public void onSuccess(ResponseParams response) {
                tv.append(response.requestParams().url() + "成功\n");
            }

            @Override
            public void onFailure(HttpException e) {
                e.show();
                tv.append(e.getPrompt() + "\n");
            }
        });
    }


    //普通带参数 get
    public void normalGET() {

        String url = "https://www.baidu.com/s";
        QSHttp.get(url)
                .param("wd", "安卓http")
                .param("ie", "UTF-8")//自动构建url--https://www.baidu.com/s?ie=UTF-8&wd=安卓http
                //.restful(123,11) 这个参数会构建这样的url--https://www.baidu.com/s/123/11
                .buildAndExecute(new HttpCallback() {
                    @Override
                    public void onSuccess(ResponseParams response) {
                        tv.setText(response.string());
                    }

                    @Override
                    public void onFailure(HttpException e) {
                        e.show();
                    }
                });
    }

    //普通键值对 post
    public void normalPost() {
        String url = "http://api.kpie.com.cn/comments/play";
        QSHttp.post(url)
                .param("userid", 10086)
                .param("password", "qwe123456对")
                .buildAndExecute(new HttpCallback() {
                    @Override
                    public void onSuccess(ResponseParams response) {
                        tv.setText(response.string());
                    }

                    @Override
                    public void onFailure(HttpException e) {
                        e.show();
                    }
                });
    }


    //post一个json给服务器 并自动解析服务器返回信息
    public void jsonPost() {
        String url = "https://www.baidu.com";
        QSHttp.postCustom(url)
                .param("userid", 10086)
                .param("password", "qwe123456")
                //.postJson(Object) 这个参数可以直接传一个实体类,fastjson会自动转化成json字符串
                .jsonModel(Bean.class)
                .buildAndExecute(new HttpCallback() {
                    @Override
                    public void onSuccess(ResponseParams response) {
                        tv.setText(response.string());
                        Bean b = response.parserObject();
                        b.getUserid();
                    }

                    @Override
                    public void onFailure(HttpException e) {
                        e.show();
                    }
                });
    }


    //文件下载
    public void downGET() {
        String url = "https://www.baidu.com";
        QSHttp.download(url, ".../xxx.txt")
                .buildAndExecute(new ProgressCallback() {
                    @Override
                    public void onProgress(long var1, long var2, String var3) {
                        tv.append(var1 * 100 / var2 + "%\n");
                    }

                    @Override
                    public void onSuccess(ResponseParams response) {
                        tv.setText(response.headers().toString());//获取响应求头
                    }

                    @Override
                    public void onFailure(HttpException e) {
                        e.show();
                    }
                });
    }


    //文件上传
    public void upLoad() {
        String url = "https://www.baidu.com";
        QSHttp.upload(url)
                .param("userid", 10086)
                .param("password", "qwe123456")

                .uploadByte("bytes", new byte[1024])//multipart方式上传一个字节数组
                .uploadFile("file", new File("xx.jpg"))//multipart方式上传一个文件

                .buildAndExecute(new ProgressCallback() {
                    @Override
                    public void onProgress(long var1, long var2, String var3) {
                        tv.append(var1 * 100 / var2 + "%\n");
                    }

                    @Override
                    public void onSuccess(ResponseParams response) {
                        tv.setText(response.headers().toString());//获取响应求头
                    }

                    @Override
                    public void onFailure(HttpException e) {
                        e.show();
                    }
                });
    }


    Parser parser = new Parser<Bean>() {
        @Override
        public Bean parser(String result) throws Exception {
            JSONObject j = new JSONObject(result);

            return null;
        }
    };
}
