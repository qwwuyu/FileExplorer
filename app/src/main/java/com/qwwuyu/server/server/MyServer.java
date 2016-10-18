package com.qwwuyu.server.server;

import android.content.Context;

import com.google.gson.Gson;
import com.qwwuyu.server.utils.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class MyServer extends NanoHTTPD {
    private final String[] url = new String[]{".html", ".css", ".js"};
    private final String[] types = new String[]{"text/html", "text/css", "text/javascript"};
    private Context context;

    public MyServer(Context context, int port) {
        super(port);
        this.context = context;
    }

    @Override
    public Response serve(IHTTPSession session) {
        try {
            String uri = "/".equals(session.getUri()) ? "/ctrl.html" : session.getUri();
            if (uri.startsWith(ServerConfig.files)) {
                return callback(session, new Gson().toJson(FileUtil.getDirectoryFile(uri.substring(ServerConfig.files.length()))));
            } else if (uri.startsWith(ServerConfig.del)) {
                return callback(session, new Gson().toJson(FileUtil.delFile(uri.substring(ServerConfig.del.length()))));
            } else if (uri.startsWith(ServerConfig.down)) {
                File file = FileUtil.downFile(uri.substring(ServerConfig.down.length()));
                return newFixedLengthResponse(Response.Status.OK, "application/octet-stream", new FileInputStream(file), file.length());
            } else if (uri.startsWith(ServerConfig.look)) {
                File file = FileUtil.downFile(uri.substring(ServerConfig.look.length()));
                return newFixedLengthResponse(Response.Status.OK, null, new FileInputStream(file), file.length());
            } else if (uri.startsWith(ServerConfig.upload)) {
                File file = FileUtil.downFile(uri.substring(ServerConfig.upload.length()));
                Map<String, String> map = new LinkedHashMap<>();
                session.parseBody(map, file);
                return txt(new Gson().toJson(map));
            }
            try {
                InputStream is = context.getAssets().open("wifi" + uri);
                String mimeType = "text/plain";
                for (int i = 0; i < url.length; i++) {
                    if (uri.endsWith(url[i])) mimeType = types[i];
                }
                return newFixedLengthResponse(Response.Status.OK, mimeType, is, 0);
            } catch (Exception e) {
                return html("访问路径无效");
            }
        } catch (Exception ignored) {
            return html("操作失败");
        }
    }

    private Response callback(IHTTPSession session, String body) {
        String callback = session.getParms().get("callback");
        return txt(callback == null ? body : session.getParms().get("callback") + "(" + body + ")");
    }

    private Response txt(String body) {
        return newFixedLengthResponse(Response.Status.OK, "application/json", body);
    }

    private Response html(String body) {
        return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_HTML, "<html><body><h1>" + body + "</h1></body></html>");
    }
}
