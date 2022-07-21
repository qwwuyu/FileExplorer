package com.qwwuyu.file.utils;

import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HttpUtils {
    public static void redirect(String url, String key, String location) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(key)) return;
        new Thread(() -> {
            try {
                Map<String, String> params = new HashMap<>();
                params.put("path", key);
                params.put("location", location);
                upload(url, params, new String[0], new File[0]);
            } catch (Exception e) {
                LogUtils.logError(e);
            }
        }).start();
    }

    /** post方法 */
    public static String post(String url, String body) throws Exception {
        URL postUrl = new URL(url);
        // 打开连接

        HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        connection.connect();

        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        out.write(body.getBytes("UTF-8"));
        out.flush();
        out.close();

        int responseCode = connection.getResponseCode();
        LogUtils.i("responseCode:" + responseCode);
        String result = new String(streamToBytes(connection.getInputStream()), "UTF-8");
        LogUtils.i("result:" + result);
        connection.disconnect();
        return result;
    }

    /** 上传文件 */
    public static String upload(String url, Map<String, String> params, String[] names, File[] files) throws Exception {
        final String enter = "\r\n";
        final String boundary = "----WebKitFormBoundaryQJDb3vvO3mUQrAVh";

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        OutputStream output = connection.getOutputStream();

        StringBuilder paramsSb = new StringBuilder();
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {// 构造文本类型参数的实体数据
                paramsSb.append("--").append(boundary).append(enter);
                paramsSb.append(String.format("Content-Disposition: form-data; name=\"%s\"", entry.getKey())).append(enter).append(enter);
                paramsSb.append(entry.getValue()).append(enter);
            }
        }
        output.write(paramsSb.toString().getBytes());

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            StringBuilder fileSB = new StringBuilder();
            fileSB.append("--").append(boundary).append(enter);
            fileSB.append(String.format("Content-Disposition: form-data;name=\"%s\";filename=\"%s\"", names[i], file.getName())).append(enter);
            fileSB.append("Content-Type: application/octet-stream").append(enter).append(enter);
            output.write(fileSB.toString().getBytes());
            FileInputStream inputStream = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer, 0, 1024)) != -1) {
                output.write(buffer, 0, len);
            }
            inputStream.close();
            output.write(enter.getBytes());
        }

        output.write(("--" + boundary + "--" + enter).getBytes());
        output.flush();
        output.close();

        int responseCode = connection.getResponseCode();
        LogUtils.i("responseCode:" + responseCode);
        String result = new String(streamToBytes(connection.getInputStream()), "UTF-8");
        LogUtils.i("result:" + result);
        connection.disconnect();
        return result;
    }

    private static byte[] streamToBytes(InputStream in) throws IOException {
        byte[] buffer = new byte[5120];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int readSize;
        while ((readSize = in.read(buffer)) >= 0) out.write(buffer, 0, readSize);
        in.close();
        return out.toByteArray();
    }
}
