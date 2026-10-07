package com.example.data.model

// Unit / Troop Types for Infiltration Raids (Clash Royale / Clash of Clans)
enum class TroopType(
    val title: String,
    val description: String,
    val elixirCost: Int,
    val baseHp: Int,
    val baseDps: Int,
    val speed: Float,
    val rangeTiles: Float,
    val isFlying: Boolean = false,
    val targetsDefensesOnly: Boolean = false,
    val targetsCoreOnly: Boolean = false
) {
    BYTE_BRAWLER(
        title = "Byte Brawler",
        description = "Heavy armored cyber-tank that absorbs fire and smashes defenses.",
        elixirCost = 3,
        baseHp = 420,
        baseDps = 38,
        speed = 1.0f,
        rangeTiles = 1.2f,
        targetsDefensesOnly = true
    ),
    GLITCH_SPRINTER(
        title = "Glitch Sprinter",
        description = "Light-speed runner that bypasses traps to loot the Core Server.",
        elixirCost = 2,
        baseHp = 140,
        baseDps = 45,
        speed = 2.4f,
        rangeTiles = 1.0f,
        targetsCoreOnly = true
    ),
    EMP_HACKER(
        title = "EMP Specialist",
        description = "Ranged cyber-operative that temporarily zaps and disables defenses.",
        elixirCost = 4,
        baseHp = 220,
        baseDps = 52,
        speed = 1.3f,
        rangeTiles = 3.2f
    ),
    PHANTOM_DRONE(
        title = "Phantom Drone",
        description = "Aerial hover-drone that bypasses ground walls and lasers.",
        elixirCost = 5,
        baseHp = 310,
        baseDps = 60,
        speed = 1.6f,
        rangeTiles = 2.5f,
        isFlying = true
    )
}

// Tactical Spells available during raids (Elixir based)
enum class TacticalSpell(
    val title: String,
    val description: String,
    val elixirCost: Int,
    val durationSeconds: Float,
    val radiusTiles: Float
) {
    EMP_SURGE(
        title = "EMP Surge",
        description = "Blasts an area, stunning all enemy turrets for 5s.",
        elixirCost = 3,
        durationSeconds = 5f,
        radiusTiles = 2.5f
    ),
    OVERCLOCK(
        title = "Overclock Stim",
        description = "Grants +80% speed and attack rate to all active troops.",
        elixirCost = 2,
        durationSeconds = 6f,
        radiusTiles = 99f // global
    ),
    ORBITAL_BEAM(
        title = "Orbital Beam",
        description = "Deals 250 direct kinetic damage to targeted enemy structures.",
        elixirCost = 5,
        durationSeconds = 1.5f,
        radiusTiles = 2.0f
    )
}

// Defense Building Types placed in the player's CyberMaze (Tower Defense / Clash base)
enum class DefenseType(
    val title: String,
    val description: String,
    val costBits: Int,
    val baseHp: Int,
    val baseDps: Int,
    val rangeTiles: Float,
    val isWall: Boolean = false,
    val isTrap: Boolean = false,
    val isCore: Boolean = false
) {
    CORE_SERVER(
        title = "Quantum Core Server",
        description = "The nerve center of your maze. If destroyed, the attacker scores 2 stars.",
        costBits = 0,
        baseHp = 1200,
        baseDps = 20,
        rangeTiles = 3.0f,
        isCore = true
    ),
    NEON_WALL(
        title = "Reinforced Neon Wall",
        description = "Solid cyber barrier that forces attackers into maze choke points.",
        costBits = 25,
        baseHp = 500,
        baseDps = 0,
        rangeTiles = 0f,
        isWall = true
    ),
    LASER_TURRET(
        title = "Pulse Laser Turret",
        description = "Rapid-fire direct energy turret that targets single invaders.",
        costBits = 120,
        baseHp = 320,
        baseDps = 42,
        rangeTiles = 3.5f
    ),
    TESLA_PYLON(
        title = "Tesla Shock Pylon",
        description = "Arc-lightning tower that shocks up to 3 nearby troops at once.",
        costBits = 180,
        baseHp = 260,
        baseDps = 35,
        rangeTiles = 2.8f
    ),
    PLASMA_MORTAR(
        title = "Plasma Mortar",
        description = "Long-range ballistic launcher dealing heavy area-of-effect splash damage.",
        costBits = 240,
        baseHp = 290,
        baseDps = 55,
        rangeTiles = 5.0f
    ),
    GLITCH_MINE(
        title = "Stealth Glitch Mine",
        description = "Invisible trap that detonates when stepped on for 180 splash damage.",
        costBits = 60,
        baseHp = 50,
        baseDps = 180,
        rangeTiles = 1.2f,
        isTrap = true
    )
}

