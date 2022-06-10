package com.qwwuyu.file.nano;

import android.content.Context;

import com.qwwuyu.file.config.Constant;
import com.qwwuyu.file.config.ManageConfig;
import com.qwwuyu.file.entity.FileBean;
import com.qwwuyu.file.entity.FileResultEntity;
import com.qwwuyu.file.entity.ResponseBean;
import com.qwwuyu.file.helper.FileHelper;
import com.qwwuyu.file.helper.GsonHelper;
import com.qwwuyu.file.utils.AppUtils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

public class NanoServer extends NanoHTTPD {
    private final String[] url = new String[]{".html", ".css", ".js"};
    private final String[] types = new String[]{"text/html", "text/css", "text/javascript"};
    private Context context;

    public NanoServer(Context context, int port) {
        super(port, new TempFileManagerImpl());
        this.context = context;
    }

    @Override
    public Response serve(IHTTPSession session) {
        try {
            String uri = "/".equals(session.getUri()) ? "/index.html" : session.getUri();
            if (uri.startsWith("/i/")) {
                String path = session.getParms().get("path");
                if (path == null) path = "/";
                if (!path.startsWith("/")) path = "/" + path;
                ProxyFile file = FileHelper.getFile(path);
                switch (uri) {
                    case Constant.URL_QUERY:
                        List<FileBean> list = FileHelper.getDirectoryFile(file);
                        ResponseBean responseBean = AppUtils.getSuccessBean().setData(list);
                        return callback(session, GsonHelper.toJson(responseBean));
                    case Constant.URL_DEL:
                        return callback(session, GsonHelper.toJson(FileHelper.delFile(file)));
                    case Constant.URL_APK:
                        file.installApk();
                        return callback(session, GsonHelper.toJson(AppUtils.getSuccessBean()));
                    case Constant.URL_DEL_DIR:
                        return callback(session, GsonHelper.toJson(FileHelper.delDir(file)));
                    case Constant.URL_DOWNLOAD:
                        return download(file, true);
                    case Constant.URL_OPEN:
                        return download(file, false);
                    case Constant.UPL_UPLOAD:
                        FileResultEntity entity = new FileResultEntity();
                        session.parseBody(entity, file);
                        return txt(GsonHelper.toJson(entity));
                    case Constant.UPL_CREATE_DIR:
                        String dirName = session.getParms().get("dirName");
                        try {
                            dirName = URLDecoder.decode(dirName, "UTF-8");
                        } catch (Exception ignored) {
                        }
                        return callback(session, GsonHelper.toJson(FileHelper.createDir(file, dirName)));
                }
            }

            try {
                InputStream is = context.getAssets().open("manage" + uri);
                String mimeType = "text/plain";
                for (int i = 0; i < url.length; i++) {
                    if (uri.endsWith(url[i])) mimeType = types[i];
                }
                return newFixedLengthResponse(Response.Status.OK, mimeType, is, 0);
            } catch (Exception e) {
                return html("访问路径无效");
            }
        } catch (Exception e) {
            return html("操作失败：" + e.getMessage());
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

    private Response download(ProxyFile file, boolean download) throws Exception {
        if (file == null) throw new FileNotFoundException();
        String fileName = file.getName();
        Response response = newFixedLengthResponse(Response.Status.OK, download ? "application/octet-stream" : null, file.inputStream(), file.length());
        response.addHeader("Content-Disposition", (download ? "attachment" : "inline")
                + "; filename=\"" + URLEncoder.encode(fileName, "utf-8") + "\"");
        if (fileName.endsWith(".txt")) {
            response.addHeader("Content-Type", "text/plain; charset=" + ManageConfig.getInstance().getTxtEncoding());
        }
        return response;
    }
}
