package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "levels")
data class LevelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val levelJson: String, // Full JSON schema for 3D map tiles, heightmap, enemies, items
    val stepCountSource: Int = 0,
    val elevationGainSource: Float = 0f,
    val depressionSource: Float = 0f,
    val distanceSourceMeters: Float = 0f,
    val providerUsed: String = "PROCEDURAL", // GROQ, OPENROUTER, OPENAI, GEMINI, PROCEDURAL
    val modelUsed: String = "local-synth",
    val difficulty: String = "Normal", // Easy, Normal, Cyberpunk, Nightmare
    val isCleared: Boolean = false,
    val bestTimeSeconds: Int = 0,
    val starsEarned: Int = 0
)
