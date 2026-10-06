package com.example.data.model

enum class TileType {
    FLOOR,
    ELEVATION_RAMP,
    DEPRESSION_PIT,
    WALL,
    LASER_GATE,
    TERMINAL,
    POWER_CORE,
    EXIT_PORTAL
}

enum class EnemyType {
    HUNTER,    // Actively tracks player coordinates
    SENTINEL,  // Defends high elevation platforms, fires pulse lasers
    PHANTOM,   // Glitches/teleports across depression zones
    STALKER    // Patrols corridors and flanks
}

data class Point3D(
    val x: Int,
    val y: Int,
    val z: Int = 0 // Elevation level: 0 is ground, 1-3 are elevated tiers, -1 is depression
)

data class LevelTile(
    val x: Int,
    val y: Int,
    val z: Int = 0,
    val type: TileType = TileType.FLOOR,
    var isCollected: Boolean = false,
    var isDeactivated: Boolean = false
)

data class EnemyData(
    val id: String,
    val name: String,
    val type: EnemyType,
    var x: Int,
    var y: Int,
    var z: Int = 0,
    var hp: Int = 100,
    val maxHp: Int = 100,
    val aggression: Float = 0.5f,
    val behaviorDescription: String = "Patrolling sector",
    var lastActionText: String = "Scanning for intruders...",
    var patrolWaypoints: List<Point3D> = emptyList(),
    var currentWaypointIndex: Int = 0
)

data class TerminalData(
    val id: String,
    val x: Int,
    val y: Int,
    val z: Int = 0,
    val requiredCores: Int = 1,
    var isUnlocked: Boolean = false,
    val securityLevel: Int = 1,
    val puzzlePrompt: String = "DECRYPT CYBER CIPHER"
)

data class LevelData(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val gridWidth: Int = 8,
    val gridHeight: Int = 8,
    val tiles: List<LevelTile>,
    val enemies: List<EnemyData>,
    val terminals: List<TerminalData>,
    val playerSpawn: Point3D,
    val exitPortal: Point3D,
    val totalCoresNeeded: Int = 3,
    val sourceElevationGain: Float = 0f,
    val sourceDepression: Float = 0f,
    val sourceSteps: Int = 0,
    val sourceDistance: Float = 0f,
    val aiBriefing: String = "Cyber-grid reconstructed from biological motion telemetry."
)
