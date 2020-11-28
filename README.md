QSHttp
====
[![QSHttp][QSHttpsvg]][QSHttp] [![fastjson][fastjsonsvg]][fastjson]  [![okhttp][okhttpsvg]][okhttp]  [![License][licensesvg]][license]

开箱即用,GET,POST,表单,JSON,上传,下载等等统统同一行代码搞定! One Code Man!  
AIP精简到极致,调用没有一行多余代码,几乎零成本使用,大道至简
<br>
  * 5年实战环境验证迭代,稳定可靠
  * 强大灵活的入参,支持泛型回调,使用简单
  * 可简单实现自动弹加载框,判断业务状态码,弹错误提示
  * 支持多拦截器,可全局配置一些公共鉴权参数
  * 支持异步(回调已在主线程),同步请求
  * 支持自签名,双向https
  * 支持自定义有效时间缓存,错误缓存(联网失败时使用),缓存控制,cookie自动管理
  * 详细的请求信息回调、错误类型(网络链接失败,超时,断网,解析失败,404...)
  * 详细的访问日记打印,非常方便调试
  * 底层支持原生和okhttp

### Gradle
```
//build.gradle
allprojects {
    repositories {
        maven {
            url "https://jitpack.io"
        }
    }
}

//app.gradle
dependencies {
    implementation 'com.github.tohodog:QSHttp:1.5.3'
}

```

### 最简单的例子
```
QSHttp.get("http://xxx").buildAndExecute();
```
### HTTP调试地址
https://api.reol.top/api_test
<br/>
可接受任何请求,该接口返回用户请求信息


### 初始化框架
```
        //初始化框架,调用一次即可,详细配置见#高级配置#
        QSHttp.init(getApplication());

```

### GET
```
        String url = "https://api.reol.top/api_test";
        //使用泛型回调,自动解析json数据成模型
        QSHttp.get(url)
                .param("name", "QSHttp")
                .buildAndExecute(new QSHttpCallback<BaseModel<User>>() {
                    @Override
                    public void onComplete(BaseModel<User> bean) {
                        //请求解析成功,执行业务逻辑,BaseModel是接口返回数据通用模型,如status,msg和泛型data
                    }
                    
                    //@Override//需要处理失败可覆盖
                    //public void onFailure(HttpException e) {
                    //    e.show();
                    //}
                });
```


### POST (application/x-www-form-urlencoded)
```
        String url = "https://api.reol.top/api_test";
        //使用原始回调
        QSHttp.post(url)
                .param("userName", 10086)
                .param("password", "qwe123456")
                .buildAndExecute(new HttpCallback() {
                    @Override
                    public void onSuccess(ResponseParams response) {
                        response.string();//响应内容
                    }

                    @Override
                    public void onFailure(HttpException e) {
                        e.show();
                    }
                });
```

### POST (application/json)
```
        String url = "https://api.reol.top/api_test";
        //不同类型请求只需改个方法名称即可实现
        QSHttp.postJSON(url)
                .param("userName", "song")
                .param("password", "123456")
                .buildAndExecute(new QSHttpCallback<Bean>() {
                    @Override
                    public void onComplete(Bean dataBean) {

                    }
                });
```


### Download
```
        //基于get下载
        String url = "https://api.reol.top/api_test";
        QSHttp.download(url,"/sdcard/xxx.txt")
                .buildAndExecute(new HttpCallbackProgress() {
                    @Override
                    public void onProgress(long var1, long var2, String var3) {
                        long i = var1 * 100 / var2;//下载百分比
                    }

                    @Override
                    public void onSuccess(ResponseParams response) {
                        response.file();//获取目录
                    }

                    @Override
                    public void onFailure(HttpException e) {
                        e.show();
                    }
                });
```


