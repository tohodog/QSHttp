package org.song.http.framework;


import org.song.http.framework.ability.HttpCallback;
import org.song.http.framework.ability.HttpFutureCallback;
import org.song.http.framework.ability.IHttpProgress;
import org.song.http.framework.ability.IHttpTask;
import org.song.http.framework.ability.Interceptor;
import org.song.http.framework.util.HttpCache;
import org.song.http.framework.util.Utils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * Created by song on 2016/9/18.
 * 中枢类 开线程 联网 解析 缓存 进度、结果回调主线程
 * 需要提供IHttpTask具体联网实现
 * 联网模块可更换
 */
public class QSHttpClient {

    private IHttpTask iHttpTask;

    private QSHttpConfig qsHttpConfig;

    private ExecutorService executorService;

    private List<Interceptor> interceptorList;

    private ThreadLocal<RequestParams> threadLocal = new ThreadLocal<>();//经过拦截器,request被改变了,这个保存最新的

    public QSHttpClient(IHttpTask iHttpTask, QSHttpConfig qsHttpConfig) {
        this.iHttpTask = iHttpTask;
        this.qsHttpConfig = qsHttpConfig;
        interceptorList = qsHttpConfig.interceptorList();
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
        final boolean isProgress = cb instanceof IHttpProgress;
        final boolean isSync = cb instanceof HttpFutureCallback;

        executorService.submit(new Runnable() {


            @Override
            public void run() {
                threadLocal.set(request);
                long time = System.currentTimeMillis();
                ResponseParams response = null;
                final HttpProgress hp = isProgress ? new HttpProgress(mThreadWhat) : null;
                try {
                    //拦截器
                    response = runInterceptor(0, threadLocal, hp);
                    response.setSuccess(true);
                } catch (Throwable e) {
                    e.printStackTrace();
                    HttpException httpException;
                    if (e instanceof HttpException) {
                        httpException = (HttpException) e;
                        response = ((HttpException) e).responseParams();
                    } else {
                        httpException = HttpException.Run(e);
                    }
                    if (response == null)
                        response = new ResponseParams();
                    response.setException(httpException);
                    response.setSuccess(false);
                }
                //获取最新请求参数,拦截器里可能修改了
                RequestParams _request = threadLocal.get();
                assert _request != null;
                response.setRequestID(mThreadWhat);
                response.setResultType(_request.resultType());
                response.setRequestParams(_request);

                try {
                    if (hp != null)
                        hp.destroy();
                    if (qsHttpConfig.debug())
                        Utils.Log(_request, response, System.currentTimeMillis() - time);

                    //返回数据后续处理 如缓存、解析等
                    HttpResultHandler.dealCache(response);
                    HttpResultHandler.dealParser(response);

                } finally {
                    HttpResultHandler.onComplete(response, isSync);
                }
            }
        });
        return mThreadWhat;
    }

    //递归调用拦截器列表
    private ResponseParams runInterceptor(final int index, final ThreadLocal<RequestParams> threadLocal, final HttpProgress hp) throws HttpException {
        if (interceptorList == null || index >= interceptorList.size()) {//递归终点
            return access(threadLocal.get(), hp);
        }

        Interceptor interceptor = interceptorList.get(index);
        return interceptor.intercept(new Interceptor.Chain() {
            @Override
            public RequestParams request() {
                return threadLocal.get();
            }

            @Override
            public ResponseParams proceed(RequestParams request) throws HttpException {
                threadLocal.set(request);
                return runInterceptor(index + 1, threadLocal, hp);//递归调用
            }
        });
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
        if (response != null)
            response.setRequestParams(request);
        return response;
    }

    private class HttpProgress implements IHttpProgress {
        private final int mThreadWhat;
        private long var1, var2 = -1;
        private String var3 = "";
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

    public IHttpTask getHttpTask() {
        return iHttpTask;
    }

    public QSHttpConfig getQsHttpConfig() {
        return qsHttpConfig;
    }
}
