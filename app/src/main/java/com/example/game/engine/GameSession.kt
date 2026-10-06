package com.example.game.engine

import com.example.data.model.EnemyData
import com.example.data.model.EnemyType
import com.example.data.model.LevelData
import com.example.data.model.LevelTile
import com.example.data.model.Point3D
import com.example.data.model.TerminalData
import com.example.data.model.TileType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

data class GamePlayState(
    val currentLevel: LevelData,
    val playerPos: Point3D,
    val playerHealth: Int = 100,
    val maxHealth: Int = 100,
    val coresCollected: Int = 0,
    val totalCoresInLevel: Int = 3,
    val empCharges: Int = 2,
    val cloakCharges: Int = 1,
    val isCloaked: Boolean = false,
    val cloakRemainingTurns: Int = 0,
    val movesCount: Int = 0,
    val elapsedSeconds: Int = 0,
    val score: Int = 0,
    val isTerminalUnlocked: Boolean = false,
    val isGameOver: Boolean = false,
    val isVictory: Boolean = false,
    val starsEarned: Int = 0,
    val isRadarActive: Boolean = false,
    val radarRemainingTicks: Int = 0,
    val shockwaveRadius: Float = 0f, // For EMP animation
    val activeCipherTerminal: TerminalData? = null, // Active hack mini-game
    val statusMessage: String = "Objective: Infiltrate sector, collect energy cores & decrypt exit terminal.",
    val camera: Camera3D = Camera3D(),
    val dynamicAiLog: String = "Tactical combat grid active."
)

enum class Direction {
    NORTH, SOUTH, EAST, WEST
}

class GameSession(initialLevel: LevelData) {

    private val _state = MutableStateFlow(
        GamePlayState(
            currentLevel = initialLevel,
            playerPos = initialLevel.playerSpawn,
            totalCoresInLevel = initialLevel.tiles.count { it.type == TileType.POWER_CORE }
        )
    )
    val state: StateFlow<GamePlayState> = _state.asStateFlow()

    fun resetLevel(level: LevelData = _state.value.currentLevel) {
        val freshTiles = level.tiles.map {
            it.copy(
                isCollected = false,
                isDeactivated = false,
                isActivated = false
            )
        }
        val freshEnemies = level.enemies.map {
            it.copy(
                hp = 100,
                isAlerted = false,
                isStunned = false,
                stunRemainingTurns = 0,
                lastActionText = "Patrolling sector..."
            )
        }
        val freshLevel = level.copy(tiles = freshTiles, enemies = freshEnemies)

        _state.value = GamePlayState(
            currentLevel = freshLevel,
            playerPos = freshLevel.playerSpawn,
            totalCoresInLevel = freshTiles.count { it.type == TileType.POWER_CORE }
        )
    }

