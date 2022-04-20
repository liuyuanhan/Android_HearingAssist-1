package me.forrest.commonlib.util;

import android.content.res.AssetManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class HttpUtil {
    private final static String TAG = HttpUtil.class.getSimpleName();
    private boolean isInitHttps;

    public void initHttps(AssetManager assetManager, String fileName) {
        if (isInitHttps) {
            return;
        }
        try {
            InputStream is = assetManager.open(fileName);

            //设置https证书  cer服务器导出的
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate cer = cf.generateCertificate(is);

            //把证书设置到证书库
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            keyStore.setCertificateEntry("as", cer);

            //信任管理
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            X509TrustManager userTrustManager = (X509TrustManager) trustManagers[0];
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null,new TrustManager[]{userTrustManager}, new SecureRandom());
            SSLSocketFactory socketFactory = sslContext.getSocketFactory();
            okHttpClient = new OkHttpClient.Builder()
                    .sslSocketFactory(socketFactory, userTrustManager)
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            //在这里验证是否为可信任客户端
                            Log.d(TAG, "hostname :" + hostname + " " + session);
                            return true;
                        }
                    })
                    .build();
//            //使用自定义的okHttpClient
//            IO.setDefaultOkHttpCallFactory(okHttpClient);
//            IO.setDefaultOkHttpWebSocketFactory(okHttpClient);
            isInitHttps = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String toParams(Map<String, String> param) {
        if (param == null) { return ""; }
        StringBuilder sb = new StringBuilder();
        sb.append("?");
        Set<Map.Entry<String, String>> entrySet = param.entrySet();
        for (Map.Entry<String, String> entry : entrySet) {
            String key   = entry.getKey();
            String value = entry.getValue();
            if (!key.equals("function")) {
                if (value != null && !value.equals("")) {
                    sb.append("&").append(key).append("=").append(value);
                }
            }
        }
        return sb.toString();
    }

    public static String toJson(Map<String, String> param) {
        if (param == null) { return ""; }
        JSONObject jo = new JSONObject();
        Set<Map.Entry<String, String>> entrySet = param.entrySet();
        for (Map.Entry<String, String> entry : entrySet) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value != null && !value.equals("")) {
                try {
                    jo.put(key, value);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        String str = jo.toString();
        if ("{}".equals(str)) {
            str = "";
        }
        return str;
    }

    public static String toSpecialJson(Map<String, Object> param) {
        if (param == null) { return ""; }
        JSONObject jo = new JSONObject();
        Set<Map.Entry<String, Object>> entrySet = param.entrySet();
        for (Map.Entry<String, Object> entry : entrySet) {
            String key   = entry.getKey();
            Object value = entry.getValue();
            if (value != null) {
                try {
                    jo.put(key, value);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        String str = jo.toString();
        if ("{}".equals(str)) {
            str = "";
        }
        return str;
    }

    public interface HttpUtilCallback {
        void onError(IOException e);
        void onSuccess(String string);
        void onProgress(int progress);
    }

    private OkHttpClient okHttpClient;      //ok请求的客户端

    public HttpUtil() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        builder.addInterceptor(loggingInterceptor);
        builder.readTimeout(10000, TimeUnit.MILLISECONDS);
        builder.writeTimeout(10000, TimeUnit.MILLISECONDS);
        builder.connectTimeout(10000, TimeUnit.MILLISECONDS);
        okHttpClient = builder.build();
    }

    // get请求
    public void get(String url, Map<String, String> param, Callback callback) {
        String params = toParams(param);
        Request.Builder builder = new Request.Builder();
        builder.url(url + params); //请求的url
        // 添加头部
//        builder.addHeader("token", "xxx");
//        builder.addHeader("sign", HttpTool.toSign(json));

        Request request = builder.build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(callback);
    }

    // post请求
    public void post(String url, Map<String, String> param, Callback callback) {
        String json = toJson(param);
        Request.Builder builder = new Request.Builder();
        builder.url(url);//请求的url
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        builder.post(requestBody);
//        builder.addHeader("sign", "xxx");

        Request request = builder.build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(callback);
    }


    public void get(String url, Map<String, String> param, HttpUtilCallback httpCallback) {
        String params = toParams(param);
        Request.Builder builder = new Request.Builder();
        builder.url(url + params); //请求的url
        // 添加头部
        //builder.addHeader("token", "xxx");
        //builder.addHeader("sign", HttpTool.toSign(json));

        Request request = builder.build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                httpCallback.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200 && response.body() != null) {
                    httpCallback.onSuccess(response.body().string());
                }
            }
        });
    }

    public void get(String url, Map<String, String> param, File file, HttpUtilCallback httpCallback) {
        String params = toParams(param);
        Request.Builder builder = new Request.Builder();
        builder.url(url + params); //请求的url
        // 添加头部
        //builder.addHeader("token", "xxx");
        //builder.addHeader("sign", HttpTool.toSign(json));

        Request request = builder.build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, e.toString());
                httpCallback.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, response.toString());
                if (response.code() == 200 && response.body() != null) {
                    InputStream is = response.body().byteStream();
                    byte[] data = new byte[1024];
                    FileOutputStream fos = new FileOutputStream(file);
                    long fileSize = response.body().contentLength();
                    int len = 0;
                    int sum = 0;
                    int progress = 0;
                    while ((len = is.read(data)) > 0) {
                        fos.write(data, 0, len);
                        sum = sum + len;
                        progress = (int) ((float)sum / fileSize * 100);
                        Log.d(TAG, String.format("fileSize:%d, sum:%d, progress:%d", fileSize, sum, progress));
                        httpCallback.onProgress(progress);
                    }
                    if (progress == 100) {
                        httpCallback.onSuccess("success");
                    }
                }
            }
        });
    }
}
