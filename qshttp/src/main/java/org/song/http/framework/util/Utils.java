package org.song.http.framework.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.song.http.framework.HttpEnum;
import org.song.http.framework.QSHttpConfig;
import org.song.http.framework.QSHttpManage;
import org.song.http.framework.RequestParams;
import org.song.http.framework.ResponseParams;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by song on 2016/11/2.
 */

public class Utils {

    public static final String TAG = "QSHTTP";
    public static boolean FORMAT_JSON = false;
    public static boolean LOG_SMIPLE = false;

    /**
     * 读取assets/path文件夹里的证书
     */
    public static SSLSocketFactory getAssetsSocketFactory(Context context, String path, String bks_storepass) {
        if (path == null)
            return null;

        //拿出证书流
        InputStream bksIS = null;
        List<InputStream> trustIS = new ArrayList<>();
        try {
            String[] certFiles = context.getAssets().list(path);
            if (certFiles != null) {
                for (String certName : certFiles) {
                    InputStream is = context.getAssets().open(path + "/" + certName);
                    if (certName.toLowerCase().contains("bks")) {
                        bksIS = is;
                        continue;
                    }
                    trustIS.add(is);
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        try {
            //单独的cer证书构建
            TrustManagerFactory trustManagerFactory = null;
            if (trustIS.size() > 0) {
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(null);

                for (int i = 0, size = trustIS.size(); i < size; ) {
                    try {
                        InputStream certificate = trustIS.get(i);
                        String certificateAlias = Integer.toString(i++);
                        keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));
                        if (certificate != null)
                            certificate.close();
                        Log.i(TAG, "add certificate " + certificateAlias);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //信任证书(服务器)构建
                trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(keyStore);
            }

            //构建客户端的证书,双向认证
            KeyManagerFactory keyManagerFactory = null;
            KeyStore clientKeyStore = KeyStore.getInstance("BKS");
            if (bksIS != null) {
                try {
                    clientKeyStore.load(bksIS, bks_storepass == null ? null : bks_storepass.toCharArray());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(clientKeyStore, bks_storepass == null ? null : bks_storepass.toCharArray());

                //如果没有cer,从bks里拿信任证书
                if (trustManagerFactory == null) {
                    trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    trustManagerFactory.init(clientKeyStore);
                }
            }

            //生成SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory == null ? null : keyManagerFactory.getKeyManagers(),
                    trustManagerFactory == null ? null : trustManagerFactory.getTrustManagers(),
                    new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //检查访问需要设置自签名ssl不
    public static SSLSocketFactory checkSSL(String host, QSHttpConfig qsHttpConfig) {
        if (host != null) {
            SSLSocketFactory sslSocketFactory = qsHttpConfig.sslSocketFactory();
            String[] sslHost = qsHttpConfig.sslHost();
            if (sslSocketFactory != null) {
                if (sslHost == null || sslHost.length == 0)
                    return sslSocketFactory;
                for (String s : sslHost)
                    if (host.contains(s))
                        return sslSocketFactory;
            }
        }
        return null;
    }

    public static void Log(RequestParams request, ResponseParams response, long time) {
        String requestLog = requestLog(request);
        String responseLog = responseLog(response);
        String log = requestLog + "\nResponse[" + time + "ms]-> ↓↓↓ \n" + responseLog + "\n ";
        Log.d(TAG, log);
    }


    public static String requestLog(RequestParams request) {

        String log = "";
        HttpEnum.RequestMethod type = request.requestMethod();
        Map<String, String> head_map = request.headers();
        Map<String, Object> param_map = request.params();

        switch (type) {
            case GET:
            case HEAD:
            case DELETE:
            case OPTIONS:
//                StringBuilder sbUrl = new StringBuilder();
//                sbUrl.append(request.urlAndPath()).append("?");
//                for (String name : param_map.keySet()) {
//                    String value = String.valueOf(param_map.get(name));
//                    sbUrl.append(name).append('=').append(value).append('&');
//                }
//                sbUrl.deleteCharAt(sbUrl.length() - 1);
                log = type + "->" + request.urlEncode()
                        + "\nHeaders->" + head_map;
                break;
            case POST:
            case PUT:
            case PATCH:
                if (request.multipartBody() != null) {
                    log = type + "->" + request.urlAndPath()
                            + "\nHeaders->" + head_map
                            + "\nContent-Type->" + request.multipartType()
                            + "\nMultipartBody->" + request.multipartBody();
                } else if (request.requestBody() != null) {
                    log = type + "->" + request.urlAndPath()
                            + "\nHeaders->" + head_map
                            + "\nContent-Type->" + request.requestBody().getContentType()
                            + "\nRequestBody->" + request.requestBody().getContent();
                } else {
                    log = type + "->" + request.urlAndPath()
                            + "\nHeaders->" + head_map
                            + "\nContent-Type->" + HttpEnum.CONTENT_TYPE_URL_ + request.charset()
                            + "\nFormBody->" + param_map;
                }
                break;
        }
        return log;
    }

    public static String responseLog(ResponseParams response) {
        String responseLog = "";
        if (response.isSuccess()) {
            switch (response.resultType()) {
                case STRING:
                    responseLog = response.string();
                    if (FORMAT_JSON)
                        responseLog = formatJson(responseLog);
                    responseLog = "Headers->" + response.headers() + "\nResult->" + responseLog;
                    break;
                case FILE:
                    responseLog = "Headers->" + response.headers() + "\nResult->file:" + response.file();
                    break;
                case BYTES:
                    responseLog = "Headers->" + response.headers() + "\nResult->bytes:" + response.bytes().length;
                    break;
            }
        } else {
            responseLog = "Error->" + response.exception().getMessage();
        }
        return responseLog;
    }

    public static String URLEncoder(String value, String charset) {
        try {
            if (TextUtils.isEmpty(charset)) charset = HttpEnum.CHARSET_UTF8;
            value = URLEncoder.encode(value, charset);// 中文转化为网址格式（%xx%xx
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return value;
    }

    public static String URLDecoder(String value, String charset) {
        try {
            if (TextUtils.isEmpty(charset)) charset = HttpEnum.CHARSET_UTF8;
            value = URLDecoder.decode(value, charset);// 网址格式（%xx%xx转化为中文
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return value;
    }

    public static Charset charset(String contentType) {
        return Charset.forName(charsetName(contentType));
    }

    public static String charsetName(String contentType) {
        String charset = HttpEnum.CHARSET_UTF8;
        try {
            if (contentType != null) {
                contentType = contentType.toLowerCase();
                String[] arr = contentType.split(";");
                for (String s : arr) {
                    if (s.contains("charset"))
                        charset = s.substring(s.indexOf("=") + 1, s.length()).trim();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return charset;
    }

    /**
     * 检查网络
     */
    public static boolean checkNet() {
        if (QSHttpManage.application == null)
            return false;
        ConnectivityManager connectivityManager = (ConnectivityManager) QSHttpManage.application.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager == null ? null : connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable();
    }

    public static void showToast(String str) {
        if (QSHttpManage.application != null && str != null) {
            Toast toast = Toast.makeText(QSHttpManage.application, str, Toast.LENGTH_LONG);
            toast.setText(str);
            toast.show();
        }
    }


    public static void cleanCache() {
        deleteAllFile(new File(getDiskCacheDir()));
    }

    /**
     * 删除目录所有文件
     */
    public static void deleteAllFile(final File FFF) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (FFF != null) {
                    dele(FFF);
                }
            }

            void dele(File files) {
                for (File file : files.listFiles())
                    if (file.isDirectory()) {
                        dele(file);
                        file.delete();// 所有子目录删除
                    } else
                        file.delete();
                // files.delete();// 包括父目录也删除
            }
        }).start();
    }

    public static File cacheDir;

    /**
     * 获取外部存储上私有数据的路径
     */
    public static String getDiskCacheDir() {
        Context context = QSHttpManage.application;
        File cacheDir;
        if (context == null)
            cacheDir = new File(Environment.getExternalStorageDirectory(), "qshttp_cache");
        else if ((Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable())
                && checkPer(context, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            cacheDir = new File(context.getExternalCacheDir(), "qshttp_cache");
        } else {
            cacheDir = new File(context.getCacheDir(), "qshttp_cache");
        }
        if (!cacheDir.exists())
            cacheDir.mkdirs();
        if (Utils.cacheDir != null) {
            if (!TextUtils.equals(cacheDir.getAbsolutePath(), Utils.cacheDir.getAbsolutePath()))
                Log.i(TAG, "getDiskCacheDir:" + cacheDir.getAbsolutePath());
        }
        Utils.cacheDir = cacheDir;
        return cacheDir.getAbsolutePath();
    }

    public static boolean checkPer(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        } else {
            int var3 = permissions.length;

            for (int i = 0; i < var3; ++i) {
                String permission = permissions[i];
                boolean hasPermission = context.checkPermission
                        (permission, android.os.Process.myPid(), Process.myUid()) == PackageManager.PERMISSION_GRANTED;
                if (!hasPermission) {
                    return false;
                }
            }
            return true;
        }
    }


    /**
     * 写入字符串文件
     *
     * @return 布尔值
     */
    public static boolean writerString(String f, String s) {
        return writerBytes(f, s.getBytes());
    }

    /**
     * 写入字节
     *
     * @return 布尔值
     */
    public static boolean writerBytes(String f, byte[] bytes) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            fos.write(bytes, 0, bytes.length);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 读取字符
     */
    public static String readString(String s) {
        byte[] b = readBytes(s);
        if (b == null)
            return null;
        return new String(b);
    }

    /**
     * 读取字节
     */
    public static byte[] readBytes(String s) {
        FileInputStream fos = null;
        byte[] bytes;
        File f = new File(s);
        try {
            if (!f.exists())
                return null;
            bytes = new byte[(int) f.length()];
            fos = new FileInputStream(s);
            fos.read(bytes, 0, bytes.length);
            return bytes;

        } catch (IOException e) {
            e.printStackTrace();
            return null;

        } finally {
            try {
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 文件拷贝
     */
    public static boolean fileCopy(String from, String to) {
        boolean result = false;
        int size = 8 * 1024;//大于8K就不需要buffered了 会变慢
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(from);
            out = new FileOutputStream(to);
            byte[] buffer = new byte[size];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            result = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
            }
        }
        return result;
    }


    //map转json
    public static String map2Json(Map<String, Object> map) {
        if (map == null)
            return "null";
        if (map.size() == 0)
            return "{}";
        StringBuilder json = new StringBuilder();
        json.append("{");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            json.append("\"" + entry.getKey() + "\":");
            Object obj = entry.getValue();
            if (obj instanceof Boolean
                    || obj instanceof Integer
                    || obj instanceof Double
                    || obj instanceof Float)
                json.append(obj);
            else if (obj instanceof Map)
                json.append(map2Json((Map<String, Object>) obj));
            else if (obj instanceof List)
                json.append(list2Json((List<?>) obj));
            else
                json.append("\"" + obj + "\"");
            json.append(",");
        }
        json.deleteCharAt(json.length() - 1).append("}");
        return json.toString();
    }

    public static String list2Json(List<?> list) {
        if (list == null)
            return "null";
        if (list.size() == 0)
            return "[]";
        StringBuilder json = new StringBuilder();
        json.append("[");
        for (Object obj : list) {
            if (obj instanceof Boolean
                    || obj instanceof Integer
                    || obj instanceof Double
                    || obj instanceof Float)
                json.append(obj);
            else if (obj instanceof Map)
                json.append(map2Json((Map<String, Object>) obj));
            else if (obj instanceof List)
                json.append(list2Json((List<?>) obj));
            else
                json.append("\"" + obj + "\"");
            json.append(",");
        }
        json.deleteCharAt(json.length() - 1).append("]");
        return json.toString();
    }

    /**
     * 格式化
     *
     * @param jsonStr
     * @return
     * @author lizhgb
     * @Date 2015-10-14 下午1:17:35
     */
    public static String formatJson(String jsonStr) {
        if (null == jsonStr || "".equals(jsonStr)) return "";
        StringBuilder sb = new StringBuilder("\n");
        char last = '\0';
        char current = '\0';
        int indent = 0;
        for (int i = 0; i < jsonStr.length(); i++) {
            last = current;
            current = jsonStr.charAt(i);
            switch (current) {
                case '{':
                case '[':
                    sb.append(current);
                    sb.append('\n');
                    indent++;
                    addIndentBlank(sb, indent);
                    break;
                case '}':
                case ']':
                    sb.append('\n');
                    indent--;
                    addIndentBlank(sb, indent);
                    sb.append(current);
                    break;
                case ',':
                    sb.append(current);
                    if (last != '\\') {
                        sb.append('\n');
                        addIndentBlank(sb, indent);
                    }
                    break;
                default:
                    sb.append(current);
            }
        }

        return sb.toString();
    }

    private static void addIndentBlank(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append('\t');
        }
    }
}
