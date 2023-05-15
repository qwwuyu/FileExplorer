package com.qwwuyu.file.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [NoteInfo::class], version = 1 , exportSchema = false)
@TypeConverters
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}