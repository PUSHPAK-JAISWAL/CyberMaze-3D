package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.LevelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelDao {
    @Query("SELECT * FROM levels ORDER BY createdAt DESC")
    fun getAllLevels(): Flow<List<LevelEntity>>

    @Query("SELECT * FROM levels WHERE id = :id LIMIT 1")
    suspend fun getLevelById(id: Long): LevelEntity?

    @Query("SELECT * FROM levels ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestLevel(): LevelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLevel(level: LevelEntity): Long

    @Update
    suspend fun updateLevel(level: LevelEntity)

    @Delete
    suspend fun deleteLevel(level: LevelEntity)

    @Query("DELETE FROM levels WHERE id = :id")
    suspend fun deleteLevelById(id: Long)

    @Query("UPDATE levels SET isCleared = 1, bestTimeSeconds = :bestTime, starsEarned = :stars WHERE id = :id")
    suspend fun markLevelCleared(id: Long, bestTime: Int, stars: Int)
}
