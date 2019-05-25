package org.song.http.framework;


import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * Created by song on 2016/9/18.
 * 中枢类 开线程 联网 解析 缓存 进度、结果回调主线程
 * 需要子类提供具体联网实现
 * 联网模块可更换
 */
public class QSHttpClient {

    private IHttpTask iHttpTask;

    private QSHttpConfig qsHttpConfig;

    private ExecutorService executorService;

    private Interceptor interceptor;

    public QSHttpClient(IHttpTask iHttpTask, QSHttpConfig qsHttpConfig) {
        this.iHttpTask = iHttpTask;
        this.qsHttpConfig = qsHttpConfig;
        interceptor = qsHttpConfig.interceptor();
        executorService = Executors.newFixedThreadPool(qsHttpConfig.poolSize());
    }

    /**
     * 具体联网
     *
     * @param cb      响应回调类 非final不带进内部类
     * @param request 请求参数类
     * @return 返回标记id 标记此次请求 返回会带上
     */
    public int execute(final RequestParams request, HttpCallback cb) {
        final int mThreadWhat = ThreadHandler.AddHttpCallback(cb);
        final boolean isProgress = cb instanceof ProgressCallback;

        executorService.submit(new Runnable() {

            RequestParams _request = request;

            @Override
            public void run() {
                ResponseParams response;
                final HttpProgress hp = isProgress ? new HttpProgress(mThreadWhat) : null;
                try {
                    if (interceptor != null) {//拦截器
                        response = interceptor.intercept(new Interceptor.Chain() {
                            @Override
                            public RequestParams request() {
                                return _request;
                            }

                            @Override
                            public ResponseParams proceed(RequestParams request) throws HttpException {
                                _request = request;
                                return access(request, hp);
                            }
                        });
                        if (response == null) {
                            throw HttpException.Custom("interceptor return is null " + interceptor);
                        }
                    } else
                        response = access(_request, hp);
                    response.setSuccess(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    response = new ResponseParams();
                    response.setException(e);
                    response.setSuccess(false);
                }
                response.setRequestID(mThreadWhat);
                response.setResultType(_request.resultType());
                response.setRequestParams(_request);

                try {
                    if (hp != null)
                        hp.destroy();
                    if (qsHttpConfig.debug())
                        Utils.Log(_request, response);
                } finally {
                    //返回数据后续处理 如缓存、解析等
                    new HttpResultHandler(response).onComplete();
                }
            }
        });
        return mThreadWhat;
    }

    //具体联网逻辑 保证返回ResponseParams对象不为null
    private ResponseParams access(final RequestParams request, final HttpProgress hp) throws HttpException {
        ResponseParams response = null;

        try {
            response = HttpCache.instance().checkAndGetCache(request);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (response != null) {
            response.setCacheYes();
        } else {
            switch (request.requestMethod()) {
                case GET:
                    response = iHttpTask.GET(request, hp);
                    break;
                case POST:
                case PUT:
                case PATCH:
                    if (request.multipartBody() != null)
                        response = iHttpTask.P_MULTIPART(request, hp);
                    else if (request.requestBody() != null)
                        response = iHttpTask.P_BODY(request, hp);
                    else
                        response = iHttpTask.P_FORM(request, hp);
                    break;
                case HEAD:
                    response = iHttpTask.HEAD(request);
                    break;
                case DELETE:
                    response = iHttpTask.DELETE(request);
                    break;
                case OPTIONS:
                    response = iHttpTask.OPTIONS(request);
                    break;
            }
        }
        return response;
    }

    private class HttpProgress implements IHttpProgress {
        private final int mThreadWhat;
        private long var1, var2 = -1;
        private String var3;
        private Timer timer = new Timer();

        HttpProgress(final int mThreadWhat) {
            this.mThreadWhat = mThreadWhat;
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    ThreadHandler.Progress(var1, var2, var3, mThreadWhat);
                }
            }, 0, qsHttpConfig.progressCallbackSpace());
        }

        void destroy() {
            timer.cancel();
            ThreadHandler.Progress(var1, var2, var3, mThreadWhat);
        }

        //这里不执行耗时操作 会阻塞下载速度
        @Override
        public void onProgress(long var1, long var2, String var3) {
            this.var1 = var1;
            this.var2 = var2;
            this.var3 = var3;
            if (var1 == var2)
                ThreadHandler.Progress(var1, var2, var3, mThreadWhat);
            //Log.e("HTTP_PROGRESS", var1 + "/" + var2 + " " + var1 * 100 / var2 + "%");
        }

    }

}