    fun movePlayer(direction: Direction, onStepSuccess: () -> Unit, onEncounter: () -> Unit) {
        val current = _state.value
        if (current.isGameOver || current.isVictory || current.activeCipherTerminal != null) return

        val dx = when (direction) {
            Direction.EAST -> 1
            Direction.WEST -> -1
            else -> 0
        }
        val dy = when (direction) {
            Direction.SOUTH -> 1
            Direction.NORTH -> -1
            else -> 0
        }

        val targetX = current.playerPos.x + dx
        val targetY = current.playerPos.y + dy

        // Boundary check
        if (targetX !in 0 until current.currentLevel.gridWidth || targetY !in 0 until current.currentLevel.gridHeight) {
            _state.value = current.copy(statusMessage = "Boundary reached. Trajectory blocked.")
            return
        }

        val targetTile = current.currentLevel.tiles.find { it.x == targetX && it.y == targetY } ?: return

        // Wall collision check
        if (targetTile.type == TileType.WALL) {
            _state.value = current.copy(statusMessage = "Solid cyber barrier! Find another path.")
            return
        }

        // Laser Gate collision check (if active)
        if (targetTile.type == TileType.LASER_GATE && !targetTile.isDeactivated) {
            _state.value = current.copy(statusMessage = "Security laser fence active! Hack terminal or step on pressure switch to disable.")
            return
        }

        // Elevation difference check: steep cliff requires Jump
        val elevDiff = targetTile.z - current.playerPos.z
        if (elevDiff > 1) {
            _state.value = current.copy(statusMessage = "Cliff too steep (+${elevDiff}m)! Use JUMP / VAULT button to scale ledge.")
            return
        }

        var damageTaken = 0
        var status = "Moved to [$targetX, $targetY]"
        var scoreDelta = 10

        // Hazard pit check
        if (targetTile.type == TileType.DEPRESSION_PIT) {
            damageTaken = 15
            status = "Dropped into sensory depression pit! (-15 HP)"
        }

        // Pressure Switch check
        var updatedTiles = current.currentLevel.tiles
        if (targetTile.type == TileType.PRESSURE_SWITCH && !targetTile.isActivated) {
            status = "Pressure switch triggered! Disabling nearest laser grid..."
            scoreDelta += 50
            updatedTiles = updatedTiles.map { t ->
                when {
                    t.x == targetX && t.y == targetY -> t.copy(isActivated = true)
                    t.type == TileType.LASER_GATE -> t.copy(isDeactivated = true)
                    else -> t
                }
            }
        }

        // Power Core collection
        var newCores = current.coresCollected
        var newEmpCharges = current.empCharges
        if (targetTile.type == TileType.POWER_CORE && !targetTile.isCollected) {
            newCores += 1
            newEmpCharges = (newEmpCharges + 1).coerceAtMost(3)
            scoreDelta += 100
            status = "Energy Core acquired! EMP charge restored ($newCores/${current.totalCoresInLevel})"
            updatedTiles = updatedTiles.map { t ->
                if (t.x == targetX && t.y == targetY) t.copy(isCollected = true) else t
            }
        }

        // Update Cloak turns
        val remainingCloak = if (current.isCloaked) (current.cloakRemainingTurns - 1).coerceAtLeast(0) else 0
        val isStillCloaked = remainingCloak > 0

        val newPos = Point3D(targetX, targetY, targetTile.z)
        val newHealth = (current.playerHealth - damageTaken).coerceAtLeast(0)

        // Check Victory at Exit Portal
        val reachedExit = targetX == current.currentLevel.exitPortal.x && targetY == current.currentLevel.exitPortal.y
        var isVictory = false
        var stars = 0
        if (reachedExit) {
            if (newCores >= current.totalCoresInLevel || current.isTerminalUnlocked) {
                isVictory = true
                stars = 1
                if (newCores >= current.totalCoresInLevel) stars++
                if (current.movesCount + 1 <= current.currentLevel.parMoves) stars++
                status = "SECTOR OVERRIDDEN! Clean extraction achieved."
                scoreDelta += 500
            } else {
                status = "Portal locked! Collect all cores or decrypt cipher terminal first."
            }
        }

        val updatedLevel = current.currentLevel.copy(tiles = updatedTiles)

        _state.value = current.copy(
            currentLevel = updatedLevel,
            playerPos = newPos,
            playerHealth = newHealth,
            coresCollected = newCores,
            empCharges = newEmpCharges,
            isCloaked = isStillCloaked,
            cloakRemainingTurns = remainingCloak,
            movesCount = current.movesCount + 1,
            score = current.score + scoreDelta,
            isGameOver = newHealth <= 0,
            isVictory = isVictory,
            starsEarned = stars,
            statusMessage = status
        )

        onStepSuccess()

        // Turn-based tactical progression: Enemies execute their move in response
        executeEnemyTurn(onEncounter)
    }

