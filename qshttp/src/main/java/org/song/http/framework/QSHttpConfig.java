package org.song.http.framework;

import android.app.Application;
import android.net.Network;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by song on 2019/4/2.
 * http框架全局配置
 */
public class QSHttpConfig {

    private boolean debug;

    private Application application;
    private HttpEnum.XX_Http xxHttp;
    private String cachePath;
    private List<Interceptor> interceptorList;

    private SSLSocketFactory sslSocketFactory;
    private String[] sslHost;
    private HostnameVerifier hostnameVerifier;
    private SocketFactory socketFactory;
    private Network network;

    private int poolSize;
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

    public SSLSocketFactory sslSocketFactory() {
        return sslSocketFactory;
    }

    public HttpEnum.XX_Http xxHttp() {
        return xxHttp;
    }

    public List<Interceptor> interceptorList() {
        return interceptorList;
    }

    public String[] sslHost() {
        return sslHost;
    }

    public HostnameVerifier hostnameVerifier() {
        return hostnameVerifier;
    }

    public SocketFactory socketFactory() {
        return socketFactory;
    }

    public Network network() {
        return network;
    }

    public int cacheSize() {
        return cacheSize;
    }

    public String cachePath() {
        return cachePath;
    }

    public int poolSize() {
        return poolSize;
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

    private QSHttpConfig() {

    }

    public static Builder Build(Application application) {
        return new Builder(application);
    }

    public static final class Builder {

        private boolean debug = true;

        private Application application;
        private HttpEnum.XX_Http xxHttp = HttpEnum.XX_Http.OK_HTTP;//
        private String cachePath;
        private List<Interceptor> interceptorList;

        private SSLSocketFactory sslSocketFactory;
        private String[] sslHost;
        private HostnameVerifier hostnameVerifier;
        private SocketFactory socketFactory;
        private Network network;

        private int poolSize = 8;
        private int cacheSize = 128 * 1024 * 1024;
        private int connectTimeout = 12_000;
        private int readTimeout = 12_000;
        private int writeTimeout = 12_000;
        private int progressCallbackSpace = 100;//进度回调的频率 毫秒

        private Builder(Application application) {
            this.application = application;
        }

        public QSHttpConfig build() {
            QSHttpConfig qsHttpConfig = new QSHttpConfig();
            qsHttpConfig.debug = debug;
            qsHttpConfig.interceptorList = interceptorList;
            qsHttpConfig.application = application;
            qsHttpConfig.xxHttp = xxHttp;
            qsHttpConfig.poolSize = poolSize;
            qsHttpConfig.sslSocketFactory = sslSocketFactory;
            qsHttpConfig.socketFactory = socketFactory;
            qsHttpConfig.network = network;
            qsHttpConfig.sslHost = sslHost;
            qsHttpConfig.hostnameVerifier = hostnameVerifier;
            qsHttpConfig.cacheSize = cacheSize;
            qsHttpConfig.cachePath = cachePath;
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

        public Builder interceptor(Interceptor interceptor) {
            if (this.interceptorList == null) this.interceptorList = new ArrayList<>();
            this.interceptorList.add(interceptor);
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

        /**
         * 配置网络通道,比如WiFi下访问4G网络
         *
         * @param socketFactory 设置socketFactory
         */
        public Builder socketFactory(SocketFactory socketFactory) {
            this.socketFactory = socketFactory;
            return this;
        }

        /**
         * 配置网络通道,比如WiFi下访问4G网络
         *
         * @param network 设置socketFactory
         */
        public Builder network(Network network) {
            this.network = network;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                socketFactory(network.getSocketFactory());
            }
            return this;
        }

        public Builder hostnameVerifier(HostnameVerifier hostnameVerifier) {
            this.hostnameVerifier = hostnameVerifier;
            return this;
        }

        public Builder poolSize(int poolSize) {
            if (poolSize > 0)
                this.poolSize = poolSize;
            return this;
        }

        public Builder cacheSize(int cacheSize) {
            this.cacheSize = cacheSize;
            return this;
        }

//        public Builder cachePath(String cachePath) {
//            this.cachePath = cachePath;
//            return this;
//        }

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
