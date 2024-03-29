package com.qwwuyu.file.nano

import androidx.documentfile.provider.DocumentFile
import com.qwwuyu.file.WApplication
import com.qwwuyu.file.utils.CommUtils
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by qiwei on 2022/6/10.
 */
class DocFile(_file: DocumentFile) : ProxyFile() {
    private val file: DocumentFile = _file

    override fun getName(): String {
        return file.name ?: "UNKNOWN"
    }

    override fun getParentFile(): ProxyFile? {
        return DocFile(file.parentFile ?: return null)
    }

    override fun isDirectory(): Boolean {
        return file.isDirectory
    }

    override fun isFile(): Boolean {
        return file.isFile
    }

    override fun lastModified(): Long {
        return file.lastModified()
    }

    override fun length(): Long {
        return file.length()
    }

    override fun delete(): Boolean {
        return file.delete()
    }

    override fun exists(): Boolean {
        return file.exists()
    }

    override fun listFiles(): Array<ProxyFile> {
        return file.listFiles().map { DocFile(it) }.toTypedArray()
    }

    override fun installApk() {
        CommUtils.installApk(WApplication.context, file.uri)
    }

    override fun inputStream(): InputStream? {
        return WApplication.context.contentResolver.openInputStream(file.uri)
    }

    override fun outputStream(): OutputStream? {
        return WApplication.context.contentResolver.openOutputStream(file.uri)
    }

    override fun createFile(name: String): ProxyFile? {
        return DocFile(file.createFile("", name) ?: return null)
    }

    override fun child(name: String): ProxyFile? {
        return DocFile(file.findFile(name) ?: return null)
    }

    override fun createDirectory(name: String): ProxyFile? {
        return DocFile(file.createDirectory(name) ?: return null)
    }

    override fun getPath(): String {
        var str = file.uri.toString()
        if (!str.contains("%3A")) return str
        str = str.substring(str.lastIndexOf("%3A") + 3)
        return "/" + str.replace("%2F", "/")
    }

    override fun canRead(): Boolean {
        return file.canRead()
    }

    override fun canWrite(): Boolean {
        return file.canWrite()
    }
}