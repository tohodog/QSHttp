package org.song.http.framework;

import com.alibaba.fastjson.JSON;

import org.song.http.framework.util.HttpCache;

/**
 * Created by song on 2016/9/18.
 */
public class HttpResultHandler {


    /**
     * 联网完成后进行数据处理
     */
    public static void onComplete(ResponseParams response, boolean isSync) {
        if (response.isSuccess())
            ThreadHandler.Success(response, isSync);
        else
            ThreadHandler.Failure(response, isSync);
    }

    public static void dealCache(ResponseParams response) {
        HttpCache httpCache = HttpCache.instance();
        try {
            if (response.isSuccess()) {
                httpCache.checkAndSaveCache(response);
            } else {
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

    public static void dealParser(ResponseParams response) {
        if (response.isSuccess() && response.requestParams().parserMode() != HttpEnum.ParserMode.NOTHING)
            try {
                switch (response.resultType()) {
                    case STRING:
                        response.setParserObject(parser(response));
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

    private static Object parser(ResponseParams response) throws Exception {
        String result = response.string();
        if (result == null || result.isEmpty())
            throw new IllegalArgumentException("server response result is null");
        Object obj = null;
        HttpEnum.ParserMode type = response.requestParams().parserMode();
        switch (type) {
            case JSON:
                if (result.charAt(0) == '{')
                    obj = JSON.parseObject(result, response.requestParams().get_class());
                else if (result.charAt(0) == '[')
                    obj = JSON.parseArray(result, response.requestParams().get_class());
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
