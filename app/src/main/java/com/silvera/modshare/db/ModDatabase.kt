package com.silvera.modshare.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ModEntity::class], version = 1, exportSchema = false)
abstract class ModDatabase : RoomDatabase() {

    abstract fun modDao(): ModDao

    companion object {
        @Volatile
        private var INSTANCE: ModDatabase? = null

        fun getInstance(context: Context): ModDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ModDatabase::class.java,
                    "silvera_mods.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