    private fun executeEnemyTurn(onEncounter: () -> Unit) {
        val current = _state.value
        if (current.isGameOver || current.isVictory) return

        var encounterDamage = 0
        var alertMessage: String? = null

        val updatedEnemies = current.currentLevel.enemies.map { enemy ->
            if (enemy.isStunned) {
                val rem = (enemy.stunRemainingTurns - 1).coerceAtLeast(0)
                enemy.copy(
                    stunRemainingTurns = rem,
                    isStunned = rem > 0,
                    lastActionText = if (rem > 0) "⚡ EMP stunned ($rem turns)" else "Rebooted! Sensors active."
                )
            } else {
                val dist = abs(enemy.x - current.playerPos.x) + abs(enemy.y - current.playerPos.y)
                val inSight = dist <= enemy.visionRange && !current.isCloaked

                when (enemy.type) {
                    EnemyType.HUNTER -> {
                        if (inSight || enemy.isAlerted) {
                            // Step 1 tile closer to player
                            val dx = when {
                                current.playerPos.x > enemy.x -> 1
                                current.playerPos.x < enemy.x -> -1
                                else -> 0
                            }
                            val dy = when {
                                current.playerPos.y > enemy.y -> 1
                                current.playerPos.y < enemy.y -> -1
                                else -> 0
                            }
                            // Prioritize the larger axis distance
                            val stepX = if (abs(current.playerPos.x - enemy.x) >= abs(current.playerPos.y - enemy.y)) dx else 0
                            val stepY = if (stepX == 0) dy else 0

                            val candX = (enemy.x + stepX).coerceIn(0, current.currentLevel.gridWidth - 1)
                            val candY = (enemy.y + stepY).coerceIn(0, current.currentLevel.gridHeight - 1)

                            val candTile = current.currentLevel.tiles.find { it.x == candX && it.y == candY }
                            val isWalkable = candTile != null && candTile.type != TileType.WALL &&
                                    !(candTile.type == TileType.LASER_GATE && !candTile.isDeactivated)

                            val finalX = if (isWalkable) candX else enemy.x
                            val finalY = if (isWalkable) candY else enemy.y

                            if (finalX == current.playerPos.x && finalY == current.playerPos.y && !current.isCloaked) {
                                val dmg = (enemy.aggression * 25).toInt().coerceAtLeast(15)
                                encounterDamage += dmg
                                alertMessage = "🚨 HUNTER DRONE AMBUSH! (-$dmg HP)"
                            }

                            enemy.copy(
                                x = finalX,
                                y = finalY,
                                isAlerted = true,
                                lastActionText = "Pursuing operative to [$finalX, $finalY]"
                            )
                        } else {
                            enemy.copy(lastActionText = "Patrolling sector...")
                        }
                    }
                    EnemyType.SENTINEL -> {
                        // High-ground turret sweeps vision
                        if (inSight && !current.isCloaked) {
                            val dmg = 10
                            encounterDamage += dmg
                            alertMessage = "⚡ SENTINEL TURRET LASER HIT! (-$dmg HP)"
                            enemy.copy(isAlerted = true, lastActionText = "Target in laser sight crosshairs!")
                        } else {
                            enemy.copy(lastActionText = "Turret sweeping grid.")
                        }
                    }
                    EnemyType.PHANTOM -> {
                        if (inSight || enemy.isAlerted) {
                            // Phase shift every turn
                            val dx = if (current.playerPos.x > enemy.x) 1 else if (current.playerPos.x < enemy.x) -1 else 0
                            val candX = (enemy.x + dx).coerceIn(0, current.currentLevel.gridWidth - 1)
                            if (candX == current.playerPos.x && enemy.y == current.playerPos.y && !current.isCloaked) {
                                encounterDamage += 15
                                alertMessage = "⚡ PHANTOM SHIFT ATTACK! (-15 HP)"
                            }
                            enemy.copy(x = candX, isAlerted = true, lastActionText = "Phase-shifted closer to [$candX, ${enemy.y}]")
                        } else enemy
                    }
                    EnemyType.STALKER -> {
                        if (inSight || enemy.isAlerted) {
                            val dy = if (current.playerPos.y > enemy.y) 1 else if (current.playerPos.y < enemy.y) -1 else 0
                            val candY = (enemy.y + dy).coerceIn(0, current.currentLevel.gridHeight - 1)
                            enemy.copy(y = candY, isAlerted = true, lastActionText = "Shadow stalker tracking [$enemy.x, $candY]")
                        } else enemy
                    }
                }
            }
        }

        val finalHealth = (current.playerHealth - encounterDamage).coerceAtLeast(0)
        _state.value = current.copy(
            playerHealth = finalHealth,
            isGameOver = finalHealth <= 0,
            currentLevel = current.currentLevel.copy(enemies = updatedEnemies),
            statusMessage = alertMessage ?: current.statusMessage
        )

        if (encounterDamage > 0) onEncounter()
    }

