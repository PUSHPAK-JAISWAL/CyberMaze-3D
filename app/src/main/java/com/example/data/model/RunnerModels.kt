package com.example.data.model

enum class RunnerLane(val index: Int) {
    LEFT(-1),
    CENTER(0),
    RIGHT(1);

    companion object {
        fun fromIndex(idx: Int): RunnerLane = when (idx) {
            -1 -> LEFT
            1 -> RIGHT
            else -> CENTER
        }
    }
}

enum class ObstacleType {
    LOW_BARRIER,     // Jump over
    HIGH_LASER,      // Slide under
    GLITCH_PIT,      // Jump over
    ELEVATED_RAMP,   // Run up onto high platform
    SECURITY_DRONE   // EMP or dodge
}

data class ObstacleItem(
    val id: Long,
    val lane: RunnerLane,
    var zDistance: Float, // Distance ahead in meters (e.g. 5m to 80m)
    val type: ObstacleType,
    var isCleared: Boolean = false,
    var isDestroyed: Boolean = false
)

enum class CollectibleType {
    NEON_BIT,        // Subway Surfers coins
    ENERGY_CORE,     // Charges battle cards
    MULTIPLIER_CHIP, // 2x score multiplier
    MAGNET,          // Pulls in all nearby coins
    CYBER_SHIELD     // Protects from crash
}

data class CollectibleItem(
    val id: Long,
    val lane: RunnerLane,
    var zDistance: Float,
    val type: CollectibleType,
    var isCollected: Boolean = false,
    val elevationOffset: Float = 0f
)

enum class BattleCardType {
    EMP_BLAST,       // Destroys all obstacles on screen (Clash Zap/Log)
    WARP_DRIVE,      // High speed invincibility coin magnet (Rocket/Jetpack)
    SHIELD_MATRIX,   // Absorbs next collision
    CHRONO_SLOW,     // Slows down time by 50% for 5s (Freeze)
    DRONE_STRIKE     // Ally drone blasts obstacles ahead
}

data class BattleCard(
    val id: String,
    val type: BattleCardType,
    val name: String,
    val description: String,
    val energyCost: Int = 30,
    val cooldownSeconds: Float = 10f,
    var currentCooldown: Float = 0f,
    var level: Int = 1,
    var upgradeBitsCost: Int = 150,
    val isUnlocked: Boolean = true
)

data class CyberChest(
    val id: Long,
    val name: String,
    val stepsRequired: Int,
    val bitsReward: Int,
    val isUnlocked: Boolean,
    val isOpened: Boolean,
    val cardTypeReward: BattleCardType? = null
)

data class RunnerGameState(
    val currentLane: RunnerLane = RunnerLane.CENTER,
    val targetLane: RunnerLane = RunnerLane.CENTER,
    val lanePosition: Float = 0f, // Smooth interpolated lane [-1f..1f]
    val playerY: Float = 0f,      // Jump altitude (0f is ground, up to 1.8f)
    val isJumping: Boolean = false,
    val isSliding: Boolean = false,
    val slideRemainingSeconds: Float = 0f,
    val hasShield: Boolean = false,
    val isMagnetActive: Boolean = false,
    val magnetRemainingSeconds: Float = 0f,
    val isWarpActive: Boolean = false,
    val warpRemainingSeconds: Float = 0f,
    val isChronoSlowActive: Boolean = false,
    val chronoRemainingSeconds: Float = 0f,
    val distanceMeters: Float = 0f,
    val speedMps: Float = 14f,
    val score: Long = 0,
    val coinsCollected: Int = 0,
    val multiplier: Int = 1,
    val energyCharge: Int = 50,    // 0 to 100 for card activation
    val maxEnergy: Int = 100,
    val bossDroneDistance: Float = 16f, // Danger when < 6m
    val isDroneAlerted: Boolean = false,
    val stumbleCount: Int = 0,
    val obstacles: List<ObstacleItem> = emptyList(),
    val collectibles: List<CollectibleItem> = emptyList(),
    val activeDeck: List<BattleCard> = emptyList(),
    val isGameOver: Boolean = false,
    val highScore: Long = 0,
    val lastBannerMessage: String = "RUN! Swipe LEFT/RIGHT to dodge, UP to vault, DOWN to slide!"
)
