package com.qwwuyu.file.database

import androidx.room.*

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(info: NoteInfo): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(info: NoteInfo): Int

    @Query("SELECT id, time, text FROM note")
    suspend fun queryAll(): List<NoteInfo>

    @Delete
    suspend fun delete(kvInfo: NoteInfo): Int

    @Query("DELETE FROM note")
    suspend fun clear(): Int
}