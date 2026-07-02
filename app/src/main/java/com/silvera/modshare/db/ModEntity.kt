package com.silvera.modshare.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mods")
data class ModEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val description: String,
    val fileName: String,
    val filePath: String,
    val fileSizeBytes: Long,
    val dateAdded: Long
)
