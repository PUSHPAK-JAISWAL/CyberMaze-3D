package com.example.game.engine

import com.example.data.model.EnemyData
import com.example.data.model.LevelData
import com.example.data.model.LevelTile
import com.example.data.model.Point3D
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
    val movesCount: Int = 0,
    val elapsedSeconds: Int = 0,
    val isTerminalUnlocked: Boolean = false,
    val isGameOver: Boolean = false,
    val isVictory: Boolean = false,
    val isRadarActive: Boolean = false,
    val radarRemainingTicks: Int = 0,
    val statusMessage: String = "Infiltrate cyber grid. Collect energy cores to hack exit terminal.",
    val camera: Camera3D = Camera3D(),
    val dynamicAiLog: String = "AI threat models standing by."
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
        // Reset collected status on tiles
        val freshTiles = level.tiles.map { it.copy(isCollected = false, isDeactivated = false) }
        val freshEnemies = level.enemies.map { it.copy(hp = 100, lastActionText = "Scanning sector...") }
        val freshLevel = level.copy(tiles = freshTiles, enemies = freshEnemies)

        _state.value = GamePlayState(
            currentLevel = freshLevel,
            playerPos = freshLevel.playerSpawn,
            totalCoresInLevel = freshTiles.count { it.type == TileType.POWER_CORE }
        )
    }

    fun movePlayer(direction: Direction, onStepSuccess: () -> Unit, onEncounter: () -> Unit) {
        val current = _state.value
        if (current.isGameOver || current.isVictory) return

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
            _state.value = current.copy(statusMessage = "Grid boundary reached. Trajectory blocked.")
            return
        }

        // Find target tile
        val targetTile = current.currentLevel.tiles.find { it.x == targetX && it.y == targetY } ?: return

        // Collision check
        if (targetTile.type == TileType.WALL) {
            _state.value = current.copy(statusMessage = "Solid cyber barrier! Find another path.")
            return
        }

        // Elevation elevation difference check: can step $\pm 1$, but cliff $>1$ requires jump/ascend ability
        val elevDiff = targetTile.z - current.playerPos.z
        if (elevDiff > 1) {
            _state.value = current.copy(
                statusMessage = "Elevation cliff too steep (+${elevDiff}m)! Use Ascend Jump or find ramp."
            )
            return
        }

        // Depression pit hazard warning
        var damageTaken = 0
        var status = "Moved to coordinates [$targetX, $targetY]"
        if (targetTile.type == TileType.DEPRESSION_PIT) {
            damageTaken = 15
            status = "Fell into sensory depression pit! Took 15 hazard damage."
        }

        // Power core collection
        var newCores = current.coresCollected
        val updatedTiles = current.currentLevel.tiles.map { t ->
            if (t.x == targetX && t.y == targetY && t.type == TileType.POWER_CORE && !t.isCollected) {
                newCores += 1
                status = "Energy Core collected! ($newCores/${current.totalCoresInLevel})"
                t.copy(isCollected = true)
            } else t
        }

        val newPos = Point3D(targetX, targetY, targetTile.z)
        val newHealth = (current.playerHealth - damageTaken).coerceAtLeast(0)

        // Check Victory at Exit Portal
        val reachedExit = targetX == current.currentLevel.exitPortal.x && targetY == current.currentLevel.exitPortal.y
        var isVictory = false
        if (reachedExit) {
            if (newCores >= current.totalCoresInLevel || current.isTerminalUnlocked) {
                isVictory = true
                status = "MISSION ACCOMPLISHED! Labyrinth bypassed."
            } else {
                status = "Portal locked! Collect all cores or hack terminal first."
            }
        }

        val updatedLevel = current.currentLevel.copy(tiles = updatedTiles)

        _state.value = current.copy(
            currentLevel = updatedLevel,
            playerPos = newPos,
            playerHealth = newHealth,
            coresCollected = newCores,
            movesCount = current.movesCount + 1,
            isGameOver = newHealth <= 0,
            isVictory = isVictory,
            statusMessage = status
        )

        onStepSuccess()

        // Check enemy collision immediately
        checkEnemyCollisions(onEncounter)
    }

    fun jumpAscendClimb() {
        val current = _state.value
        if (current.isGameOver || current.isVictory) return

        // Look at all adjacent tiles to find an elevated ledge (+1 or +2)
        val candidates = listOf(
            Point3D(current.playerPos.x + 1, current.playerPos.y, 0),
            Point3D(current.playerPos.x - 1, current.playerPos.y, 0),
            Point3D(current.playerPos.x, current.playerPos.y + 1, 0),
            Point3D(current.playerPos.x, current.playerPos.y - 1, 0)
        )

        for (cand in candidates) {
            val tile = current.currentLevel.tiles.find { it.x == cand.x && it.y == cand.y }
            if (tile != null && tile.type != TileType.WALL && tile.z > current.playerPos.z) {
                // Leap onto elevated ledge
                _state.value = current.copy(
                    playerPos = Point3D(tile.x, tile.y, tile.z),
                    movesCount = current.movesCount + 1,
                    statusMessage = "Ascended to tier ${tile.z} elevation platform!"
                )
                return
            }
        }

        _state.value = current.copy(statusMessage = "No ascending ledge within leap range.")
    }

    fun hackTerminal() {
        val current = _state.value
        if (current.isGameOver || current.isVictory) return

        // Look for adjacent or current terminal
        val adjacentTerminal = current.currentLevel.terminals.find { term ->
            abs(term.x - current.playerPos.x) <= 1 && abs(term.y - current.playerPos.y) <= 1 && !term.isUnlocked
        }

        if (adjacentTerminal == null) {
            _state.value = current.copy(statusMessage = "No accessible terminal in hacking proximity.")
            return
        }

        if (current.coresCollected < adjacentTerminal.requiredCores) {
            _state.value = current.copy(
                statusMessage = "Insufficient energy cores! Requires ${adjacentTerminal.requiredCores} (Have ${current.coresCollected})."
            )
            return
        }

        // Unlock terminal & disable nearest security laser
        val updatedTerminals = current.currentLevel.terminals.map {
            if (it.id == adjacentTerminal.id) it.copy(isUnlocked = true) else it
        }

        val updatedTiles = current.currentLevel.tiles.map {
            if (it.x == adjacentTerminal.x && it.y == adjacentTerminal.y) {
                it.copy(isDeactivated = true)
            } else it
        }

        _state.value = current.copy(
            currentLevel = current.currentLevel.copy(tiles = updatedTiles, terminals = updatedTerminals),
            isTerminalUnlocked = true,
            statusMessage = "CYBER LOCK DECRYPTED! Exit Portal protocols activated."
        )
    }

    fun triggerRadarPing() {
        val current = _state.value
        _state.value = current.copy(
            isRadarActive = true,
            radarRemainingTicks = 15,
            statusMessage = "Radar ping emitted: revealing cores, portals & enemy vectors."
        )
    }

    fun updateEnemyPositions(newEnemies: List<EnemyData>, onEncounter: () -> Unit) {
        val current = _state.value
        _state.value = current.copy(
            currentLevel = current.currentLevel.copy(enemies = newEnemies)
        )
        checkEnemyCollisions(onEncounter)
    }

    private fun checkEnemyCollisions(onEncounter: () -> Unit) {
        val current = _state.value
        val collidingEnemy = current.currentLevel.enemies.find {
            it.x == current.playerPos.x && it.y == current.playerPos.y
        }

        if (collidingEnemy != null) {
            val dmg = (collidingEnemy.aggression * 30).toInt().coerceAtLeast(15)
            val newHealth = (current.playerHealth - dmg).coerceAtLeast(0)
            _state.value = current.copy(
                playerHealth = newHealth,
                isGameOver = newHealth <= 0,
                statusMessage = "ALERT! Ambushed by ${collidingEnemy.name}! Suffered $dmg cyber damage."
            )
            onEncounter()
        }
    }

    fun rotateCamera(deltaYaw: Float) {
        val currentCam = _state.value.camera
        val newYaw = ((currentCam.yawDegrees + deltaYaw) % 360f + 360f) % 360f
        _state.value = _state.value.copy(camera = currentCam.copy(yawDegrees = newYaw))
    }

    fun setCameraZoom(zoom: Float) {
        val currentCam = _state.value.camera
        _state.value = _state.value.copy(camera = currentCam.copy(zoom = zoom.coerceIn(0.6f, 2.2f)))
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
