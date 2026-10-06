package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.MovementLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovementLogDao {
    @Query("SELECT * FROM movement_logs ORDER BY timestamp DESC")
    fun getAllMovementLogs(): Flow<List<MovementLogEntity>>

    @Query("SELECT * FROM movement_logs ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMovementLog(): MovementLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovementLog(log: MovementLogEntity): Long

    @Query("DELETE FROM movement_logs")
    suspend fun clearAllLogs()
}
