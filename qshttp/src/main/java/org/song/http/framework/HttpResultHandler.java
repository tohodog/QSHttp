package org.song.http.framework;

import com.alibaba.fastjson.JSON;

/**
 * Created by song on 2016/9/18.
 */
public class HttpResultHandler {

    protected final ResponseParams response;

    public HttpResultHandler(ResponseParams response) {
        this.response = response;
    }

    /**
     * 联网完成后进行数据处理
     */
    public void onComplete() {
        dealCache();
        dealParser();
        if (response.isSuccess())
            ThreadHandler.Success(response);
        else
            ThreadHandler.Failure(response);
    }

    private void dealCache() {
        HttpCache httpCache = HttpCache.instance();
        try {
            if (response.isSuccess())
                httpCache.checkAndSaveCache(response);
            else {
                boolean b = httpCache.checkAndGetErrCache(response);
                if (b) {
                    response.setSuccess(true);
                    response.setCacheYes();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dealParser() {
        if (response.isSuccess() && response.requestParams().parserMode() != HttpEnum.ParserMode.NOTHING)
            try {
                switch (response.resultType()) {
                    case STRING:
                        response.setParserObject(parser(response.string()));
                        break;
                    case BYTES:
                    case FILE:
                    default:
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.setException(HttpException.Parser(e));
                response.setSuccess(false);
            }
    }

    private Object parser(String result) throws Exception {
//        if (result == null)
//            throw new IllegalArgumentException("server response result is null");
        Object obj = null;
        HttpEnum.ParserMode type = response.requestParams().parserMode();
        switch (type) {
            case JSON:
                obj = JSON.parseObject(result, response.requestParams().get_class());
                break;
            case COSTOM:
                obj = response.requestParams().parser().parser(result);
                break;
            case XML:
                throw new IllegalArgumentException("Xml parsing is not supported yet");
            case NOTHING:
                break;
        }
        if (obj == null && type != HttpEnum.ParserMode.NOTHING)
            throw new IllegalArgumentException("The parser result is empty");
        return obj;
    }

}
