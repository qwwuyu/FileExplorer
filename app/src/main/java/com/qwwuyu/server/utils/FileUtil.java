package com.qwwuyu.server.utils;

import android.os.Environment;

import com.qwwuyu.server.bean.FileBean;
import com.qwwuyu.server.bean.ResultBean;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * 文件工具类
 */
public class FileUtil {
    /** 单例 */
    private static FileUtil instance = new FileUtil();
    /** 是否创建路径成功 */
    private boolean isCreate = false;
    /** 文件存储的基础路径 */
    private String basePath;
    /** 临时存储路径 */
    private String cachePath;
    /** 根路径名 */
    private static String externalPath;

    private FileUtil() {
        create();
    }

    /** 创建文件SD卡路径 */
    private void create() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            externalPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            basePath = externalPath + File.separator + "AAAA" + File.separator;
            isCreate = new File(basePath).exists() || new File(basePath).mkdirs();
            cachePath = basePath + "Cache" + File.separator;
            new File(cachePath).mkdirs();
        }
    }

    public static FileUtil getInstance() {
        return instance;
    }

    public boolean isCreate() {
        if (isCreate) return true;
        create();
        return isCreate;
    }

    public String getBasePath() {
        return basePath;
    }

    public String getCachePath() {
        return cachePath;
    }

    public File getBaseFile(String filename) {
        return new File(basePath + filename);
    }

    public File getCacheFile(String filename) {
        return new File(cachePath + filename);
    }

    public static void safeClose(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException ignored) {
        }
    }

    public static ArrayList<FileBean> getDirectoryFile(String path) {
        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + path);
        ArrayList<FileBean> fileBeans = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        File[] files = f.listFiles();
        for (File file : files) {
            FileBean bean = new FileBean();
            bean.setDirectory(file.isDirectory());
            bean.setTime(format.format(new Date(file.lastModified())));
            bean.setName(file.getName());
            bean.setPath(file.getPath().substring(externalPath.length()));
            fileBeans.add(bean);
        }
        SortUtil.sortFileBean(fileBeans);
        return fileBeans;
    }

    public static ResultBean delFile(String path) {
        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + path);
        boolean result = f.delete();
        ResultBean bean = new ResultBean();
        bean.setResult(result);
        bean.setData("删除成功");
        return bean;
    }

    public static File downFile(String path) {
        return new File(Environment.getExternalStorageDirectory().getAbsolutePath() + path);
    }
}
