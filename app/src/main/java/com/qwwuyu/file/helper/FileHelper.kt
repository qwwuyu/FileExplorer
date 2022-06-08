package com.qwwuyu.file.helper

import android.os.Environment
import com.qwwuyu.file.WApplication
import com.qwwuyu.file.config.Constant
import com.qwwuyu.file.config.ManageConfig
import com.qwwuyu.file.entity.FileBean
import com.qwwuyu.file.entity.ResponseBean
import com.qwwuyu.file.utils.CommUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 文件工具类
 */
class FileHelper private constructor() {
    init {
        create()
    }

    /** 是否创建路径成功  */
    private var isCreate = false

    /** 临时存储路径  */
    var cachePath: String? = null
        private set

    /** 创建文件SD卡路径  */
    private fun create() {
        var cacheDir: File? = WApplication.context.cacheDir
        if (cacheDir == null && CommUtils.isExternalCacheEnable(WApplication.context)) {
            cacheDir = WApplication.context.externalCacheDir
        }
        cachePath = cacheDir.toString() + File.separator + "ManageCache" + File.separator
        val file = File(cachePath!!)
        if (file.isFile) file.delete()
        isCreate = file.isDirectory || file.mkdirs()
        file.listFiles()?.forEach { it.delete() }
    }

    fun checkCreate(): Boolean {
        if (!isCreate) create()
        return isCreate
    }

    companion object {
        /** 单例  */
        @JvmStatic
        val instance = FileHelper()

        @JvmStatic
        fun getDirectoryFile(path: String): List<FileBean> {
            val fileBeans: MutableList<FileBean> = ArrayList()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            file(path).listFiles()?.forEach { cf ->
                val name = cf.name
                if (ManageConfig.instance.isShowPointFile() || !name.startsWith(".")) {
                    val date: String = dateFormat.format(Date(cf.lastModified()))
                    val directory = cf.isDirectory
                    val child = if (directory) cf.list().orEmpty().size else 0
                    val info = if (directory) "${child}项" else CommUtils.getFileSize(cf.length())
                    val isInfo = !directory || ManageConfig.instance.isDirInfo()
                    val fileBean = FileBean(name, cf.isDirectory,
                        if (isInfo) date else null, if (isInfo) info else null)
                    fileBean.apk = ManageConfig.instance.isShowApk() && name.endsWith(".apk")
                    fileBeans.add(fileBean)
                }
            }
            fileBeans.sortWith { lhs, rhs ->
                if (lhs.dir && !rhs.dir) -1
                else if (!lhs.dir && rhs.dir) 1
                else lhs.name.compareTo(rhs.name)
            }
            return fileBeans
        }

        @JvmStatic
        fun delFile(path: String): ResponseBean {
            val f = file(path)
            val bean = ResponseBean()
            if (!f.isFile) {
                bean.state = Constant.HTTP_ERR
                bean.info = "文件不存在"
            } else if (f.delete()) {
                bean.state = Constant.HTTP_SUC
                bean.info = "删除成功"
            } else {
                bean.state = Constant.HTTP_ERR
                bean.info = "删除失败"
            }
            return bean
        }

        @JvmStatic
        fun delDir(path: String): ResponseBean {
            val f = file(path)
            val bean = ResponseBean()
            bean.state = Constant.HTTP_ERR
            if (!f.isDirectory) {
                bean.info = "文件夹不存在"
            } else {
                val list = f.list()
                if (list != null && list.isNotEmpty()) {
                    bean.info = "文件夹包含其他文件，无法删除"
                } else if (f.delete()) {
                    bean.state = Constant.HTTP_SUC
                    bean.info = "删除成功"
                } else {
                    bean.info = "删除失败"
                }
            }
            return bean
        }

        @JvmStatic
        fun createDir(path: String, dirName: String?): ResponseBean {
            val bean = ResponseBean()
            bean.state = Constant.HTTP_ERR
            if (dirName == null || dirName.matches(".*[\\\\/:*?\"<>|]+.*".toRegex())) {
                bean.info = "不能包含特殊字符\\/:*?\"<>|"
                return bean
            }
            val file = file(path)
            if (!file.isDirectory) {
                bean.info = "文件夹不存在"
                return bean
            }
            val dirFile = File(file, dirName)
            if (dirFile.exists()) {
                bean.info = "文件夹已存在"
                return bean
            }
            if (dirFile.mkdir()) {
                bean.state = Constant.HTTP_SUC
                bean.info = "创建文件夹成功"
            } else {
                bean.info = "创建文件夹失败"
            }
            return bean
        }

        @JvmStatic
        fun file(path: String): File {
            return File(Environment.getExternalStorageDirectory().absolutePath + path)
        }
    }
}