// Card in collection (troops or defenses)
data class CardItem(
    val id: String,
    val title: String,
    val isTroop: Boolean,
    val troopType: TroopType? = null,
    val defenseType: DefenseType? = null,
    val spellType: TacticalSpell? = null,
    var level: Int = 1,
    var cardsOwned: Int = 1,
    var upgradeCostBits: Int = 100,
    var isUnlocked: Boolean = true
)

// Building on the Maze Grid
data class MazeBuilding(
    val id: Long,
    val type: DefenseType,
    val gridX: Int,
    val gridY: Int,
    var currentHp: Float,
    val maxHp: Float,
    var level: Int = 1,
    var attackCooldown: Float = 0f,
    var isDestroyed: Boolean = false,
    var isStunned: Boolean = false,
    var stunRemaining: Float = 0f
)

// Active Troop in Raid Battle
data class ActiveTroop(
    val id: Long,
    val type: TroopType,
    var posX: Float,
    var posY: Float,
    var currentHp: Float,
    val maxHp: Float,
    val dps: Float,
    val speed: Float,
    val rangeTiles: Float,
    var targetBuildingId: Long? = null,
    var attackCooldown: Float = 0f,
    var isDead: Boolean = false
)

// Real-World Outdoor Syndicate Cache / Radar Node
data class RadarNode(
    val id: String,
    val title: String,
    val distanceMeters: Int,
    val requiredSteps: Int,
    val bitsReward: Int,
    val nanitesReward: Int,
    val blueprintReward: String,
    val isUnlocked: Boolean = false,
    val isClaimed: Boolean = false,
    val angleDegrees: Float = 0f // Direction on radar
)

// Pokemon GO style Proximity Player Base (Nearby Architect base detected by Geo-Radar)
data class NearbyPlayerBase(
    val id: String,
    val architectName: String,
    val rankTitle: String,
    val distanceMeters: Int,
    val angleDegrees: Float,
    val trophyCount: Int,
    val lootableBits: Int,
    val isDefeated: Boolean = false,
    val buildings: List<MazeBuilding> = emptyList()
)

// Live Raid State
data class RaidBattleState(
    val sectorName: String = "Syndicate Outpost Alpha",
    val sectorDifficulty: String = "Normal",
    val buildings: List<MazeBuilding> = emptyList(),
    val troops: List<ActiveTroop> = emptyList(),
    val elixir: Float = 5.0f,
    val maxElixir: Float = 10.0f,
    val timeRemainingSeconds: Float = 90.0f,
    val totalBuildingsInitial: Int = 0,
    val buildingsDestroyed: Int = 0,
    val destructionPercent: Int = 0,
    val starsEarned: Int = 0,
    val isCoreDestroyed: Boolean = false,
    val isOverclockActive: Boolean = false,
    val overclockRemaining: Float = 0f,
    val battleEnded: Boolean = false,
    val isVictory: Boolean = false,
    val bitsLooted: Int = 0,
    val trophiesWon: Int = 0,
    val bannerMessage: String = "Deploy troops at breach perimeter to infiltrate the syndicate maze!"
)