### Upload (multipart/form-data)
```
        String url = "https://api.reol.top/api_test";
        QSHttp.upload(url)
                //文本参数
                .param("userName", 10086)
                .param("password", "qwe123456")
                //文件参数
                .param("file", new File("xx.jpg"))
                .param("bytes", new byte[1024])//上传一个字节数组
                //指定上传的文件名,content-type参数
                .multipartBody("icon", "image/*", "icon.jpg", new File("xx.jpg"))
                .multipartBody(new String("icon"), "image/*", "icon2.jpg", new byte[1024])//icon文件数组上传
                .buildAndExecute(new HttpCallbackProgress() {
                    @Override
                    public void onProgress(long rwLen, long allLen, String mark) {
                        int progress=rwLen * 100 / allLen ;//进度百分比,mark=参数名
                    }

                    @Override
                    public void onSuccess(ResponseParams response) {
                        response.string();//获取响应内容
                    }

                    @Override
                    public void onFailure(HttpException e) {
                        e.show();
                    }
                });
```
###  实现一个自动弹出加载框,判断业务状态码的Callback
　在实际项目中,和后端交互JSON会有一套标准的格式,如`{"status":0,"msg":"OK","data":{}}`, 每个请求都判断太过于麻烦,而且也不方便统一处理,下面就实现一个用起来非常愉悦的回调:
```
public abstract class MyHttpCallback<T> extends QSHttpCallback<T> {

    protected boolean isShow = true;

    public MyHttpCallback() {
        super();
    }

    //在Activity/Fragment/view里调用QSHTTP可不传入context,会自动反射获取
    public MyHttpCallback(boolean isShow) {
        this.isShow = isShow;
    }

    public MyHttpCallback(Activity activity, boolean isShow) {
        super(activity);
        this.isShow = isShow;
    }

    @Override//这里开发者根据自己的情况修改键值即可
    public T map(String response) throws HttpException {
        JSONObject jsonObject = JSON.parseObject(response);
        //服务器状态码不对
        if (jsonObject.getIntValue("status") != 0) {
            throw HttpException.Custom(jsonObject.getString("msg"), jsonObject);
        }
        //这里可以继续加统一的处理代码,如登录失效
        
        return parserT(jsonObject.getString("data"));
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
```


###  高级配置
```
        //混淆
        -keep class * extends org.song.http.** { *; }

        //使用配置初始化,全局参数
        QSHttp.init(QSHttpConfig.Build(getApplication())
                //配置需要签名的网站, 读取assets/cers文件夹里的证书
                //支持双向认证,只需放入xxx.bks,见demo
                .ssl(Utils.getAssetsSocketFactory(this, "cers", "password")
                        , "192.168.1.168")//host参数:设置需要自签名的主机地址,不设置则只能访问证书列表里的https网站
                .hostnameVerifier(new TrustAllCerts.TrustAllHostnameVerifier())//证书信任规则(全信任)
                .cacheSize(128 * 1024 * 1024)
                .connectTimeout(18 * 1000)
                .debug(true)//打印日记
                //拦截器 全局添加头参数 鉴权
                .interceptor(interceptor)
                .build());

        //拦截器
        static Interceptor interceptor = new Interceptor() {
                @Override
                public ResponseParams intercept(Chain chain) throws HttpException {
                    RequestParams r = chain.request()
                            .newBuild()
                            .header("Interceptor", "Interceptor")//全局
                            //继续添加修改其他
                            .build();
                    return chain.proceed(r);//请求结果参数如有需要也可以进行修改
                }
            };
         
        //添加不同配置的client,满足不同需求
        QSHttp.addClient("test", QSHttpConfig.Build(getApplication())
                .xxHttp(HttpEnum.XX_Http.JAVA_HTTP)//使用原生底层实现
                .cacheSize(128 * 1024 * 1024)
                .connectTimeout(10 * 1000)
                .debug(true)
                .build());
        QSHttp.get("url").qsClient("test").buildAndExecute();//该请求将使用上述的配置
```


### 所有API一览

