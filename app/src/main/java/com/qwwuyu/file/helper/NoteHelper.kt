package com.qwwuyu.file.helper

import com.qwwuyu.file.NoteActivity
import com.qwwuyu.file.database.DbHelper
import com.qwwuyu.file.database.NoteInfo
import com.qwwuyu.file.utils.TransferUtil
import com.qwwuyu.file.utils.requestDb
import kotlinx.coroutines.GlobalScope

class NoteHelper {
    companion object {
        private fun <T> exec(data: suspend () -> T): T {
            return TransferUtil.transfer { result ->
                GlobalScope.requestDb(data, {
                    result.result(it, null)
                }, {
                    result.result(null, it)
                })
            }
        }

        @JvmStatic
        fun queryAll(): List<NoteInfo> {
            return exec { DbHelper.noteDao().queryAll() }
        }

        @JvmStatic
        fun insert(noteInfo: NoteInfo) {
            exec { DbHelper.noteDao().insert(noteInfo) }
            NoteActivity.refresh()
        }

        @JvmStatic
        fun update(noteInfo: NoteInfo) {
            exec { DbHelper.noteDao().update(noteInfo) }
            NoteActivity.refresh()
        }

        @JvmStatic
        fun delete(noteInfo: NoteInfo) {
            exec { DbHelper.noteDao().delete(noteInfo) }
            NoteActivity.refresh()
        }

        @JvmStatic
        fun clear() {
            exec { DbHelper.noteDao().clear() }
            NoteActivity.refresh()
        }
    }
}



