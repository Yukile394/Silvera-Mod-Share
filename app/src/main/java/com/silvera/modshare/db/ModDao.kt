package com.silvera.modshare.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ModDao {

    @Query("SELECT * FROM mods ORDER BY dateAdded DESC")
    fun getAll(): Flow<List<ModEntity>>

    @Insert
    suspend fun insert(mod: ModEntity): Long

    @Delete
    suspend fun delete(mod: ModEntity)
}
