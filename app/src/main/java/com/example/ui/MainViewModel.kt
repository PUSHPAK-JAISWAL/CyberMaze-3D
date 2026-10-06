package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.AiLevelService
import com.example.data.local.AppDatabase
import com.example.data.local.entities.GameSettingsEntity
import com.example.data.local.entities.LevelEntity
import com.example.data.local.entities.MovementLogEntity
import com.example.data.model.LevelData
import com.example.data.repository.GameRepository
import com.example.data.sensor.MotionTracker
import com.example.game.engine.Direction
import com.example.game.engine.GameSession
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject

enum class AppNavTab {
    ARENA,      // 3D Game Play
    MOTION_LAB, // Sensor telemetry & movement tracker
    HOLODECK,   // Level archive & AI generator
    SETTINGS    // BYOK API Keys & configurations
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = GameRepository(database)
    val motionTracker = MotionTracker(application)
    private val aiService = AiLevelService()

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    // Navigation Tab
    private val _currentTab = MutableStateFlow(AppNavTab.ARENA)
    val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

    // Room DB Flows
    val savedLevels: StateFlow<List<LevelEntity>> = repository.allLevels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val movementLogs: StateFlow<List<MovementLogEntity>> = repository.allMovementLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _settings = MutableStateFlow(GameSettingsEntity())
    val settings: StateFlow<GameSettingsEntity> = _settings.asStateFlow()

    // Active Game Session
    private var gameSession: GameSession = GameSession(
        aiService.generateProceduralLevel(
            motionTracker.telemetry.value,
            "Normal",
            "Initial Boot Sequence: Neural Arena initialized."
        )
    )
    val gameState = MutableStateFlow(gameSession.state.value)

    // Generation State
    private val _isGeneratingLevel = MutableStateFlow(false)
    val isGeneratingLevel: StateFlow<Boolean> = _isGeneratingLevel.asStateFlow()

    private val _generationMessage = MutableStateFlow("")
    val generationMessage: StateFlow<String> = _generationMessage.asStateFlow()

    // Test connection feedback
    private val _apiTestResult = MutableStateFlow<String?>(null)
    val apiTestResult: StateFlow<String?> = _apiTestResult.asStateFlow()

    // In-App GitHub Update Manager
    val updateManager = com.example.update.UpdateManager(application, viewModelScope)
    val updateState = updateManager.updateState

    private var gameLoopJob: Job? = null
    private var enemyAiLoopJob: Job? = null

    init {
        // Load settings from Room
        viewModelScope.launch {
            val savedSettings = repository.getSettings()
            _settings.value = savedSettings
        }

        // Start motion tracking by default
        motionTracker.startTracking()

        // Start game tick loop
        startGameLoops()

        // Check for updates silently on launch
        updateManager.checkForUpdates(silent = true)
    }

    fun checkForUpdates(silent: Boolean = false) {
        updateManager.checkForUpdates(silent)
    }

    fun startUpdateDownload(info: com.example.update.UpdateInfo) {
        updateManager.startDownload(info)
    }

    fun installDownloadedApk(file: java.io.File) {
        updateManager.retryInstall(file)
    }

    fun saveUpdateToDownloads(file: java.io.File) {
        updateManager.exportToDownloads(file)
    }

    fun dismissUpdateDialog() {
        updateManager.dismissUpdate()
    }

    fun selectTab(tab: AppNavTab) {
        _currentTab.value = tab
    }

    private fun startGameLoops() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                gameSession.tickTimer()
                gameState.value = gameSession.state.value

