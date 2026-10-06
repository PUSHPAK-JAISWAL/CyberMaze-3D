package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movement_logs")
data class MovementLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Long = 0,
    val steps: Int = 0,
    val elevationGainMeters: Float = 0f,
    val depressionMeters: Float = 0f,
    val forwardDistanceMeters: Float = 0f,
    val lateralDistanceMeters: Float = 0f,
    val totalDistanceMeters: Float = 0f,
    val generatedLevelId: Long? = null
)
