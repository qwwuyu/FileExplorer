package com.qwwuyu.file.utils

import com.qwwuyu.file.database.DbHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


fun <T> CoroutineScope.requestDb(
    data: suspend () -> T,
    onSuc: (data: T) -> Unit,
    onErr: ((e: Exception) -> Unit)? = null
) {
    launch {
        try {
            val t = withContext(DbHelper.dbCD()) { data() }
            onSuc(t)
        } catch (e: Exception) {
            onErr?.invoke(e)
        }
    }
}

class AppExt {
    companion object {
        @JvmStatic
        fun <T> requestDb(
            data: suspend () -> T,
            onSuc: (data: T) -> Unit,
            onErr: ((e: Exception) -> Unit)? = null
        ) {
            GlobalScope.requestDb(data, onSuc, onErr)
        }
    }
}