    fun jumpVault() {
        val current = _state.value
        if (current.isGameOver || current.isVictory) return

        // Check for adjacent ascending/descending ledges or depression pit to leap across
        val deltas = listOf(Pair(0, -1), Pair(0, 1), Pair(1, 0), Pair(-1, 0))

        // Check if adjacent tile is a depression pit; leap across by 2 tiles!
        for ((dx, dy) in deltas) {
            val pitTile = current.currentLevel.tiles.find { it.x == current.playerPos.x + dx && it.y == current.playerPos.y + dy }
            if (pitTile != null && pitTile.type == TileType.DEPRESSION_PIT) {
                val leapX = current.playerPos.x + dx * 2
                val leapY = current.playerPos.y + dy * 2
                val landingTile = current.currentLevel.tiles.find { it.x == leapX && it.y == leapY }
                if (landingTile != null && landingTile.type != TileType.WALL) {
                    _state.value = current.copy(
                        playerPos = Point3D(leapX, leapY, landingTile.z),
                        movesCount = current.movesCount + 1,
                        score = current.score + 25,
                        statusMessage = "Vaulted cleanly across hazard depression trench!"
                    )
                    return
                }
            }
        }

        // Vault up to higher elevation ledge
        for ((dx, dy) in deltas) {
            val candTile = current.currentLevel.tiles.find { it.x == current.playerPos.x + dx && it.y == current.playerPos.y + dy }
            if (candTile != null && candTile.type != TileType.WALL && candTile.z != current.playerPos.z) {
                _state.value = current.copy(
                    playerPos = Point3D(candTile.x, candTile.y, candTile.z),
                    movesCount = current.movesCount + 1,
                    score = current.score + 20,
                    statusMessage = "Scaled vertical ledge to Tier ${candTile.z}!"
                )
                return
            }
        }

        _state.value = current.copy(statusMessage = "No ledge or pit within vaulting trajectory.")
    }

    fun triggerEmpBlast(onBlast: () -> Unit) {
        val current = _state.value
        if (current.empCharges <= 0) {
            _state.value = current.copy(statusMessage = "No EMP charges remaining! Collect power cores to recharge.")
            return
        }

        // Stun all drones within 2 tiles
        var dronesStunned = 0
        val updatedEnemies = current.currentLevel.enemies.map { enemy ->
            val dist = abs(enemy.x - current.playerPos.x) + abs(enemy.y - current.playerPos.y)
            if (dist <= 2) {
                dronesStunned++
                enemy.copy(
                    isStunned = true,
                    stunRemainingTurns = 3,
                    isAlerted = false,
                    lastActionText = "⚡ EMP OVERLOAD! Systems rebooting (3 turns)"
                )
            } else enemy
        }

        _state.value = current.copy(
            empCharges = current.empCharges - 1,
            shockwaveRadius = 3f,
            score = current.score + (dronesStunned * 75),
            currentLevel = current.currentLevel.copy(enemies = updatedEnemies),
            statusMessage = "EMP SHOCKWAVE DISCHARGED! $dronesStunned hostile drone(s) disabled."
        )

        onBlast()
    }

    fun activateCloak() {
        val current = _state.value
        if (current.cloakCharges <= 0) {
            _state.value = current.copy(statusMessage = "No Cloak modules remaining.")
            return
        }
        if (current.isCloaked) {
            _state.value = current.copy(statusMessage = "Cloak already active (${current.cloakRemainingTurns} turns).")
            return
        }

        _state.value = current.copy(
            cloakCharges = current.cloakCharges - 1,
            isCloaked = true,
            cloakRemainingTurns = 3,
            statusMessage = "OPTICAL CLOAK ENGAGED! Invisible to drone vision cones for 3 turns."
        )
    }

    fun openTerminalCipher() {
        val current = _state.value
        val adjacentTerminal = current.currentLevel.terminals.find { term ->
            abs(term.x - current.playerPos.x) <= 1 && abs(term.y - current.playerPos.y) <= 1 && !term.isUnlocked
        }

        if (adjacentTerminal == null) {
            _state.value = current.copy(statusMessage = "No accessible terminal in hacking proximity.")
            return
        }

        if (current.coresCollected < adjacentTerminal.requiredCores) {
            _state.value = current.copy(
                statusMessage = "Need ${adjacentTerminal.requiredCores} energy cores to interface! (Have ${current.coresCollected})"
            )
            return
        }

        _state.value = current.copy(
            activeCipherTerminal = adjacentTerminal,
            statusMessage = "Connected to Terminal ${adjacentTerminal.id}. Align cyber cipher rings."
        )
    }

    fun solveCipherSuccess() {
        val current = _state.value
        val term = current.activeCipherTerminal ?: return

        val updatedTerminals = current.currentLevel.terminals.map {
            if (it.id == term.id) it.copy(isUnlocked = true) else it
        }

        val updatedTiles = current.currentLevel.tiles.map { t ->
            when {
                t.x == term.x && t.y == term.y -> t.copy(isDeactivated = true)
                t.type == TileType.LASER_GATE -> t.copy(isDeactivated = true)
                else -> t
            }
        }

        _state.value = current.copy(
            activeCipherTerminal = null,
            isTerminalUnlocked = true,
            score = current.score + 200,
            currentLevel = current.currentLevel.copy(tiles = updatedTiles, terminals = updatedTerminals),
            statusMessage = "CIPHER DECRYPTED! Security fences dropped. Exit Portal armed."
        )
    }

