QSHttp
====
One Code Man! GET,POST,表单,JSON,上传,下载等等统统同一行代码搞定!
<br>
  * 5年实战环境验证迭代,稳定可靠
  * 强大灵活的入参,支持泛型回调,使用简单
  * 简单实现自动弹加载框,判断业务状态码,弹错误提示
  * 支持异步(回调已在主线程),同步请求
  * 支持自签名,双向https
  * 支持自定义有效时间缓存,错误缓存(联网失败时使用),缓存控制,cookie自动管理
  * 详细的请求信息回调、错误类型(网络链接失败,超时,断网,解析失败,404...)
  * 详细的访问日记打印,非常方便调试
  * 支持多拦截器,可添加一些公共鉴权参数...

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
    implementation 'com.github.tohodog:QSHttp:1.5.2'
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
        //初始化框架,调用一次即可
        QSHttp.init(getApplication());

```

### GET
```
        String url = "https://api.reol.top/api_test";
        //使用泛型回调,自动解析json数据
        QSHttp.get(url)
                .param("name", "QSHttp")
                .buildAndExecute(new QSHttpCallback<User>() {
                    @Override
                    public void onComplete(User bean) {
                        //请求解析成功,执行业务逻辑
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
                .buildAndExecute(new QSHttpCallback<UserBean>() {
                    @Override
                    public void onComplete(UserBean dataUser) {

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


###  高级配置
```
        //混淆
        -keep class * extends org.song.http.framework.QSHttpCallback { *; }

        //使用配置初始化
        QSHttp.init(QSHttpConfig.Build(getApplication())
                //配置需要签名的网站 读取assets/cers文件夹里的证书
                //支持双向认证 放入xxx.bks
                .ssl(Utils.getAssetsSocketFactory(this, "cers", "password")
                        , "192.168.1.168")//地址参数:设置需要自签名的主机地址,不设置则只能访问证书列表里的https网站
                .hostnameVerifier(new TrustAllCerts.TrustAllHostnameVerifier())//证书信任规则(全信任)
                .cacheSize(128 * 1024 * 1024)
                .connectTimeout(18 * 1000)
                .debug(true)
                //拦截器 添加头参数 鉴权
                .interceptor(interceptor)
                .build());

        //拦截器
        //TODO 拦截器需放到在 Application/静态变量/非内部类 里,否则外部类将会内存泄露
        static Interceptor interceptor = new Interceptor() {
                @Override
                public ResponseParams intercept(Chain chain) throws HttpException {
                    RequestParams r = chain.request()
                            .newBuild()
                            .header("Interceptor", "Interceptor")
                            //继续添加修改其他
                            .build();
                    return chain.proceed(r);//请求结果参数如有需要也可以进行修改
                }
            };
         
        //配置多个client,这里实现在wifi下使用蜂窝网络
        QSHttp.addClient("CELLULAR", QSHttpConfig.Build(getApplication())
//                .network(network)//配置蜂窝网络通道
                .cacheSize(128 * 1024 * 1024)
                .connectTimeout(10 * 1000)
                .debug(true)
                .build());
        QSHttp.get("url").qsClient("CELLULAR").buildAndExecute();//该请求将使用上述的配置,走蜂窝网路
```


### 所有API一览

```
        String url = "https://api.reol.top/api_test";
                QSHttp.post(url)//选择请求的类型
                        .header("User-Agent", "QsHttp/Android")//添加请求头

                        .path(2333, "video")//构建成这样的url https://api.reol.top/api_test/2233/video

                        .param("userName", 123456)//键值对参数
                        .param("password", "asdfgh")//键值对参数
                        .param(new Bean())//键值对参数

                        .toJsonBody()//把 params 转为json;application/json
                        .jsonBody(new Bean())//传入一个对象,会自动转化为json上传;application/json

                        .requestBody("image/jpeg", new File("xx.jpg"))//直接上传自定义的内容 自定义contentType (postjson内部是调用这个实现)

                        .param("bytes", new byte[1024])//传一个字节数组,multipart支持此参数
                        .param("file", new File("xx.jpg"))//传一个文件,multipart支持此参数
                        .toMultiBody()//把 params 转为multipartBody参数;multipart/form-data


                        .parser(parser)//自定义解析,由自己写解析逻辑
                        .jsonModel(Bean.class)//使用FastJson自动解析json,传一个实体类即可

                        .resultByBytes()//请求结果返回一个字节组 默认是返回字符
                        .resultByFile(".../1.txt")//本地路径 有此参数 请求的内容将被写入文件

                        .errCache()//开启这个 [联网失败]会使用缓存,如果有的话
                        .clientCache(24 * 3600)//开启缓存,有效时间一天
                        .timeOut(10 * 1000)
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
```
## Log
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
