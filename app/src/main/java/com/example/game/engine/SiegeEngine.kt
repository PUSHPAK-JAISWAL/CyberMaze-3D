package com.example.game.engine

import com.example.data.model.ActiveTroop
import com.example.data.model.DefenseType
import com.example.data.model.MazeBuilding
import com.example.data.model.RaidBattleState
import com.example.data.model.TacticalSpell
import com.example.data.model.TroopType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class SiegeEngine {

    private var nextId = 1000L

    // Player's Custom Defense Maze (8x8 Grid)
    private val _playerBase = MutableStateFlow<List<MazeBuilding>>(createDefaultPlayerBase())
    val playerBase: StateFlow<List<MazeBuilding>> = _playerBase.asStateFlow()

    // Active Raid Battle State
    private val _raidState = MutableStateFlow(RaidBattleState())
    val raidState: StateFlow<RaidBattleState> = _raidState.asStateFlow()

    // Is battle currently running (either raid attack or test defense)
    private var isSimulating = false

    private fun createDefaultPlayerBase(): List<MazeBuilding> {
        val list = mutableListOf<MazeBuilding>()

        // Core Quantum Server in center
        list.add(
            MazeBuilding(
                id = nextId++,
                type = DefenseType.CORE_SERVER,
                gridX = 4,
                gridY = 4,
                currentHp = DefenseType.CORE_SERVER.baseHp.toFloat(),
                maxHp = DefenseType.CORE_SERVER.baseHp.toFloat()
            )
        )

        // Defensive Neon Walls funneling into maze corridors
        val wallCoords = listOf(
            Pair(2, 2), Pair(3, 2), Pair(4, 2), Pair(5, 2), Pair(6, 2),
            Pair(2, 3),                                     Pair(6, 3),
            Pair(2, 4),                                     Pair(6, 4),
            Pair(2, 5),                                     Pair(6, 5),
            Pair(2, 6), Pair(3, 6),             Pair(5, 6), Pair(6, 6) // Choke opening at (4,6)
        )
        for ((wx, wy) in wallCoords) {
            list.add(
                MazeBuilding(
                    id = nextId++,
                    type = DefenseType.NEON_WALL,
                    gridX = wx,
                    gridY = wy,
                    currentHp = DefenseType.NEON_WALL.baseHp.toFloat(),
                    maxHp = DefenseType.NEON_WALL.baseHp.toFloat()
                )
            )
        }

        // Defenses inside maze perimeter
        list.add(
            MazeBuilding(
                id = nextId++,
                type = DefenseType.LASER_TURRET,
                gridX = 3,
                gridY = 3,
                currentHp = DefenseType.LASER_TURRET.baseHp.toFloat(),
                maxHp = DefenseType.LASER_TURRET.baseHp.toFloat()
            )
        )
        list.add(
            MazeBuilding(
                id = nextId++,
                type = DefenseType.TESLA_PYLON,
                gridX = 5,
                gridY = 3,
                currentHp = DefenseType.TESLA_PYLON.baseHp.toFloat(),
                maxHp = DefenseType.TESLA_PYLON.baseHp.toFloat()
            )
        )
        list.add(
            MazeBuilding(
                id = nextId++,
                type = DefenseType.PLASMA_MORTAR,
                gridX = 3,
                gridY = 5,
                currentHp = DefenseType.PLASMA_MORTAR.baseHp.toFloat(),
                maxHp = DefenseType.PLASMA_MORTAR.baseHp.toFloat()
            )
        )
        list.add(
            MazeBuilding(
                id = nextId++,
                type = DefenseType.GLITCH_MINE,
                gridX = 4,
                gridY = 6, // Hidden trap at the entrance choke!
                currentHp = DefenseType.GLITCH_MINE.baseHp.toFloat(),
                maxHp = DefenseType.GLITCH_MINE.baseHp.toFloat()
            )
        )

        return list
    }

    // Toggle or place building on Player Base Grid
    fun placeBuildingOnBase(type: DefenseType, gridX: Int, gridY: Int): Boolean {
        if (gridX !in 1..7 || gridY !in 1..7) return false
        val current = _playerBase.value.toMutableList()

        // Core cannot be placed twice or moved outside center
        if (type == DefenseType.CORE_SERVER && current.any { it.type == DefenseType.CORE_SERVER }) {
            return false
        }

        // Check if tile already occupied
        val existingIndex = current.indexOfFirst { it.gridX == gridX && it.gridY == gridY }
        if (existingIndex != -1) {
            val existing = current[existingIndex]
            if (existing.type == DefenseType.CORE_SERVER) return false // Cannot overwrite Core
            current.removeAt(existingIndex)
        }

        current.add(
            MazeBuilding(
                id = nextId++,
                type = type,
                gridX = gridX,
                gridY = gridY,
                currentHp = type.baseHp.toFloat(),
                maxHp = type.baseHp.toFloat()
            )
        )
        _playerBase.value = current
        return true
    }

    fun removeBuildingFromBase(gridX: Int, gridY: Int): Boolean {
        val current = _playerBase.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.gridX == gridX && it.gridY == gridY }
        if (existingIndex != -1) {
            if (current[existingIndex].type == DefenseType.CORE_SERVER) return false
            current.removeAt(existingIndex)
            _playerBase.value = current
            return true
        }
        return false
    }

    // Initialize a Syndicate Raid Infiltration Battle
    fun startRaidSector(sectorIndex: Int = 1) {
        val sectorName = when (sectorIndex) {
            1 -> "Syndicate Sector: Neon Alley"
            2 -> "Syndicate Sector: Iron Bastion"
            3 -> "Syndicate Sector: Quantum Spire"
            else -> "Syndicate Sector: Apex Citadel"
        }
        val difficulty = when (sectorIndex) {
            1 -> "Normal"
            2 -> "Hard"
            3 -> "Cyberpunk"
            else -> "Nightmare"
        }

        val enemyBuildings = generateEnemySectorMaze(sectorIndex)
        _raidState.value = RaidBattleState(
            sectorName = sectorName,
            sectorDifficulty = difficulty,
            buildings = enemyBuildings,
            troops = emptyList(),
            elixir = 6.0f,
            maxElixir = 10.0f,
            timeRemainingSeconds = 90.0f,
            totalBuildingsInitial = enemyBuildings.count { !it.type.isWall && !it.type.isTrap },
            buildingsDestroyed = 0,
            destructionPercent = 0,
            starsEarned = 0,
            isCoreDestroyed = false,
            battleEnded = false,
            isVictory = false,
            bitsLooted = 0,
            trophiesWon = 0,
            bannerMessage = "Battle Commenced! Tap troop icons below, then tap breach perimeter to deploy!"
        )
        isSimulating = true
    }

    // Test player's own base defense
    fun startDefenseTest() {
        val currentBase = _playerBase.value.map { it.copy(currentHp = it.maxHp, isDestroyed = false) }
        _raidState.value = RaidBattleState(
            sectorName = "Defense Simulation Protocol",
            sectorDifficulty = "Simulator",
            buildings = currentBase,
            troops = emptyList(),
            elixir = 10.0f,
            maxElixir = 10.0f,
            timeRemainingSeconds = 60.0f,
            totalBuildingsInitial = currentBase.count { !it.type.isWall && !it.type.isTrap },
            buildingsDestroyed = 0,
            destructionPercent = 0,
            starsEarned = 0,
            isCoreDestroyed = false,
            battleEnded = false,
            isVictory = false,
            bannerMessage = "Simulating Enemy Breach! Watch your maze defenses engage invaders!"
        )

        // Spawn simulated AI attackers at map edges
        spawnSimulatedAttackers()
        isSimulating = true
    }

    private fun spawnSimulatedAttackers() {
        val cur = _raidState.value
        val spawnTroops = mutableListOf<ActiveTroop>()
        val spawnCoords = listOf(
            Pair(4f, 0.5f), Pair(0.5f, 4f), Pair(4f, 8.5f), Pair(8.5f, 4f)
        )
        val types = listOf(TroopType.BYTE_BRAWLER, TroopType.GLITCH_SPRINTER, TroopType.EMP_HACKER, TroopType.PHANTOM_DRONE)

        for (i in 0..5) {
            val coord = spawnCoords[i % spawnCoords.size]
            val type = types[i % types.size]
            spawnTroops.add(
                ActiveTroop(
                    id = nextId++,
                    type = type,
                    posX = coord.first + Random.nextFloat() * 0.4f,
                    posY = coord.second + Random.nextFloat() * 0.4f,
                    currentHp = type.baseHp.toFloat(),
                    maxHp = type.baseHp.toFloat(),
                    dps = type.baseDps.toFloat(),
                    speed = type.speed,
                    rangeTiles = type.rangeTiles
                )
            )
        }
        _raidState.value = cur.copy(troops = spawnTroops)
    }

    private fun generateEnemySectorMaze(tier: Int): List<MazeBuilding> {
        val list = mutableListOf<MazeBuilding>()
        list.add(
            MazeBuilding(
                id = nextId++,
                type = DefenseType.CORE_SERVER,
                gridX = 4,
                gridY = 4,
                currentHp = (1000 + tier * 250).toFloat(),
                maxHp = (1000 + tier * 250).toFloat()
            )
        )

        // Walls layout
        val walls = listOf(
            Pair(2, 2), Pair(3, 2), Pair(4, 2), Pair(5, 2), Pair(6, 2),
            Pair(2, 3), Pair(6, 3),
            Pair(2, 5), Pair(6, 5),
            Pair(2, 6), Pair(3, 6), Pair(5, 6), Pair(6, 6)
        )
        for ((wx, wy) in walls) {
            list.add(
                MazeBuilding(
                    id = nextId++,
                    type = DefenseType.NEON_WALL,
                    gridX = wx,
                    gridY = wy,
                    currentHp = (400 + tier * 100).toFloat(),
                    maxHp = (400 + tier * 100).toFloat()
                )
            )
        }

        // Defenses
        list.add(
            MazeBuilding(
                id = nextId++,
                type = DefenseType.LASER_TURRET,
                gridX = 3,
                gridY = 3,
                currentHp = 300f + tier * 50f,
                maxHp = 300f + tier * 50f
            )
        )
        list.add(
            MazeBuilding(
                id = nextId++,
                type = DefenseType.TESLA_PYLON,
                gridX = 5,
                gridY = 3,
                currentHp = 250f + tier * 50f,
                maxHp = 250f + tier * 50f
            )
        )
        if (tier >= 2) {
            list.add(
                MazeBuilding(
                    id = nextId++,
                    type = DefenseType.PLASMA_MORTAR,
                    gridX = 3,
                    gridY = 5,
                    currentHp = 280f + tier * 60f,
                    maxHp = 280f + tier * 60f
                )
            )
        }
        if (tier >= 3) {
            list.add(
                MazeBuilding(
                    id = nextId++,
                    type = DefenseType.LASER_TURRET,
                    gridX = 5,
                    gridY = 5,
                    currentHp = 300f + tier * 50f,
                    maxHp = 300f + tier * 50f
                )
            )
        }
        return list
    }

    // Deploy Troop at coordinate (called by UI)
    fun deployTroop(type: TroopType, spawnX: Float, spawnY: Float, onDeployed: () -> Unit, onFail: (String) -> Unit) {
        val cur = _raidState.value
        if (cur.battleEnded) {
            onFail("Battle has concluded.")
            return
        }
        if (cur.elixir < type.elixirCost) {
            onFail("Insufficient Elixir! Need ${type.elixirCost} ⚡")
            return
        }

        // Clamp to perimeter or valid deployment zone
        val clampedX = spawnX.coerceIn(0.5f, 8.5f)
        val clampedY = spawnY.coerceIn(0.5f, 8.5f)

        val newTroop = ActiveTroop(
            id = nextId++,
            type = type,
            posX = clampedX,
            posY = clampedY,
            currentHp = type.baseHp.toFloat(),
            maxHp = type.baseHp.toFloat(),
            dps = type.baseDps.toFloat(),
            speed = type.speed,
            rangeTiles = type.rangeTiles
        )

        _raidState.value = cur.copy(
            elixir = cur.elixir - type.elixirCost,
            troops = cur.troops + newTroop,
            bannerMessage = "${type.title} deployed! Breaching perimeter!"
        )
        onDeployed()
    }

    // Cast Tactical Spell
    fun castSpell(spell: TacticalSpell, targetX: Float, targetY: Float, onCast: () -> Unit, onFail: (String) -> Unit) {
        val cur = _raidState.value
        if (cur.battleEnded) {
            onFail("Battle ended.")
            return
        }
        if (cur.elixir < spell.elixirCost) {
            onFail("Need ${spell.elixirCost} ⚡ Elixir!")
            return
        }

        when (spell) {
            TacticalSpell.EMP_SURGE -> {
                // Stun buildings in radius
                val updatedBuildings = cur.buildings.map { b ->
                    val dist = hypot(b.gridX - targetX, b.gridY - targetY)
                    if (dist <= spell.radiusTiles && !b.type.isWall) {
                        b.copy(isStunned = true, stunRemaining = spell.durationSeconds)
                    } else b
                }
                _raidState.value = cur.copy(
                    elixir = cur.elixir - spell.elixirCost,
                    buildings = updatedBuildings,
                    bannerMessage = "⚡ EMP Surge delivered! Enemy turrets disabled!"
                )
            }
            TacticalSpell.OVERCLOCK -> {
                _raidState.value = cur.copy(
                    elixir = cur.elixir - spell.elixirCost,
                    isOverclockActive = true,
                    overclockRemaining = spell.durationSeconds,
                    bannerMessage = "🔥 OVERCLOCK ENGAGED! Troops moving & attacking at 180%!"
                )
            }
            TacticalSpell.ORBITAL_BEAM -> {
                // Deal massive damage to buildings near target
                val updatedBuildings = cur.buildings.map { b ->
                    val dist = hypot(b.gridX - targetX, b.gridY - targetY)
                    if (dist <= spell.radiusTiles) {
                        val newHp = b.currentHp - 250f
                        b.copy(
                            currentHp = max(0f, newHp),
                            isDestroyed = newHp <= 0f
                        )
                    } else b
                }
                _raidState.value = cur.copy(
                    elixir = cur.elixir - spell.elixirCost,
                    buildings = updatedBuildings,
                    bannerMessage = "💥 Orbital Kinetic Strike obliterated target grid!"
                )
            }
        }
        onCast()
    }

    // Main 60 FPS Battle Physics & AI Tick
    fun tickBattleFrame(dt: Float, onStructureDestroyed: () -> Unit, onBattleEnd: (Boolean) -> Unit) {
        val cur = _raidState.value
        if (cur.battleEnded || !isSimulating) return

        // 1. Recharge Elixir (1 per 1.5s, up to 10)
        val newElixir = min(cur.maxElixir, cur.elixir + (dt * 0.68f))

        // 2. Battle Timer
        val newTime = max(0f, cur.timeRemainingSeconds - dt)

        // 3. Overclock Timer
        var overclockActive = cur.isOverclockActive
        var overclockTime = cur.overclockRemaining
        if (overclockActive) {
            overclockTime -= dt
            if (overclockTime <= 0f) {
                overclockActive = false
                overclockTime = 0f
            }
        }

        // 4. Update Buildings (cooldowns & stun timers)
        val buildings = cur.buildings.map { b ->
            var stunTime = b.stunRemaining
            var isStunned = b.isStunned
            if (isStunned) {
                stunTime -= dt
                if (stunTime <= 0f) {
                    isStunned = false
                    stunTime = 0f
                }
            }
            val newCooldown = max(0f, b.attackCooldown - dt)
            b.copy(attackCooldown = newCooldown, isStunned = isStunned, stunRemaining = stunTime)
        }.toMutableList()

        // 5. Update Troops (Movement & Attack AI)
        val troops = cur.troops.filter { !it.isDead }.map { troop ->
            val speedMult = if (overclockActive) 1.8f else 1.0f
            val dpsMult = if (overclockActive) 1.8f else 1.0f

            // Find optimal target
            val target = findBestTargetBuilding(troop, buildings)

            if (target != null) {
                val dist = hypot(troop.posX - target.gridX, troop.posY - target.gridY)

                if (dist <= troop.rangeTiles) {
                    // In attack range: attack building
                    val newCooldown = max(0f, troop.attackCooldown - dt)
                    if (newCooldown <= 0f) {
                        val dmg = (troop.dps * dpsMult) * 0.8f // attack interval ~0.8s
                        val bIndex = buildings.indexOfFirst { it.id == target.id }
                        if (bIndex != -1) {
                            val b = buildings[bIndex]
                            val newHp = max(0f, b.currentHp - dmg)
                            val destroyed = newHp <= 0f
                            buildings[bIndex] = b.copy(currentHp = newHp, isDestroyed = destroyed)
                            if (destroyed) onStructureDestroyed()
                        }
                        troop.copy(attackCooldown = 0.8f, targetBuildingId = target.id)
                    } else {
                        troop.copy(attackCooldown = newCooldown, targetBuildingId = target.id)
                    }
                } else {
                    // Move toward target
                    val dx = (target.gridX - troop.posX)
                    val dy = (target.gridY - troop.posY)
                    val moveDist = (troop.speed * speedMult) * dt
                    val totalDist = max(0.01f, hypot(dx, dy))
                    val stepX = (dx / totalDist) * moveDist
                    val stepY = (dy / totalDist) * moveDist

                    troop.copy(
                        posX = troop.posX + stepX,
                        posY = troop.posY + stepY,
                        attackCooldown = max(0f, troop.attackCooldown - dt),
                        targetBuildingId = target.id
                    )
                }
            } else {
                troop
            }
        }.toMutableList()

        // 6. Buildings Attack Troops
        for (i in buildings.indices) {
            val b = buildings[i]
            if (b.isDestroyed || b.isStunned || b.type.isWall || b.type.isTrap || b.attackCooldown > 0f) continue

            // Find nearest living troop
            val targetTroop = troops.filter { !it.isDead }.minByOrNull {
                hypot(it.posX - b.gridX, it.posY - b.gridY)
            }

            if (targetTroop != null) {
                val dist = hypot(targetTroop.posX - b.gridX, targetTroop.posY - b.gridY)
                if (dist <= b.type.rangeTiles) {
                    // Shoot target
                    val dmg = b.type.baseDps * 1.0f
                    val tIndex = troops.indexOfFirst { it.id == targetTroop.id }
                    if (tIndex != -1) {
                        val t = troops[tIndex]
                        val newHp = max(0f, t.currentHp - dmg)
                        troops[tIndex] = t.copy(currentHp = newHp, isDead = newHp <= 0f)
                    }
                    buildings[i] = b.copy(attackCooldown = 1.0f)
                }
            }
        }

        // 7. Calculate Destruction & Victory Conditions
        val destroyedDefenses = buildings.count { it.isDestroyed && !it.type.isWall && !it.type.isTrap }
        val core = buildings.find { it.type == DefenseType.CORE_SERVER }
        val isCoreDead = core?.isDestroyed == true

        val totalScoreBuildings = max(1, cur.totalBuildingsInitial)
        val percent = min(100, (destroyedDefenses * 100) / totalScoreBuildings)

        var stars = 0
        if (percent >= 50) stars++
        if (isCoreDead) stars++
        if (percent >= 100) stars++

        val allTroopsDead = troops.isNotEmpty() && troops.all { it.isDead }
        val timeout = newTime <= 0f
        val fullClear = percent >= 100

        var battleEnded = false
        var victory = false
        var bits = cur.bitsLooted
        var trophies = cur.trophiesWon

        if (fullClear || timeout || (allTroopsDead && newElixir < 2f)) {
            battleEnded = true
            isSimulating = false
            victory = stars > 0
            bits = if (victory) 350 + (stars * 180) else 50
            trophies = if (victory) 10 + (stars * 10) else 0
            onBattleEnd(victory)
        }

        _raidState.value = cur.copy(
            buildings = buildings,
            troops = troops.filter { !it.isDead },
            elixir = newElixir,
            timeRemainingSeconds = newTime,
            buildingsDestroyed = destroyedDefenses,
            destructionPercent = percent,
            starsEarned = stars,
            isCoreDestroyed = isCoreDead,
            isOverclockActive = overclockActive,
            overclockRemaining = overclockTime,
            battleEnded = battleEnded,
            isVictory = victory,
            bitsLooted = bits,
            trophiesWon = trophies,
            bannerMessage = if (battleEnded) {
                if (victory) "🏆 VICTORY! $stars STARS! Looted $bits Bits & +$trophies Trophies!"
                else "❌ RAID REPELLED! Reinforce your squad and try again!"
            } else cur.bannerMessage
        )
    }

    private fun findBestTargetBuilding(troop: ActiveTroop, buildings: List<MazeBuilding>): MazeBuilding? {
        val activeBuildings = buildings.filter { !it.isDestroyed }
        if (activeBuildings.isEmpty()) return null

        if (troop.type.targetsCoreOnly) {
            val core = activeBuildings.find { it.type == DefenseType.CORE_SERVER }
            if (core != null) return core
        }

        if (troop.type.targetsDefensesOnly) {
            val defenses = activeBuildings.filter { !it.type.isWall && !it.type.isTrap }
            if (defenses.isNotEmpty()) {
                return defenses.minByOrNull { hypot(it.gridX - troop.posX, it.gridY - troop.posY) }
            }
        }

        // Flying units ignore walls
        val candidateBuildings = if (troop.type.isFlying) {
            activeBuildings.filter { !it.type.isWall && !it.type.isTrap }
        } else {
            activeBuildings.filter { !it.type.isTrap }
        }

        return candidateBuildings.minByOrNull { hypot(it.gridX - troop.posX, it.gridY - troop.posY) }
    }
}
