package com.qwwuyu.file.nano;

import android.content.Context;
import android.text.TextUtils;

import com.qwwuyu.file.config.Constant;
import com.qwwuyu.file.config.ManageConfig;
import com.qwwuyu.file.database.NoteInfo;
import com.qwwuyu.file.entity.FileBean;
import com.qwwuyu.file.entity.FileResultEntity;
import com.qwwuyu.file.entity.ResponseBean;
import com.qwwuyu.file.helper.FileHelper;
import com.qwwuyu.file.helper.GsonHelper;
import com.qwwuyu.file.helper.NoteHelper;
import com.qwwuyu.file.utils.AppUtils;
import com.qwwuyu.file.utils.CommUtils;

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
                    case Constant.URL_QUERY: {
                        List<FileBean> list = FileHelper.getDirectoryFile(file);
                        ResponseBean responseBean = AppUtils.getSuccessBean().setData(list);
                        return callback(session, GsonHelper.toJson(responseBean));
                    }
                    case Constant.URL_DEL: {
                        return callback(session, GsonHelper.toJson(FileHelper.delFile(file)));
                    }
                    case Constant.URL_APK: {
                        file.installApk();
                        return callback(session, GsonHelper.toJson(AppUtils.getSuccessBean()));
                    }
                    case Constant.URL_DEL_DIR: {
                        return callback(session, GsonHelper.toJson(FileHelper.delDir(file)));
                    }
                    case Constant.URL_DOWNLOAD: {
                        return download(file, true);
                    }
                    case Constant.URL_OPEN: {
                        return download(file, false);
                    }
                    case Constant.URL_UPLOAD: {
                        FileResultEntity entity = new FileResultEntity();
                        session.parseBody(entity, file);
                        String txt = GsonHelper.toJson(entity);
                        if (entity.successFile != null && entity.successFile.size() > 0) {
                            return newFixedLengthResponse(Response.Status.OK, "application/json", txt);
                        } else if (entity.existFile != null && entity.existFile.size() > 0) {
                            return newFixedLengthResponse(Response.Status.CONFLICT, "application/json", txt);
                        } else {
                            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", txt);
                        }
                    }
                    case Constant.URL_CREATE_DIR: {
                        String dirName = session.getParms().get("dirName");
                        try {
                            dirName = URLDecoder.decode(dirName, "UTF-8");
                        } catch (Exception ignored) {
                        }
                        return callback(session, GsonHelper.toJson(FileHelper.createDir(file, dirName)));
                    }
                    case Constant.URL_NOTE_QUERY: {
                        ResponseBean bean = AppUtils.getSuccessBean().setData(NoteHelper.queryAll());
                        return callback(session, GsonHelper.toJson(bean));
                    }
                    case Constant.URL_NOTE_ADD: {
                        String text = session.getParms().get("text");
                        if (text == null || text.length() == 0) {
                            return callback(session, GsonHelper.toJson(AppUtils.getErrorBean()));
                        } else {
                            NoteHelper.insert(new NoteInfo(0, System.currentTimeMillis(), text));
                            return callback(session, GsonHelper.toJson(AppUtils.getSuccessBean()));
                        }
                    }
                    case Constant.URL_NOTE_DEL: {
                        String id = session.getParms().get("id");
                        if (id == null) {
                            return callback(session, GsonHelper.toJson(AppUtils.getErrorBean()));
                        }
                        NoteHelper.delete(new NoteInfo(Long.parseLong(id), 0, ""));
                        return callback(session, GsonHelper.toJson(AppUtils.getSuccessBean()));
                    }
                    case Constant.URL_NOTE_EDIT: {
                        String id = session.getParms().get("id");
                        String text = session.getParms().get("text");
                        if (id == null || text == null || text.length() == 0) {
                            return callback(session, GsonHelper.toJson(AppUtils.getErrorBean()));
                        }
                        NoteHelper.update(new NoteInfo(Long.parseLong(id), System.currentTimeMillis(), text));
                        return callback(session, GsonHelper.toJson(AppUtils.getSuccessBean()));
                    }
                    case Constant.URL_NOTE_CLEAR: {
                        NoteHelper.clear();
                        return callback(session, GsonHelper.toJson(AppUtils.getSuccessBean()));
                    }
                    case Constant.URL_NOTE_COPY2PHONE: {
                        String text = session.getParms().get("text");
                        if (!TextUtils.isEmpty(text)) {
                            CommUtils.setClipText(text);
                        }
                        return callback(session, GsonHelper.toJson(AppUtils.getSuccessBean()));
                    }
                    case Constant.URL_NOTE_COPY2WEB: {
                        String text = CommUtils.getClipText();
                        if (!TextUtils.isEmpty(text)) {
                            ResponseBean bean = AppUtils.getSuccessBean().setData(text);
                            return callback(session, GsonHelper.toJson(bean));
                        }
                        return callback(session, GsonHelper.toJson(AppUtils.getErrorBean().setInfo("剪切板没有数据或应用不在前台")));
                    }
                }
            }

            try {
                InputStream is = context.getAssets().open("build" + uri);
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
        return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_HTML, "<html><body><h1>" + body + "</h1></body></html>");
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
