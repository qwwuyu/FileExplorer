package com.qwwuyu.file.database

import androidx.room.Room
import com.qwwuyu.file.WApplication
import kotlinx.coroutines.*
import java.util.concurrent.Executors

class DbHelper private constructor() {
    private val db: AppDatabase =
        Room.databaseBuilder(WApplication.context, AppDatabase::class.java, "file_explorer.db")
            .build()
    private val executor = Executors.newSingleThreadExecutor()
    private val dbCD = executor.asCoroutineDispatcher()

    companion object {
        private val instance: DbHelper by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { DbHelper() }

        fun db(): AppDatabase {
            return instance.db
        }

        fun noteDao(): NoteDao {
            return instance.db.noteDao()
        }

        fun dbCD(): ExecutorCoroutineDispatcher {
            return instance.dbCD
        }
    }
}