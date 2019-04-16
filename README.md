QSHttp
====
  * 一句代码联网,参数控制方便,自动json解析,使用简单
  * 支持http/自签名双向https(get post put head...) 文件上传、下载、进度监听、自动解析,基于Okhttp的支持cookie自动管理,缓存控制
  * 支持自定义有效时间缓存,错误缓存(联网失败时使用)
  * 详细的请求信息回调、错误类型(网络链接失败,超时,断网,解析失败,404...)
  * 详细的访问日记打印,非常方便调试
  * 提供拦截器,可添加一些公共鉴权参数...
  * 模块化设计,联网模块可更换,目前提供OkHttp和java原生两种实现

### Gradle
```
dependencies {
    implementation 'com.github.tohodog:QSHttp:1.3.3'
}
```

### 最简单的使用例子
```
QSHttp.get("http://xxx").buildAndExecute();
```
### HTTP调试地址
https://api.reol.top/api_test
<br/>
可接受任何请求,该接口返回用户请求信息


### 初始化框架
```
        //初始化框架 调用一次即可
        QSHttp.init(getApplication());


        //使用配置初始化
        QSHttp.init(QSHttpConfig.Build(getApplication())
                //配置需要签名的网站 读取assets/cers文件夹里的证书
                //支持双向认证 放入xxx.bks
                .ssl(Utils.getAssetsSocketFactory(this, "cers", "2923584")
                        , "192.168.1.168")//设置需要自签名的主机地址,不设置则只能访问sslSocketFactory里的https网站
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
```

### 普通带参数get请求
```
        String url = "https://api.reol.top/api_test";
        QSHttp.get(url)
                .param("wd", "安卓http")
                .param("ie", "UTF-8")//自动构建url--https://api.reol.top/api_test?ie=UTF-8&wd=安卓http
                //.path(123,11) 这个参数会构建这样的url--https://api.reol.top/api_test/123/11
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


### 普通键值对post请求(application/x-www-form-urlencoded)
```
        String url = "https://api.reol.top/api_test";
        QSHttp.post(url)
                .param("userid", 10086)
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

###  post一个json,并自动解析返回信息(application/json)
```
        String url = "https://api.reol.top/api_test";
        QSHttp.postJSON(url)
                .param("userid", 10086)
                .param("password", "qwe123456")
                //.jsonBody(Object) 这个参数可以直接传一个实体类,fastjson会自动转化成json字符串
                .jsonModel(Bean.class)
                .buildAndExecute(new HttpCallback() {
                    @Override
                    public void onSuccess(ResponseParams response) {
                        response.string();//响应内容
                        Bean b = response.parserObject();//自动解析好的实体类
                        b.getUserid();
                    }

                    @Override
                    public void onFailure(HttpException e) {
                        e.show();
                    }
                });
        //使用QSHttpCallback,支持外部类是activity,fragment时销毁不回调
        QSHttp.postJSON(url)
                .param("userid", 10086)
                .param("password", "qwe123456")
                .buildAndExecute(new QSHttpCallback<Bean>() {
                    @Override
                    public void onComplete(Bean dataBean) {

                    }
                });
```


###  文件下载
```
        String url = "https://api.reol.top/api_test";
        QSHttp.download(url,"/xxx/xxx.txt")
                .buildAndExecute(new ProgressCallback() {
                    @Override
                    public void onProgress(long var1, long var2, String var3) {
                        Log.i("http",var1 * 100 / var2 + "%\n");
                    }

                    @Override
                    public void onSuccess(ResponseParams response) {
                        response.headers().toString();//获取响应求头
                        response.bytes();
                    }

                    @Override
                    public void onFailure(HttpException e) {
                        e.show();
                    }
                });
```


###  文件上传(multipart/form-data)
```
        String url = "https://api.reol.top/api_test";
        QSHttp.upload(url)
                .param("userid", 10086)
                .param("password", "qwe123456")

                .param("bytes", new byte[1024])//multipart方式上传一个字节数组
                .param("file", new File("xx.jpg"))//multipart方式上传一个文件
                .multipartBody("icon", "image/*", "x.jpg", new byte[1024])

                .buildAndExecute(new ProgressCallback() {
                    @Override
                    public void onProgress(long rwLen, long allLen, String mark) {
                        int i=rwLen * 100 / allLen ;//百分比
                        //mark 在传文件的时候为文件路径 其他无意义
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

###  配置多个client
```
        QSHttp.addClient("server2", QSHttpConfig.Build(getApplication())
                .hostnameVerifier(new TrustAllCerts.TrustAllHostnameVerifier())//证书信任规则
                .cacheSize(128 * 1024 * 1024)
                .connectTimeout(10 * 1000)
                .debug(true)
                .build());
        QSHttp.get("url").qsClient("server2").buildAndExecute();
```


### 基本所有API一览

```
        String url = "https://api.reol.top/api_test";
                QSHttp.post(url)//选择请求的类型
                        .header("User-Agent", "QsHttp/Android")//添加请求头

                        .path(2333, "video")//构建成这样的url https://api.reol.top/api_test/2233/video

                        .param("userid", 123456)//键值对参数
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
