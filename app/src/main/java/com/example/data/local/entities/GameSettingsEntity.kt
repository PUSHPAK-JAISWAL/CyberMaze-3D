package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_settings")
data class GameSettingsEntity(
    @PrimaryKey
    val id: Int = 1, // Single row configuration
    val apiProvider: String = "GROQ", // GROQ, OPENROUTER, OPENAI, GEMINI, CUSTOM
    val apiKey: String = "",
    val modelId: String = "llama-3.3-70b-versatile",
    val customBaseUrl: String = "https://api.groq.com/openai/v1",
    val hapticsEnabled: Boolean = true,
    val soundFxEnabled: Boolean = true,
    val sensorSensitivity: Float = 1.0f,
    val dynamicAiEnemyEnabled: Boolean = true,
    val lastSelectedDifficulty: String = "Normal"
)