```
        String url = "https://api.reol.top/api_test";
        //同步请求
        Future<ResponseParams> future = QSHttp.get(url).param("future", "future").buildAndExecute();
        ResponseParams res = future.get();//get阻塞

        QSHttp.post(url)//选择请求的类型
                .header("User-Agent", "QsHttp/Android")//添加请求头

                .path(2333, "video")//构建成这样的url https://api.reol.top/api_test/2233/video

                .param("userName", 123456)//键值对参数
                .param("password", "asdfgh")//键值对参数
                .param(new Bean())//键值对参数
                .param("bytes", new byte[1024])//传一个字节数组,multipart支持此参数
                .param("file", new File("xx.jpg"))//传一个文件,multipart支持此参数

                .jsonBody(new Bean())//传入一个对象,会自动转化为json上传;application/json
                //自定义Body的内容 自定义contentType (postjson内部是调用这个实现)
                .requestBody("image/jpeg", new File("xx.jpg"))

                .parser(parser)//自定义解析,由自己写解析逻辑
                .resultByBytes()//请求结果返回一个字节组 默认是返回字符
                .resultByFile(".../1.txt")//本地路径 有此参数 请求的内容将被写入文件

                .errCache()//开启这个 [联网失败]会使用缓存,如果有的话
                .timeOut(10 * 1000)//单独设置超时
                .openServerCache()//开启服务器缓存规则 基于okhttp支持
                .clientCache(24 * 3600)//开启强制缓存,一天内都不会请求了
                .charset("utf-8")//特殊需求可以更改编码
                .tag("no token")//可配合拦截器使用
                //执行联网
                .buildAndExecute(new HttpCallbackEx() {

                    @Override
                    public void onStart() {
                        //开始请求
                    }

                    @Override
                    public void onSuccess(ResponseParams response) {
                        response.string();//获得响应字符串 *默认
                        response.file();//设置了下载 获得路径
                        response.bytes();//设置了返回字节组 获得字节组

                        response.headers();//获得响应头

                        //获得解析的模型
                        Bean b = response.jsonModel(Bean.class);
                    }

                    @Override
                    public void onFailure(HttpException e) {
                        e.show();//弹出错误提示 网络连接失败 超时 404 解析失败 ...等
                        String response = (String) e.getExObject();//可获取非200异常的参数
                    }

                    @Override
                    public void onEnd() {
                        //结束请求
                    }


                    @Override
                    public boolean isDestroy() {
                        return isFinishing();//页面关闭不会回调
                    }
                });
```
## Log
### v1.5.3(2020-11-27)
  * 支持同步
  * 优化监听回调,url兼容性
### v1.5.1(2020-07-03)
  * 支持多网络同时访问(在打开WIFI情况下访问蜂窝网络)
  * 优化
### v1.5.0(2019-12-19)
  * 支持多拦截器
  * 支持multipart数组上传
### v1.4.4(2019-09-06)
  * 非200状态码也会接受body数据
  * 优化
### v1.4.3(2019-07-18)
  * 优化(泛型)
### v1.4.2(2019-06-27)
  * 优化(进度监听,泛型)
### v1.4.1(2019-05-30)
  * 增加PATCH,OPTIONS
  * 优化泛型回调
### v1.3.3(2019-04-16)
  * 增加泛型回调,支持外部类是activity,fragment销毁时不回调
### v1.3.1(2019-04-04)
  * 可单独配置多个client
  * 双向ssl优化
### v1.3.0(2019-04-03)
  * 支持双向认证
  * 优化全局配置
  * 支持自定义编码
### v1.2.0(2019-03-27)
  * 船新版本,使用更愉悦
  * 支持自定义有效期缓存
## Other
  * 有问题请Add [issues](https://github.com/tohodog/QSHttp/issues)
  * 如果项目对你有帮助的话欢迎[![star][starsvg]][star]
  
[starsvg]: https://img.shields.io/github/stars/tohodog/QSHttp.svg?style=social&label=Stars
[star]: https://github.com/tohodog/QSHttp

[licensesvg]: https://img.shields.io/badge/License-Apache--2.0-red.svg
[license]: https://raw.githubusercontent.com/tohodog/QSHttp/master/LICENSE

[QSHttpsvg]: https://img.shields.io/badge/QSHttp-1.5.3-green.svg
[QSHttp]: https://github.com/tohodog/QSHttp
[fastjsonsvg]: https://img.shields.io/badge/fastjson-1.1.71-blue.svg
[fastjson]: https://github.com/alibaba/fastjson
[okhttpsvg]: https://img.shields.io/badge/okhttp3-3.14.7-orange.svg
[okhttp]: https://github.com/square/okhttp
