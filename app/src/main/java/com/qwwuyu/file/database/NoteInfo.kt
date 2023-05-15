package com.qwwuyu.file.database

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Keep
@Entity(tableName = "note", indices = [Index(value = ["id"], unique = true)])
data class NoteInfo(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var time: Long,
    var text: String,
) : Serializable