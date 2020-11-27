package com.song.demo;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import org.song.http.QSHttp;
import org.song.http.framework.QSHttpConfig;
import org.song.http.framework.util.TrustAllCerts;

import java.io.IOException;

import static android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET;
import static android.net.NetworkCapabilities.TRANSPORT_CELLULAR;


/**
 * 网络变化监听+获取
 */
public class NetManager extends BroadcastReceiver {

    private static NetworkInfo nowNetworkInfo;
    private static ConnectivityManager connectivityManager;

    static {
        connectivityManager = (ConnectivityManager) AppContext.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        nowNetworkInfo = connectivityManager.getActiveNetworkInfo();
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        ConnectivityManager connectivityManager = (ConnectivityManager) AppContext.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null)
            Toast.makeText(context, "网络断开,请检查您的网络设置", Toast.LENGTH_LONG).show();
        else if (nowNetworkInfo != null && networkInfo.getType() != nowNetworkInfo.getType())//避免重复弹
            Toast.makeText(context, "当前网络为:" +
                    (networkInfo.getType() == 0 ? "移动网络" :
                            networkInfo.getType() == 1 ? "WIFI网络" : "其他网络"), Toast.LENGTH_LONG).show();
        nowNetworkInfo = networkInfo;
    }

    public static boolean isWIFI() {
        return nowNetworkInfo != null && nowNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public static boolean isGPRS() {
        return nowNetworkInfo != null && nowNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    public static boolean isNull() {
        return nowNetworkInfo == null || !nowNetworkInfo.isAvailable();
    }

    private static Network mNetwork;

    //选择4G网络
    public static void select4GNetwork() {
        if (Build.VERSION.SDK_INT >= 21) {
            NetworkRequest.Builder builder = new NetworkRequest.Builder();
            builder.addCapability(NET_CAPABILITY_INTERNET);
            //强制使用蜂窝数据网络-移动数据
            builder.addTransportType(TRANSPORT_CELLULAR);
            NetworkRequest request = builder.build();

            ConnectivityManager.NetworkCallback callback = new ConnectivityManager.NetworkCallback() {
                @TargetApi(Build.VERSION_CODES.M)
                @Override
                public void onAvailable(Network network) {
                    super.onAvailable(network);
                    mNetwork = network;
                    Log.i("onAvailable", "网络:" + network);

                    // 可以通过下面代码将app接下来的请求都绑定到这个网络下请求
//                    if (Build.VERSION.SDK_INT >= 23) {
//                        connectivityManager.bindProcessToNetwork(network);
//                    } else {// 23后这个方法舍弃了
//                        ConnectivityManager.setProcessDefaultNetwork(network);
//                    }
                    //配置多个client
                    QSHttp.addClient("CELLULAR", QSHttpConfig.Build(AppContext.getInstance())
                            .hostnameVerifier(new TrustAllCerts.TrustAllHostnameVerifier())//证书信任规则
                            .cacheSize(128 * 1024 * 1024)
                            .connectTimeout(18 * 1000)
                            .socketFactory(network.getSocketFactory())
                            .network(network)
//                            .xxHttp(HttpEnum.XX_Http.JAVA_HTTP)
                            .debug(true)
                            //拦截器 添加头参数 鉴权
                            .interceptor(new QSInterceptor())
                            .build());

                }
            };
            connectivityManager.registerNetworkCallback(request, callback);

            QSHttp.get("http://baidu.com?").buildAndExecute();//对照用 使用默认通道
            QSHttp.get("http://baidu.com").qsClient("CELLULAR").buildAndExecute();//将使用4G通道

        }
    }

    public static void unSelectNetwork() {
        if (Build.VERSION.SDK_INT >= 23) {
            connectivityManager.bindProcessToNetwork(null);
        } else {// 23后这个方法舍弃了
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ConnectivityManager.setProcessDefaultNetwork(null);
            }
        }
    }

    /*
     * WIFI是否连接
     * */
    private boolean isNetworkConnected() {
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /*
     * 外网是否可以访问
     * */
    public boolean isNetworkOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("ping -c 1 www.baidu.com");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

}
