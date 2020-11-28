package org.song.http.framework.ability;

import org.song.http.framework.HttpException;
import org.song.http.framework.ResponseParams;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by QSong
 * Contact github.com/tohodog
 * Date 2020/11/26
 * 同步
 */
public class HttpFutureCallback implements HttpCallback, Future<ResponseParams> {

    private final CountDownLatch latch = new CountDownLatch(1);

    private ResponseParams result = null;
    private Throwable error = null;

    @Override
    public void onSuccess(ResponseParams response) {
        this.result = response;
        latch.countDown();
    }

    @Override
    public void onFailure(HttpException e) {
        this.error = e;
        latch.countDown();
    }


    @Override
    public ResponseParams get() throws ExecutionException, InterruptedException {
        latch.await();
        if (error != null) {
            throw new ExecutionException(error);
        }
        return result;
    }

    @Override
    public ResponseParams get(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
        if (latch.await(timeout, unit)) {
            if (error != null) {
                throw new ExecutionException(error);
            }
            return result;
        } else {
            throw new TimeoutException("HttpFutureCallback get time out " + unit.toMillis(timeout));
        }
    }


    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return latch.getCount() == 0;
    }
}
