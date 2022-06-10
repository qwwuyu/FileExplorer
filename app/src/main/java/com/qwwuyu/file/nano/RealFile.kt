package com.qwwuyu.file.nano

import java.io.*

/**
 * Created by qiwei on 2022/6/10.
 */
class RealFile(_file: File) : ProxyFile() {
    private val file: File = _file

    override fun getName(): String {
        return file.name
    }

    override fun getParentFile(): ProxyFile? {
        return RealFile(file.parentFile ?: return null)
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
        return (file.listFiles() ?: emptyArray()).map { RealFile(it) }.toTypedArray()
    }

    override fun inputStream(): InputStream {
        return FileInputStream(file)
    }

    override fun outputStream(): OutputStream {
        return FileOutputStream(file)
    }

    override fun createFile(name: String): ProxyFile {
        return RealFile(File(file, name))
    }

    override fun child(name: String): ProxyFile {
        return RealFile(File(file, name))
    }

    override fun createDirectory(name: String): ProxyFile? {
        val dir = File(file, name)
        val suc = dir.mkdir()
        return if (suc) RealFile(dir) else null
    }
}