                // If won, save score to Room
                val current = gameState.value
                if (current.isVictory && current.currentLevel.id > 0) {
                    repository.markLevelCleared(current.currentLevel.id, current.elapsedSeconds, 3)
                }
            }
        }

        // Dynamic Enemy Tactical Comms (runs periodically for radio chatter & LLM telemetry)
        enemyAiLoopJob?.cancel()
        enemyAiLoopJob = viewModelScope.launch {
            var turn = 0
            while (isActive) {
                delay(5000)
                val current = gameState.value
                if (_settings.value.dynamicAiEnemyEnabled && !current.isGameOver && !current.isVictory && current.currentLevel.enemies.isNotEmpty()) {
                    turn++
                    try {
                        val leadEnemy = current.currentLevel.enemies.first()
                        val (_, dialogue) = aiService.queryEnemyTacticalBehavior(
                            _settings.value,
                            leadEnemy,
                            current.playerPos,
                            turn
                        )
                        val updated = current.currentLevel.enemies.mapIndexed { i, e ->
                            if (i == 0) e.copy(lastActionText = dialogue) else e
                        }
                        gameSession.updateEnemyPositions(updated) {}
                        gameState.value = gameSession.state.value
                    } catch (_: Exception) {
                        // Keep current positions smoothly
                    }
                }
            }
        }
    }

    fun stepTowardAdjacentTile(targetX: Int, targetY: Int) {
        gameSession.stepTowardAdjacentTile(
            targetX = targetX,
            targetY = targetY,
            onStepSuccess = { triggerHaptic(longVibe = false) },
            onEncounter = { triggerHaptic(longVibe = true) }
        )
        gameState.value = gameSession.state.value
    }

    fun movePlayer(direction: Direction) {
        gameSession.movePlayer(
            direction = direction,
            onStepSuccess = { triggerHaptic(longVibe = false) },
            onEncounter = { triggerHaptic(longVibe = true) }
        )
        gameState.value = gameSession.state.value
    }

    fun jumpVault() {
        gameSession.jumpVault()
        gameState.value = gameSession.state.value
        triggerHaptic(longVibe = false)
    }

    fun triggerEmpBlast() {
        gameSession.triggerEmpBlast {
            triggerHaptic(longVibe = true)
        }
        gameState.value = gameSession.state.value
    }

    fun activateCloak() {
        gameSession.activateCloak()
        gameState.value = gameSession.state.value
        triggerHaptic(longVibe = false)
    }

    fun openTerminalCipher() {
        gameSession.openTerminalCipher()
        gameState.value = gameSession.state.value
    }

    fun solveCipherSuccess() {
        gameSession.solveCipherSuccess()
        gameState.value = gameSession.state.value
        triggerHaptic(longVibe = true)
    }

    fun closeCipherModal() {
        gameSession.closeCipherModal()
        gameState.value = gameSession.state.value
    }

    fun triggerRadarPing() {
        gameSession.triggerRadarPing()
        gameState.value = gameSession.state.value
        triggerHaptic(longVibe = false)
    }

    fun rotateCamera(deltaDegrees: Float) {
        gameSession.rotateCamera(deltaDegrees)
        gameState.value = gameSession.state.value
    }

    fun setCameraPreset(pitchRatio: Float, yaw: Float) {
        gameSession.setCameraPreset(pitchRatio, yaw)
        gameState.value = gameSession.state.value
    }

    fun setCameraZoom(zoom: Float) {
        gameSession.setCameraZoom(zoom)
        gameState.value = gameSession.state.value
    }

    fun restartCurrentLevel() {
        gameSession.resetLevel()
        gameState.value = gameSession.state.value
    }

    fun generateLevelFromSensors(themeTitle: String, difficulty: String) {
        viewModelScope.launch {
            _isGeneratingLevel.value = true
            _generationMessage.value = "Translating mobile sensor vectors (elevation + depression + steps)..."

            val telemetry = motionTracker.telemetry.value

            // 1. Log motion session to Room
            val logEntity = MovementLogEntity(
                durationSeconds = telemetry.durationSeconds,
                steps = telemetry.stepCount,
                elevationGainMeters = telemetry.elevationGainMeters,
                depressionMeters = telemetry.depressionMeters,
                forwardDistanceMeters = telemetry.forwardDistanceMeters,
                lateralDistanceMeters = telemetry.lateralDistanceMeters,
                totalDistanceMeters = telemetry.totalDistanceMeters
            )
            repository.logMovementSession(logEntity)

            delay(600)
            _generationMessage.value = if (_settings.value.apiKey.isNotBlank()) {
                "Invoking LLM (${_settings.value.apiProvider} / ${_settings.value.modelId}) with movement data..."
            } else {
                "Synthesizing 3D cyber labyrinth via neural procedural engine..."
            }

            val result = aiService.generateLevelWithAiOrFallback(
                settings = _settings.value,
                telemetry = telemetry,
                themeTitle = themeTitle,
                difficulty = difficulty
            )

            result.onSuccess { generatedLevel ->
                // Cache level into Room database
                val json = serializeLevelToJson(generatedLevel)
                val levelEntity = LevelEntity(
                    title = generatedLevel.name,
                    description = generatedLevel.description,
                    levelJson = json,
                    stepCountSource = telemetry.stepCount,
                    elevationGainSource = telemetry.elevationGainMeters,
                    depressionSource = telemetry.depressionMeters,
                    distanceSourceMeters = telemetry.totalDistanceMeters,
                    providerUsed = if (_settings.value.apiKey.isNotBlank()) _settings.value.apiProvider else "PROCEDURAL",
                    modelUsed = if (_settings.value.apiKey.isNotBlank()) _settings.value.modelId else "Local Synthesizer",
                    difficulty = difficulty
                )
                val savedId = repository.saveLevel(levelEntity)
                val levelWithId = generatedLevel.copy(id = savedId)

                // Load into game session
                gameSession = GameSession(levelWithId)
                gameState.value = gameSession.state.value
                _currentTab.value = AppNavTab.ARENA
                _generationMessage.value = "Level generation complete! Infiltrating sector..."
                triggerHaptic(longVibe = false)
            }.onFailure { err ->
                _generationMessage.value = "Error: ${err.localizedMessage}. Using offline fallback."
            }

            delay(500)
            _isGeneratingLevel.value = false
        }
    }

    fun loadSavedLevel(entity: LevelEntity) {
        val parsed = aiService.parseJsonToLevel(entity.levelJson, motionTracker.telemetry.value)
        val levelWithId = parsed.copy(
            id = entity.id,
            name = entity.title,
            description = entity.description
        )
        gameSession = GameSession(levelWithId)
        gameState.value = gameSession.state.value
        _currentTab.value = AppNavTab.ARENA
    }

    fun deleteLevel(id: Long) {
        viewModelScope.launch {
            repository.deleteLevel(id)
        }
    }

    fun updateSettings(newSettings: GameSettingsEntity) {
        _settings.value = newSettings
        viewModelScope.launch {
            repository.saveSettings(newSettings)
        }
    }

    fun testApiConnection(provider: String, apiKey: String, model: String, customBaseUrl: String) {
        viewModelScope.launch {
            _apiTestResult.value = "Testing connection to $provider..."
            val testSettings = GameSettingsEntity(
                apiProvider = provider,
                apiKey = apiKey,
                modelId = model,
                customBaseUrl = customBaseUrl
            )
            val telemetry = motionTracker.telemetry.value
            try {
                val res = aiService.generateLevelWithAiOrFallback(testSettings, telemetry, "Ping Test", "Normal")
                if (res.isSuccess) {
                    _apiTestResult.value = "Success! Model responded and generated sector: '${res.getOrNull()?.name}'"
                } else {
                    _apiTestResult.value = "Failed: ${res.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _apiTestResult.value = "Connection error: ${e.message}"
            }
        }
    }

    fun clearApiTestResult() {
        _apiTestResult.value = null
    }

    fun clearMovementHistory() {
        viewModelScope.launch {
            repository.clearMovementLogs()
            motionTracker.resetSession()
        }
    }

    private fun triggerHaptic(longVibe: Boolean) {
        if (!_settings.value.hapticsEnabled || vibrator == null) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = if (longVibe) {
                    VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE)
                } else {
                    VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE)
                }
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(if (longVibe) 120L else 35L)
            }
        } catch (_: Exception) {}
    }

    private fun serializeLevelToJson(level: LevelData): String {
        val root = JSONObject()
        root.put("levelName", level.name)
        root.put("description", level.description)
        root.put("gridSize", level.gridWidth)
        root.put("aiBriefing", level.aiBriefing)

        val elevArr = org.json.JSONArray()
        val depArr = org.json.JSONArray()
        val wallArr = org.json.JSONArray()
        val coresArr = org.json.JSONArray()

        level.tiles.forEach { t ->
            if (t.z > 0) {
                val o = JSONObject().apply {
                    put("x", t.x)
                    put("y", t.y)
                    put("z", t.z)
                }
                elevArr.put(o)
            }
            if (t.type == com.example.data.model.TileType.DEPRESSION_PIT) {
                val o = JSONObject().apply {
                    put("x", t.x)
                    put("y", t.y)
                }
                depArr.put(o)
            }
            if (t.type == com.example.data.model.TileType.WALL) {
                val o = JSONObject().apply {
                    put("x", t.x)
                    put("y", t.y)
                }
                wallArr.put(o)
            }
            if (t.type == com.example.data.model.TileType.POWER_CORE) {
                val o = JSONObject().apply {
                    put("x", t.x)
                    put("y", t.y)
                    put("z", t.z)
                }
                coresArr.put(o)
            }
        }

        root.put("elevatedCoords", elevArr)
        root.put("depressionCoords", depArr)
        root.put("wallCoords", wallArr)
        root.put("powerCores", coresArr)

        val spawnObj = JSONObject().apply {
            put("x", level.playerSpawn.x)
            put("y", level.playerSpawn.y)
            put("z", level.playerSpawn.z)
        }
        root.put("playerSpawn", spawnObj)

        val exitObj = JSONObject().apply {
            put("x", level.exitPortal.x)
            put("y", level.exitPortal.y)
            put("z", level.exitPortal.z)
        }
        root.put("exitPortal", exitObj)

        val enemiesArr = org.json.JSONArray()
        level.enemies.forEach { e ->
            val eo = JSONObject().apply {
                put("id", e.id)
                put("name", e.name)
                put("type", e.type.name)
                put("x", e.x)
                put("y", e.y)
                put("z", e.z)
                put("aggression", e.aggression.toDouble())
                put("behaviorDescription", e.behaviorDescription)
                put("dialogue", e.lastActionText)
            }
            enemiesArr.put(eo)
        }
        root.put("enemies", enemiesArr)

        val termsArr = org.json.JSONArray()
        level.terminals.forEach { term ->
            val to = JSONObject().apply {
                put("id", term.id)
                put("x", term.x)
                put("y", term.y)
                put("z", term.z)
                put("requiredCores", term.requiredCores)
                put("securityLevel", term.securityLevel)
                put("puzzlePrompt", term.puzzlePrompt)
            }
            termsArr.put(to)
        }
        root.put("terminals", termsArr)

        return root.toString()
    }
}
