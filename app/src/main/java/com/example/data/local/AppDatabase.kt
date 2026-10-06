package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.GameSettingsDao
import com.example.data.local.dao.LevelDao
import com.example.data.local.dao.MovementLogDao
import com.example.data.local.entities.GameSettingsEntity
import com.example.data.local.entities.LevelEntity
import com.example.data.local.entities.MovementLogEntity

@Database(
    entities = [
        LevelEntity::class,
        MovementLogEntity::class,
        GameSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun levelDao(): LevelDao
    abstract fun movementLogDao(): MovementLogDao
    abstract fun gameSettingsDao(): GameSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cybermaze_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
