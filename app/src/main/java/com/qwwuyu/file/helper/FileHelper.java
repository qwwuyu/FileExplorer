package com.qwwuyu.file.helper;

import android.os.Environment;

import com.qwwuyu.file.WApplication;
import com.qwwuyu.file.config.Constant;
import com.qwwuyu.file.config.ManageConfig;
import com.qwwuyu.file.entity.FileBean;
import com.qwwuyu.file.entity.ResponseBean;
import com.qwwuyu.file.utils.AppUtils;
import com.qwwuyu.file.utils.CommUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 文件工具类
 */
public class FileHelper {
    /** 单例 */
    private static FileHelper instance = new FileHelper();
    /** 是否创建路径成功 */
    private boolean isCreate = false;
    /** 临时存储路径 */
    private String cachePath;

    public static FileHelper getInstance() {
        return instance;
    }

    private FileHelper() {
        create();
    }

    /** 创建文件SD卡路径 */
    private void create() {
        if (CommUtils.isExternalEnable(WApplication.context)) {
            String externalPath = WApplication.context.getExternalCacheDir().toString();
            cachePath = externalPath + File.separator + "ManageCache" + File.separator;
            File file = new File(cachePath);
            if (file.isFile()) file.delete();
            isCreate = file.exists() || file.mkdirs();
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
        }
    }

    public boolean checkCreate() {
        if (isCreate) return true;
        create();
        return isCreate;
    }

    public String getCachePath() {
        return cachePath;
    }

    public static List<FileBean> getDirectoryFile(String path) {
        File f = file(path);
        List<FileBean> fileBeans = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        File[] files = f.listFiles();
        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                if (ManageConfig.Companion.getInstance().isShowPointFile() || !name.startsWith(".")) {
                    fileBeans.add(new FileBean(name, file.isDirectory(), format.format(new Date(file.lastModified()))));
                }
            }
        }
        AppUtils.sortFileBean(fileBeans);
        return fileBeans;
    }

    public static ResponseBean delFile(String path) {
        File f = file(path);
        ResponseBean bean = new ResponseBean();
        if (!f.isFile()) {
            bean.setState(Constant.HTTP_ERR);
            bean.setInfo("文件不存在");
        } else if (f.delete()) {
            bean.setState(Constant.HTTP_SUC);
            bean.setInfo("删除成功");
        } else {
            bean.setState(Constant.HTTP_ERR);
            bean.setInfo("删除失败");
        }
        return bean;
    }

    public static ResponseBean delDir(String path) {
        File f = file(path);
        ResponseBean bean = new ResponseBean();
        bean.setState(Constant.HTTP_ERR);
        if (!f.isDirectory()) {
            bean.setInfo("文件夹不存在");
        } else {
            String[] list = f.list();
            if (list != null && list.length > 0) {
                bean.setInfo("文件夹包含其他文件，无法删除");
            } else if (f.delete()) {
                bean.setState(Constant.HTTP_SUC);
                bean.setInfo("删除成功");
            } else {
                bean.setInfo("删除失败");
            }
        }
        return bean;
    }

    public static ResponseBean createDir(String path, String dirName) {
        ResponseBean bean = new ResponseBean();
        bean.setState(Constant.HTTP_ERR);
        if (dirName == null || dirName.matches(".*[\\\\/:*?\"<>|]+.*")) {
            bean.setInfo("不能包含特殊字符\\/:*?\"<>|");
            return bean;
        }
        File file = file(path);
        if (!file.isDirectory()) {
            bean.setInfo("文件夹不存在");
            return bean;
        }
        File dirFile = new File(file, dirName);
        if (dirFile.exists()) {
            bean.setInfo("文件夹已存在");
            return bean;
        }
        if (dirFile.mkdir()) {
            bean.setState(Constant.HTTP_SUC);
            bean.setInfo("创建文件夹成功");
        } else {
            bean.setInfo("创建文件夹失败");
        }
        return bean;
    }

    public static File file(String path) {
        return new File(Environment.getExternalStorageDirectory().getAbsolutePath() + path);
    }
}
