package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entities.GameSettingsEntity
import com.example.data.local.entities.LevelEntity
import com.example.data.local.entities.MovementLogEntity
import kotlinx.coroutines.flow.Flow

class GameRepository(private val database: AppDatabase) {

    private val levelDao = database.levelDao()
    private val movementLogDao = database.movementLogDao()
    private val settingsDao = database.gameSettingsDao()

    val allLevels: Flow<List<LevelEntity>> = levelDao.getAllLevels()
    val allMovementLogs: Flow<List<MovementLogEntity>> = movementLogDao.getAllMovementLogs()
    val settingsFlow: Flow<GameSettingsEntity?> = settingsDao.getSettingsFlow()

    suspend fun getSettings(): GameSettingsEntity {
        return settingsDao.getSettings() ?: GameSettingsEntity().also {
            settingsDao.insertOrUpdateSettings(it)
        }
    }

    suspend fun saveSettings(settings: GameSettingsEntity) {
        settingsDao.insertOrUpdateSettings(settings)
    }

    suspend fun saveLevel(level: LevelEntity): Long {
        return levelDao.insertLevel(level)
    }

    suspend fun getLevelById(id: Long): LevelEntity? {
        return levelDao.getLevelById(id)
    }

    suspend fun getLatestLevel(): LevelEntity? {
        return levelDao.getLatestLevel()
    }

    suspend fun deleteLevel(levelId: Long) {
        levelDao.deleteLevelById(levelId)
    }

    suspend fun markLevelCleared(levelId: Long, bestTimeSeconds: Int, starsEarned: Int) {
        levelDao.markLevelCleared(levelId, bestTimeSeconds, starsEarned)
    }

    suspend fun logMovementSession(log: MovementLogEntity): Long {
        return movementLogDao.insertMovementLog(log)
    }

    suspend fun clearMovementLogs() {
        movementLogDao.clearAllLogs()
    }
}
