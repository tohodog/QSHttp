package org.song.http.framework;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;

/*
 * Created by song on 2016/9/18.
 * 子线程联网任务 回调主线程 类
 */
public class ThreadHandler extends Handler {

    private int mThreadWhat = 233;
    private SparseArray<HttpCallback> sparseArray;

    private ThreadHandler() {
        super(Looper.getMainLooper());
        sparseArray = new SparseArray<>();
    }

    private static ThreadHandler instance;

    private static ThreadHandler getInstance() {
        if (instance == null)
            instance = new ThreadHandler();
        return instance;
    }


    public synchronized int addHttpDataCallback(HttpCallback cb) {
        mThreadWhat++;
        sparseArray.put(mThreadWhat, cb);
        if (cb instanceof HttpCallbackEx)
            ((HttpCallbackEx) cb).onStart();
        return mThreadWhat;
    }

    @Override
    public void handleMessage(Message message) {
        final int what = message.what;
        HttpCallback cb = sparseArray.get(what);
        if (cb == null) {
            sparseArray.remove(what);
            return;
        }
        switch (message.arg1) {
            case HttpEnum.HTTP_SUCCESS:
                sparseArray.remove(what);
                if (cb instanceof HttpCallbackEx && ((HttpCallbackEx) cb).isDestroy())
                    break;
                cb.onSuccess((ResponseParams) message.obj);
                if (cb instanceof HttpCallbackEx)
                    ((HttpCallbackEx) cb).onEnd();
                break;
            case HttpEnum.HTTP_FAILURE:
                sparseArray.remove(what);
                if (cb instanceof HttpCallbackEx && ((HttpCallbackEx) cb).isDestroy())
                    break;
                cb.onFailure((HttpException) message.obj);
                if (cb instanceof HttpCallbackEx)
                    ((HttpCallbackEx) cb).onEnd();
                break;
            case HttpEnum.HTTP_PROGRESS:
                if (cb instanceof ProgressCallback) {
                    Object[] arr = (Object[]) message.obj;
                    ((ProgressCallback) cb).onProgress((long) arr[0], (long) arr[1], (String) arr[2]);
                }
                break;
        }

    }


    /**
     * 添加回调类
     *
     * @return 返回int 需保存 处理完网络数据回调主线程时带上
     */
    public static int AddHttpCallback(HttpCallback cb) {
        return getInstance().addHttpDataCallback(cb);
    }

    /**
     * 移除回调类
     */
    public static void removeHttpCallback(int mThreadWhat) {
        getInstance().sparseArray.remove(mThreadWhat);
    }

    /**
     * 成功的处理
     */
    public static void Success(ResponseParams obj) {
        Message msg = Message.obtain();
        msg.what = obj.requestID();
        msg.arg1 = HttpEnum.HTTP_SUCCESS;
        msg.obj = obj;
        getInstance().sendMessage(msg);
    }

    /**
     * 失败的处理
     */
    public static void Failure(ResponseParams obj) {
        Message msg = Message.obtain();
        msg.what = obj.requestID();
        msg.arg1 = HttpEnum.HTTP_FAILURE;
        Exception e = obj.exception();
        if (!(e instanceof HttpException))
            e = HttpException.Run(e);
        msg.obj = ((HttpException) e).responseParams(obj);
        getInstance().sendMessage(msg);
    }

    /**
     * 进度的处理
     * 这里应该加限制 太快貌似会崩溃
     * 或者不跳主线程 直接调用 唔...
     */
    public static void Progress(long var1, long var2, String var3, int mThreadWhat) {
        if (var2 == 0)
            var2 = -1;
        Message msg = Message.obtain();
        msg.what = mThreadWhat;
        msg.arg1 = HttpEnum.HTTP_PROGRESS;
        msg.obj = new Object[]{var1, var2, var3};
        getInstance().sendMessage(msg);

    }
}
