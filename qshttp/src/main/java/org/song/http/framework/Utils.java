package org.song.http.framework;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by song on 2016/11/2.
 */

public class Utils {

    private static final String TAG = "QSHTTP";

    /**
     * 读取assets/path文件夹里的证书
     */
    public static SSLSocketFactory getAssetsSocketFactory(Context context, String path) {
        if (path == null)
            return null;
        List<InputStream> certificates = new ArrayList<>();
        // 添加https证书
        try {
            String[] certFiles = context.getAssets().list(path);
            if (certFiles != null) {
                for (String cert : certFiles) {
                    InputStream is = context.getAssets().open(path + "/" + cert);
                    certificates.add(is);
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        if (certificates.size() == 0)
            return null;
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            try {
                for (int i = 0, size = certificates.size(); i < size; ) {
                    InputStream certificate = certificates.get(i);
                    String certificateAlias = Integer.toString(i++);
                    keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));
                    if (certificate != null)
                        certificate.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            sslContext.init
                    (null, trustManagerFactory.getTrustManagers(), new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //检查访问需要设置自签名ssl不
    public static SSLSocketFactory checkSSL(String host) {
        if (host != null)
            if (HttpManage.sslSocketFactory != null) {
                if (HttpManage.sslHost == null)
                    return HttpManage.sslSocketFactory;
                for (String s : HttpManage.sslHost)
                    if (host.contains(s))
                        return HttpManage.sslSocketFactory;
            }
        return null;
    }

    public static void Log(RequestParams params, ResponseParams response) {
        if (HttpManage.DEBUG) {
            if (response.isSuccess())
                switch (response.resultType()) {
                    case STRING:
                        Log(params, "\nHeaders->" + response.headers() + "\nResult->" + response.string());
                        break;
                    case FILE:
                        Log(params, "\nHeaders->" + response.headers() + "\nResult->" + response.string());
                        break;
                    case BYTES:
                        Log(params, "\nHeaders" + response.headers() + "\nResult->" + response.bytes().length);
                        break;
                }
            else
                Log(params, "\nError->" + response.exception().getMessage());
        }
    }

    public static void Log(RequestParams params, String result) {
        //result = formatJson(result);
        HttpEnum.RequestType type = params.requestType();

        Map<String, String> head_map = params.headers();

        Map<String, String> param_map = params.params();
        StringBuilder sbParams = new StringBuilder();
        if (param_map != null)
            for (Map.Entry<String, String> entry : param_map.entrySet())
                sbParams.append("\nParam->" + entry.getKey() + "=" + entry.getValue());

        switch (type) {
            case GET:
            case HEAD:
            case DELETE:
                Log.e(TAG, type + "->" + params.urlFormat()
                        + "\nHeaders->" + head_map
                        + "\n请求结果-> ↓↓↓" + result);
                break;
            case POST:
            case PUT:
                Log.e(TAG, type + "->" + params.urlRestful()
                        + "\nHeaders->" + head_map
                        + sbParams.toString()
                        + "\n请求结果-> ↓↓↓" + result);

                break;
            case POST_CUSTOM:
            case PUT_CUSTOM:
                Log.e(TAG, type + "->" + params.urlRestful()
                        + "\nHeaders->" + head_map
                        + "\nContent-Type->" + params.customContent().getContentType()
                        + "\nContent->" + params.customContent().getContent()
                        + "\n请求结果-> ↓↓↓" + result);
                break;
            case POST_MULTIPART:
            case PUT_MULTIPART:
                Log.e(TAG, type + "->" + params.urlRestful()
                        + "\nHeaders->" + head_map
                        + sbParams.toString()
                        + "\nUpContent->" + params.uploadContent()
                        + "\n请求结果-> ↓↓↓" + result);

                break;
        }

    }

    public static String URLEncoder(String value) {
        try {
            value = URLEncoder.encode(value, charset(HttpManage.CONTENT_TYPE_URL).name());// 中文转化为网址格式（%xx%xx
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return value;
    }

    public static Charset charset(String contentType) {
        try {
            String charset = "utf-8";
            if (contentType != null) {
                contentType = contentType.toLowerCase();
                String[] arr = contentType.split(";");
                for (String s : arr) {
                    if (s.contains("charset"))
                        charset = s.substring(s.indexOf("=") + 1, s.length()).trim();
                }
            }
            return Charset.forName(charset);
        } catch (Exception e) {
            e.printStackTrace();
            return Charset.forName("utf-8");
        }
    }

    /**
     * 检查网络
     */
    public static boolean checkNet() {
        if (HttpManage.application == null)
            return true;
        ConnectivityManager connectivityManager = (ConnectivityManager) HttpManage.application.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable();
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

    /**
     * 获取外部存储上私有数据的路径
     */
    public static String getDiskCacheDir() {
        File file;
        Context context = HttpManage.application;
        if (context == null)
            file = new File("/sdcard/qshttp_cache");
        else if ((Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable())) {
            file = new File(context.getExternalCacheDir(), "qshttp_cache");
        } else {
            file = new File(context.getCacheDir(), "qshttp_cache");
        }
        if (!file.exists())
            file.mkdirs();
        return file.getAbsolutePath();
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
        f = getDiskCacheDir() + "/" + f;
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
        s = getDiskCacheDir() + "/" + s;
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
            }
        }
    }

    /**
     * 文件拷贝
     *
     * @param from
     * @param to
     * @return
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
        StringBuilder sb = new StringBuilder();
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
