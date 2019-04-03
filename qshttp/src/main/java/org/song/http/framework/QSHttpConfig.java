package org.song.http.framework;

import android.app.Application;

import org.song.http.framework.java.JavaHttpClient;
import org.song.http.framework.ok.OkHttpClient;

import javax.net.ssl.SSLSocketFactory;

/**
 * Created by song on 2019/4/2.
 * http框架全局配置
 */
public class QSHttpConfig {

    private boolean debug;

    private Application application;
    private HttpEnum.XX_Http xxHttp;

    private SSLSocketFactory sslSocketFactory;
    private String[] sslHost;

    private int cacheSize;
    private int connectTimeout;
    private int readTimeout;
    private int writeTimeout;
    private int progressCallbackSpace;

    public boolean debug() {
        return debug;
    }

    public Application application() {
        return application;
    }

    public HttpEnum.XX_Http xxHttp() {
        return xxHttp;
    }

    public SSLSocketFactory sslSocketFactory() {
        return sslSocketFactory;
    }

    public String[] sslHost() {
        return sslHost;
    }

    public int cacheSize() {
        return cacheSize;
    }

    public int connectTimeout() {
        return connectTimeout;
    }

    public int readTimeout() {
        return readTimeout;
    }

    public int writeTimeout() {
        return writeTimeout;
    }

    public int progressCallbackSpace() {
        return progressCallbackSpace;
    }

    public static Builder Build(Application application) {
        return new Builder(application);
    }

    public static final class Builder {

        private boolean debug = true;

        private Application application;
        private HttpEnum.XX_Http xxHttp = HttpEnum.XX_Http.OK_HTTP;//

        private SSLSocketFactory sslSocketFactory;
        private String[] sslHost;

        private int cacheSize = 128 * 1024 * 1024;
        private int connectTimeout = 12_000;
        private int readTimeout = 12_000;
        private int writeTimeout = 12_000;
        private int progressCallbackSpace = 500;//进度回调的频率 毫秒

        private Builder(Application application) {
            this.application = application;
        }

        public QSHttpConfig build() {
            QSHttpConfig qsHttpConfig = new QSHttpConfig();
            qsHttpConfig.debug = debug;
            qsHttpConfig.application = application;
            qsHttpConfig.xxHttp = xxHttp;
            qsHttpConfig.sslSocketFactory = sslSocketFactory;
            qsHttpConfig.sslHost = sslHost;
            qsHttpConfig.cacheSize = cacheSize;
            qsHttpConfig.connectTimeout = connectTimeout;
            qsHttpConfig.readTimeout = readTimeout;
            qsHttpConfig.writeTimeout = writeTimeout;
            qsHttpConfig.progressCallbackSpace = progressCallbackSpace;
            return qsHttpConfig;
        }

        public Builder debug(boolean debug) {
            this.debug = debug;
            return this;
        }

        public Builder application(Application application) {
            this.application = application;
            return this;
        }

        public Builder xxHttp(HttpEnum.XX_Http xxHttp) {
            this.xxHttp = xxHttp;
            return this;
        }

        /**
         * 配置自签名ssl
         *
         * @param sslSocketFactory Utils里有提供方法使用
         * @param sslHost          设置需要自签名的主机地址,不设置则只能访问信任证书里的https网站
         */
        public Builder ssl(SSLSocketFactory sslSocketFactory, String... sslHost) {
            this.sslSocketFactory = sslSocketFactory;
            this.sslHost = sslHost;
            return this;
        }

        public Builder cacheSize(int cacheSize) {
            this.cacheSize = cacheSize;
            return this;
        }

        public Builder connectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder readTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder writeTimeout(int writeTimeout) {
            this.writeTimeout = writeTimeout;
            return this;
        }

        public Builder progressCallbackSpace(int progressCallbackSpace) {
            this.progressCallbackSpace = progressCallbackSpace;
            return this;
        }
    }
}
