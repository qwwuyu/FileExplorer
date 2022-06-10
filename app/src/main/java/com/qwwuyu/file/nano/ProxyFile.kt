package com.qwwuyu.file.nano

import java.io.InputStream
import java.io.OutputStream

/**
 * Created by qiwei on 2022/6/10.
 */
abstract class ProxyFile {
    abstract fun getName(): String
    abstract fun getParentFile(): ProxyFile?
    abstract fun isDirectory(): Boolean
    abstract fun isFile(): Boolean
    abstract fun lastModified(): Long
    abstract fun length(): Long
    abstract fun delete(): Boolean
    abstract fun exists(): Boolean
    abstract fun listFiles(): Array<ProxyFile>

    abstract fun installApk()
    abstract fun inputStream(): InputStream?
    abstract fun outputStream(): OutputStream?
    abstract fun createFile(name: String): ProxyFile?
    abstract fun child(name: String): ProxyFile?
    abstract fun createDirectory(name: String): ProxyFile?
    abstract fun getPath(): String
}