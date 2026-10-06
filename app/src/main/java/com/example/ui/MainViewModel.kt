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
import com.example.data.model.CardItem
import com.example.data.model.DefenseType
import com.example.data.model.MazeBuilding
import com.example.data.model.RaidBattleState
import com.example.data.model.RadarNode
import com.example.data.model.TacticalSpell
import com.example.data.model.TroopType
import com.example.data.repository.GameRepository
import com.example.data.sensor.MotionTracker
import com.example.game.engine.SiegeEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class AppNavTab {
    RAID,       // Syndicate Raid Infiltration Arena
    MAZE,       // Player's CyberMaze Fortress Defense Builder
    DECK,       // Battle Squad Cards & Defense Upgrades
    OUTDOOR,    // Real-World Motion Sensor Radar & Geo-Vaults
    SETTINGS    // BYOK API Keys & App Updates
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
    private val _currentTab = MutableStateFlow(AppNavTab.RAID)
    val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

    // Room DB Flows
    val savedLevels: StateFlow<List<LevelEntity>> = repository.allLevels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val movementLogs: StateFlow<List<MovementLogEntity>> = repository.allMovementLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _settings = MutableStateFlow(GameSettingsEntity())
    val settings: StateFlow<GameSettingsEntity> = _settings.asStateFlow()

    // 1. Tactical Siege & Defense Engine
    private val siegeEngine = SiegeEngine()
    val raidBattleState: StateFlow<RaidBattleState> = siegeEngine.raidState
    val playerBase: StateFlow<List<MazeBuilding>> = siegeEngine.playerBase

    // 2. Player Currency & Trophies
    private val _userBits = MutableStateFlow(850)
    val userBits: StateFlow<Int> = _userBits.asStateFlow()

    private val _userNanites = MutableStateFlow(25)
    val userNanites: StateFlow<Int> = _userNanites.asStateFlow()

    private val _trophies = MutableStateFlow(120)
    val trophies: StateFlow<Int> = _trophies.asStateFlow()

    // 3. Card Deck (Troops & Defenses)
    private val _cards = MutableStateFlow(
        listOf(
            CardItem("c1", "Byte Brawler", isTroop = true, troopType = TroopType.BYTE_BRAWLER, level = 1, upgradeCostBits = 120),
            CardItem("c2", "Glitch Sprinter", isTroop = true, troopType = TroopType.GLITCH_SPRINTER, level = 1, upgradeCostBits = 100),
            CardItem("c3", "EMP Specialist", isTroop = true, troopType = TroopType.EMP_HACKER, level = 1, upgradeCostBits = 180),
            CardItem("c4", "Phantom Drone", isTroop = true, troopType = TroopType.PHANTOM_DRONE, level = 1, upgradeCostBits = 250),
            CardItem("d1", "Pulse Laser Turret", isTroop = false, defenseType = DefenseType.LASER_TURRET, level = 1, upgradeCostBits = 150),
            CardItem("d2", "Tesla Shock Pylon", isTroop = false, defenseType = DefenseType.TESLA_PYLON, level = 1, upgradeCostBits = 200),
            CardItem("d3", "Plasma Mortar", isTroop = false, defenseType = DefenseType.PLASMA_MORTAR, level = 1, upgradeCostBits = 280)
        )
    )
    val cards: StateFlow<List<CardItem>> = _cards.asStateFlow()

    // 4. Outdoor Radar Nodes
    private val _radarNodes = MutableStateFlow(
        listOf(
            RadarNode("n1", "Scout Signal Cache", distanceMeters = 80, requiredSteps = 100, bitsReward = 200, nanitesReward = 5, blueprintReward = "+10 Brawler Cards", angleDegrees = 45f),
            RadarNode("n2", "High-Altitude Relay", distanceMeters = 180, requiredSteps = 250, bitsReward = 450, nanitesReward = 10, blueprintReward = "+1 Orbital Strike", angleDegrees = 135f),
            RadarNode("n3", "Darknet Data Vault", distanceMeters = 350, requiredSteps = 500, bitsReward = 800, nanitesReward = 20, blueprintReward = "+15 Phantom Cards", angleDegrees = 220f),
            RadarNode("n4", "Apex Quantum Core", distanceMeters = 600, requiredSteps = 1000, bitsReward = 2000, nanitesReward = 50, blueprintReward = "Legendary Apex Blueprint", angleDegrees = 310f)
        )
    )
    val radarNodes: StateFlow<List<RadarNode>> = _radarNodes.asStateFlow()

    // Generation State & Feedback
    private val _isGeneratingLevel = MutableStateFlow(false)
    val isGeneratingLevel: StateFlow<Boolean> = _isGeneratingLevel.asStateFlow()

    private val _generationMessage = MutableStateFlow("")
    val generationMessage: StateFlow<String> = _generationMessage.asStateFlow()

    private val _apiTestResult = MutableStateFlow<String?>(null)
    val apiTestResult: StateFlow<String?> = _apiTestResult.asStateFlow()

    // GitHub In-App Update Manager
    val updateManager = com.example.update.UpdateManager(application, viewModelScope)
    val updateState = updateManager.updateState

    private var battleLoopJob: Job? = null
    private var lastRecordedSteps = 0

    init {
        // Load settings from Room
        viewModelScope.launch {
            val savedSettings = repository.getSettings()
            _settings.value = savedSettings
        }

        // Start motion tracking by default
        motionTracker.startTracking()

        // Start initial sector raid
        startRaid(1)

        // Start 60 FPS battle simulation loop
        startBattleSimulationLoop()

        // Monitor outdoor steps to update radar nodes and currency
        monitorOutdoorMovement()

        // Check for updates silently on launch
        updateManager.checkForUpdates(silent = true)
    }

    private fun startBattleSimulationLoop() {
        battleLoopJob?.cancel()
        battleLoopJob = viewModelScope.launch {
            var lastNanos = System.nanoTime()
            while (isActive) {
                delay(16) // ~60 FPS
                val now = System.nanoTime()
                val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0.005f, 0.05f)
                lastNanos = now

                siegeEngine.tickBattleFrame(
                    dt = dt,
                    onStructureDestroyed = { triggerHaptic(longVibe = false) },
                    onBattleEnd = { victory ->
                        triggerHaptic(longVibe = true)
                        if (victory) {
                            val st = siegeEngine.raidState.value
                            _userBits.value += st.bitsLooted
                            _trophies.value += st.trophiesWon
                        }
                    }
                )
            }
        }
    }

    private fun monitorOutdoorMovement() {
        viewModelScope.launch {
            motionTracker.telemetry.collect { telem ->
                val currentSteps = telem.stepCount
                if (currentSteps > lastRecordedSteps) {
                    val stepDelta = currentSteps - lastRecordedSteps
                    lastRecordedSteps = currentSteps

                    // Walking awards Bits
                    _userBits.value += (stepDelta * 2)

                    // Auto unlock nodes when step requirement is met
                    _radarNodes.value = _radarNodes.value.map { node ->
                        if (!node.isUnlocked && currentSteps >= node.requiredSteps) {
                            triggerHaptic(longVibe = true)
                            node.copy(isUnlocked = true)
                        } else node
                    }
                }
            }
        }
    }

    fun selectTab(tab: AppNavTab) {
        _currentTab.value = tab
    }

    // Raid Actions
    fun deployTroop(type: TroopType, x: Float, y: Float) {
        siegeEngine.deployTroop(
            type = type,
            spawnX = x,
            spawnY = y,
            onDeployed = { triggerHaptic(longVibe = false) },
            onFail = { /* feedback */ }
        )
    }

    fun castSpell(spell: TacticalSpell, x: Float, y: Float) {
        siegeEngine.castSpell(
            spell = spell,
            targetX = x,
            targetY = y,
            onCast = { triggerHaptic(longVibe = true) },
            onFail = { /* feedback */ }
        )
    }

    fun startRaid(sectorIndex: Int = 1) {
        siegeEngine.startRaidSector(sectorIndex)
        _currentTab.value = AppNavTab.RAID
    }

    fun startDefenseTest() {
        siegeEngine.startDefenseTest()
        _currentTab.value = AppNavTab.RAID
    }

    // Maze Base Builder Actions
    fun placeBuilding(type: DefenseType, gridX: Int, gridY: Int) {
        if (_userBits.value >= type.costBits) {
            val placed = siegeEngine.placeBuildingOnBase(type, gridX, gridY)
            if (placed) {
                _userBits.value -= type.costBits
                triggerHaptic(longVibe = false)
            }
        }
    }

    fun removeBuilding(gridX: Int, gridY: Int) {
        val removed = siegeEngine.removeBuildingFromBase(gridX, gridY)
        if (removed) {
            _userBits.value += 15 // Refund salvage bits
            triggerHaptic(longVibe = false)
        }
    }

    // Card Upgrades
    fun upgradeCard(cardId: String) {
        val card = _cards.value.find { it.id == cardId } ?: return
        if (_userBits.value >= card.upgradeCostBits) {
            _userBits.value -= card.upgradeCostBits
            _cards.value = _cards.value.map {
                if (it.id == cardId) {
                    it.copy(
                        level = it.level + 1,
                        upgradeCostBits = (it.upgradeCostBits * 1.75f).toInt()
                    )
                } else it
            }
            triggerHaptic(longVibe = true)
        }
    }

    // Outdoor Radar Actions
    fun claimRadarNode(nodeId: String) {
        val node = _radarNodes.value.find { it.id == nodeId } ?: return
        if (!node.isClaimed) {
            _userBits.value += node.bitsReward
            _userNanites.value += node.nanitesReward
            _radarNodes.value = _radarNodes.value.map {
                if (it.id == nodeId) it.copy(isClaimed = true) else it
            }
            triggerHaptic(longVibe = true)
        }
    }

    // Virtual Recon Drone (Indoor / emulator testing)
    fun triggerReconDrone() {
        _userBits.value += 150
        _userNanites.value += 3
        // Also unlock next available locked node
        val nextLocked = _radarNodes.value.find { !it.isUnlocked }
        if (nextLocked != null) {
            _radarNodes.value = _radarNodes.value.map {
                if (it.id == nextLocked.id) it.copy(isUnlocked = true) else it
            }
        }
        triggerHaptic(longVibe = false)
    }

    // Settings & BYOK
    fun saveSettings(newSettings: GameSettingsEntity) {
        viewModelScope.launch {
            _settings.value = newSettings
            repository.saveSettings(newSettings)
            triggerHaptic(longVibe = false)
        }
    }

    fun testApiConnection(provider: String, key: String, model: String, baseUrl: String) {
        viewModelScope.launch {
            _apiTestResult.value = "Testing link to $provider [$model]..."
            val result = aiService.testConnection(provider, key, model, baseUrl)
            _apiTestResult.value = result
            triggerHaptic(longVibe = result.startsWith("Success"))
        }
    }

    fun clearApiTestResult() {
        _apiTestResult.value = null
    }

    fun clearMovementLogs() {
        viewModelScope.launch {
            repository.clearMovementLogs()
            triggerHaptic(longVibe = false)
        }
    }

    // GitHub Updates
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

    private fun triggerHaptic(longVibe: Boolean) {
        if (!_settings.value.hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = if (longVibe) {
                    VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE)
                } else {
                    VibrationEffect.createOneShot(35, 180)
                }
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(if (longVibe) 120 else 35)
            }
        } catch (_: Exception) {}
    }
}