    fun closeCipherModal() {
        _state.value = _state.value.copy(activeCipherTerminal = null)
    }

    fun triggerRadarPing() {
        val current = _state.value
        _state.value = current.copy(
            isRadarActive = true,
            radarRemainingTicks = 15,
            statusMessage = "Radar ping emitted: revealing cores, portals, and drone vectors."
        )
    }

    fun updateEnemyPositions(newEnemies: List<EnemyData>, onEncounter: () -> Unit) {
        val current = _state.value
        _state.value = current.copy(
            currentLevel = current.currentLevel.copy(enemies = newEnemies)
        )
        checkDroneDetectionAndCollisions(onEncounter)
    }

    private fun checkDroneDetectionAndCollisions(onEncounter: () -> Unit) {
        val current = _state.value
        var damageTaken = 0
        var encounterText: String? = null

        val updatedEnemies = current.currentLevel.enemies.map { enemy ->
            if (enemy.isStunned) {
                val newStun = (enemy.stunRemainingTurns - 1).coerceAtLeast(0)
                enemy.copy(
                    stunRemainingTurns = newStun,
                    isStunned = newStun > 0,
                    lastActionText = if (newStun > 0) "⚡ Stunned ($newStun turns left)" else "Systems rebooted!"
                )
            } else {
                // Direct collision
                if (enemy.x == current.playerPos.x && enemy.y == current.playerPos.y) {
                    val dmg = (enemy.aggression * 25).toInt().coerceAtLeast(15)
                    damageTaken += dmg
                    encounterText = "AMBUSH! Intercepted by ${enemy.name}! (-$dmg HP)"
                }

                // Check Line of Sight (unless cloaked)
                val dist = abs(enemy.x - current.playerPos.x) + abs(enemy.y - current.playerPos.y)
                val inSight = dist <= enemy.visionRange && !current.isCloaked
                if (inSight && !enemy.isAlerted) {
                    enemy.copy(
                        isAlerted = true,
                        lastActionText = "🚨 TARGET ACQUIRED! Closing distance..."
                    )
                } else enemy
            }
        }

        val newHealth = (current.playerHealth - damageTaken).coerceAtLeast(0)
        _state.value = current.copy(
            playerHealth = newHealth,
            isGameOver = newHealth <= 0,
            currentLevel = current.currentLevel.copy(enemies = updatedEnemies),
            statusMessage = encounterText ?: current.statusMessage
        )

        if (damageTaken > 0) onEncounter()
    }

    fun rotateCamera(deltaYaw: Float) {
        val currentCam = _state.value.camera
        val newYaw = ((currentCam.yawDegrees + deltaYaw) % 360f + 360f) % 360f
        _state.value = _state.value.copy(camera = currentCam.copy(yawDegrees = newYaw))
    }

    fun setCameraPreset(pitchRatio: Float, yaw: Float) {
        val currentCam = _state.value.camera
        _state.value = _state.value.copy(camera = currentCam.copy(pitchRatio = pitchRatio, yawDegrees = yaw))
    }

    fun setCameraZoom(zoom: Float) {
        val currentCam = _state.value.camera
        val clamped = zoom.coerceIn(0.6f, 2.0f)
        _state.value = _state.value.copy(camera = currentCam.copy(zoom = clamped))
    }

    fun stepTowardAdjacentTile(targetX: Int, targetY: Int, onStepSuccess: () -> Unit, onEncounter: () -> Unit) {
        val cur = _state.value.playerPos
        val dx = targetX - cur.x
        val dy = targetY - cur.y

        val dir = when {
            dx == 1 && dy == 0 -> Direction.EAST
            dx == -1 && dy == 0 -> Direction.WEST
            dx == 0 && dy == 1 -> Direction.SOUTH
            dx == 0 && dy == -1 -> Direction.NORTH
            else -> null
        }

        if (dir != null) {
            movePlayer(dir, onStepSuccess, onEncounter)
        }
    }

    fun tickTimer() {
        val current = _state.value
        if (current.isGameOver || current.isVictory) return

        val newRemainingRadar = (current.radarRemainingTicks - 1).coerceAtLeast(0)
        _state.value = current.copy(
            elapsedSeconds = current.elapsedSeconds + 1,
            radarRemainingTicks = newRemainingRadar,
            isRadarActive = newRemainingRadar > 0
        )
    }
}
