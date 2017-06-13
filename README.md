#QSHttp
  * 一句代码联网,参数控制方便,自动json解析,使用简单(介绍也很简单(X
  * 模块化设计,联网模块可更换,目前提供OkHttp和java原生两种实现
  * 支持http/自签名https(get post put head...) 文件上传、下载、进度监听、自动解析,基于Okhttp的支持cookie自动管理,缓存控制
  * 详细的请求信息回调、错误类型(网络链接失败,超时,断网,解析失败,404...)
  * 详细的访问日记打印,满足一般调试
  * 提供精简包仅有50+K(基于原生实现,移除okhttp,fastjson)
  * 提供拦截器,可添加一些鉴权参数...

### 使用方法
1.完整版
[qshttp-1.1.1.jar](https://raw.githubusercontent.com/tohodog/QSHttp/master/libs/qshttp-1.1.1.jar) 
[okhttp-3.2.0.jar](https://raw.githubusercontent.com/tohodog/QSHttp/master/libs/okhttp-3.2.0.jar) 
[okio-1.7.0.jar](https://raw.githubusercontent.com/tohodog/QSHttp/master/libs/okio-1.7.0.jar) 
[fastjson-1.1.45.jar](https://raw.githubusercontent.com/tohodog/QSHttp/master/libs/fastjson-1.1.45.jar)

2.精简版
[qshttp-simple-1.1.1..jar](https://raw.githubusercontent.com/tohodog/QSHttp/master/libs/qshttp-simple-1.1.1.jar)  
3.下载qshttp文件夹 添加为自己的项目依赖即可
4.gradle......
### 最简单的使用例子
```
QSHttp.get("http://xxx").buildAndExecute();
```


### 普通带参数get请求
```
        String url = "https://www.baidu.com/s";
        QSHttp.get(url)
                .param("wd", "安卓http")
                .param("ie", "UTF-8")//自动构建url--https://www.baidu.com/s?ie=UTF-8&wd=安卓http
                //.restful(123,11) 这个参数会构建这样的url--https://www.baidu.com/s/123/11
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


### 普通键值对post请求
```
        String url = "https://www.baidu.com";
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

###  post一个json给服务器 并自动解析服务器返回信息
```
        String url = "https://www.baidu.com";
        QSHttp.postCustom(url)
                .param("userid", 10086)
                .param("password", "qwe123456")
                //.postJson(Object) 这个参数可以直接传一个实体类,fastjson会自动转化成json字符串
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
```


###  文件下载
```
        String url = "https://www.baidu.com";
        QSHttp.download(url,".../xxx.txt")
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


###  文件上传
```
        String url = "https://www.baidu.com";
        QSHttp.upload(url)
                .param("userid", 10086)
                .param("password", "qwe123456")

                .uploadByte("bytes", new byte[1024])//multipart方式上传一个字节数组
                .uploadFile("file", new File("xx.jpg"))//multipart方式上传一个文件

                .buildAndExecute(new ProgressCallback() {
                    @Override
                    public void onProgress(long var1, long var2, String var3) {
                        int i=var1 * 100 / var2 ;//百分比
                        //var3 在传文件的时候为文件路径 其他无意义
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



### 基本所有API一览

``` 
        //使用前进行初始化 在Application初始化即可
        HttpConfing.init(getApplication());

        String url = "https://www.baidu.com/s";
        QSHttp.post(url)//选择请求的类型
                .param("userid", 123456)//键值对参数,get post postJson upload(multipart)支持此参数
                .restful("video", 2333)//构建成这样的url https://www.baidu.com/s/video/233
                .postJson(new Bean())//传入一个对象 会自动转化为json上传
                .header("User-Agent", "QsHttp/Android")//添加请求头
                .uploadByte("bytes", new byte[1024])//multipart方式上传一个字节数组
                .uploadFile("file", new File("xx.jpg"))//multipart方式上传一个文件
                
                .parser(parser)//自定义解析,由自己写解析逻辑
                .jsonModel(Bean.class)//使用FastJson自动解析json,传一个实体类即可
                
                .resultByBytes()//请求结果返回一个字节组 默认是返回字符
                .resultByFile(".../1.txt")//本地路径 有此参数 请求的内容将被写入文件

                .customContent("image/jpeg", new File("xx.jpg"))//上传自定义的内容(file string byte[]) 
                                                                  自定义contentType(postjson内部是调用这个实现)
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
```
