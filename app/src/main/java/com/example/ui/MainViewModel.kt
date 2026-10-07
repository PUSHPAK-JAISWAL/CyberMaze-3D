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
import com.example.data.model.NearbyPlayerBase
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

    // Sector / Level Progression
    private val _currentSector = MutableStateFlow(1)
    val currentSector: StateFlow<Int> = _currentSector.asStateFlow()

    private val _maxUnlockedSector = MutableStateFlow(1)
    val maxUnlockedSector: StateFlow<Int> = _maxUnlockedSector.asStateFlow()

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

    // 5. Pokemon GO Proximity Player Bases (Nearby Syndicate Architects detected on Geo-Radar)
    private val _nearbyPlayerBases = MutableStateFlow(
        listOf(
            NearbyPlayerBase(
                id = "p1",
                architectName = "Kira_Zero",
                rankTitle = "Apex Syndicate",
                distanceMeters = 42,
                angleDegrees = 85f,
                trophyCount = 480,
                lootableBits = 350,
                buildings = listOf(
                    MazeBuilding(901, DefenseType.CORE_SERVER, 4, 4, 1500f, 1500f),
                    MazeBuilding(902, DefenseType.NEON_WALL, 3, 2, 600f, 600f),
                    MazeBuilding(903, DefenseType.NEON_WALL, 4, 2, 600f, 600f),
                    MazeBuilding(904, DefenseType.NEON_WALL, 5, 2, 600f, 600f),
                    MazeBuilding(905, DefenseType.NEON_WALL, 2, 4, 600f, 600f),
                    MazeBuilding(906, DefenseType.NEON_WALL, 6, 4, 600f, 600f),
                    MazeBuilding(907, DefenseType.LASER_TURRET, 3, 3, 400f, 400f),
                    MazeBuilding(908, DefenseType.TESLA_PYLON, 5, 3, 380f, 380f),
                    MazeBuilding(909, DefenseType.PLASMA_MORTAR, 4, 6, 420f, 420f),
                    MazeBuilding(910, DefenseType.GLITCH_MINE, 4, 3, 80f, 80f)
                )
            ),
            NearbyPlayerBase(
                id = "p2",
                architectName = "Ghost_Byte",
                rankTitle = "Hacker Elite",
                distanceMeters = 115,
                angleDegrees = 195f,
                trophyCount = 310,
                lootableBits = 260,
                buildings = listOf(
                    MazeBuilding(911, DefenseType.CORE_SERVER, 4, 4, 1300f, 1300f),
                    MazeBuilding(912, DefenseType.NEON_WALL, 3, 3, 500f, 500f),
                    MazeBuilding(913, DefenseType.NEON_WALL, 5, 3, 500f, 500f),
                    MazeBuilding(914, DefenseType.NEON_WALL, 3, 5, 500f, 500f),
                    MazeBuilding(915, DefenseType.NEON_WALL, 5, 5, 500f, 500f),
                    MazeBuilding(916, DefenseType.LASER_TURRET, 4, 2, 350f, 350f),
                    MazeBuilding(917, DefenseType.TESLA_PYLON, 4, 6, 320f, 320f),
                    MazeBuilding(918, DefenseType.GLITCH_MINE, 2, 4, 80f, 80f)
                )
            ),
            NearbyPlayerBase(
                id = "p3",
                architectName = "Valkyrie_99",
                rankTitle = "Cyber Sentinel",
                distanceMeters = 240,
                angleDegrees = 290f,
                trophyCount = 590,
                lootableBits = 520,
                buildings = listOf(
                    MazeBuilding(921, DefenseType.CORE_SERVER, 4, 4, 1800f, 1800f),
                    MazeBuilding(922, DefenseType.NEON_WALL, 2, 2, 700f, 700f),
                    MazeBuilding(923, DefenseType.NEON_WALL, 6, 2, 700f, 700f),
                    MazeBuilding(924, DefenseType.NEON_WALL, 2, 6, 700f, 700f),
                    MazeBuilding(925, DefenseType.NEON_WALL, 6, 6, 700f, 700f),
                    MazeBuilding(926, DefenseType.PLASMA_MORTAR, 3, 3, 450f, 450f),
                    MazeBuilding(927, DefenseType.PLASMA_MORTAR, 5, 3, 450f, 450f),
                    MazeBuilding(928, DefenseType.LASER_TURRET, 3, 5, 420f, 420f),
                    MazeBuilding(929, DefenseType.TESLA_PYLON, 5, 5, 400f, 400f)
                )
            )
        )
    )
    val nearbyPlayerBases: StateFlow<List<NearbyPlayerBase>> = _nearbyPlayerBases.asStateFlow()

    // Generation State & Feedback
    private val _isGeneratingLevel = MutableStateFlow(false)
    val isGeneratingLevel: StateFlow<Boolean> = _isGeneratingLevel.asStateFlow()

    private val _generationMessage = MutableStateFlow("")
    val generationMessage: StateFlow<String> = _generationMessage.asStateFlow()

    private val _apiTestResult = MutableStateFlow<String?>(null)
    val apiTestResult: StateFlow<String?> = _apiTestResult.asStateFlow()

    // AI Tactical Intel & Security Audits
    private val _aiTacticalIntel = MutableStateFlow<String>("🛰️ AI OPERATOR: Tap 'SCAN INTEL' for tactical breach analysis.")
    val aiTacticalIntel: StateFlow<String> = _aiTacticalIntel.asStateFlow()

    private val _aiBaseAudit = MutableStateFlow<String>("")
    val aiBaseAudit: StateFlow<String> = _aiBaseAudit.asStateFlow()

    private val _isAuditingBase = MutableStateFlow<Boolean>(false)
    val isAuditingBase: StateFlow<Boolean> = _isAuditingBase.asStateFlow()

    // How to Play Dialog State
    private val _isHowToPlayOpen = MutableStateFlow<Boolean>(false)
    val isHowToPlayOpen: StateFlow<Boolean> = _isHowToPlayOpen.asStateFlow()

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

                            // Unlock the next sector level upon victory
                            val nextSec = _currentSector.value + 1
                            if (nextSec > _maxUnlockedSector.value) {
                                _maxUnlockedSector.value = nextSec.coerceAtMost(12)
                            }
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

    fun openHowToPlay() {
        _isHowToPlayOpen.value = true
        triggerHaptic(longVibe = false)
    }

    fun closeHowToPlay() {
        _isHowToPlayOpen.value = false
    }

    fun requestTacticalIntel() {
        viewModelScope.launch {
            val state = raidBattleState.value
            val turrets = state.buildings.count { !it.type.isWall && !it.type.isCore }
            val core = state.buildings.find { it.type == DefenseType.CORE_SERVER }
            val coreHp = core?.currentHp ?: 1000f

            _aiTacticalIntel.value = "🛰️ Querying Syndicate Recon Satellite..."
            val intel = aiService.generateTacticalIntel(
                settings = _settings.value,
                sectorName = state.sectorName,
                difficulty = state.sectorDifficulty,
                turretsCount = turrets,
                coreHp = coreHp
            )
            _aiTacticalIntel.value = intel
            triggerHaptic(longVibe = false)
        }
    }

    fun requestBaseAudit() {
        viewModelScope.launch {
            _isAuditingBase.value = true
            val buildings = playerBase.value
            val walls = buildings.count { it.type == DefenseType.NEON_WALL }
            val turrets = buildings.count { !it.type.isWall && !it.type.isCore }
            val coreProtected = walls >= 6

            _aiBaseAudit.value = "🔍 Neural Defense Auditor scanning maze perimeter..."
            val audit = aiService.auditBaseDefense(
                settings = _settings.value,
                wallCount = walls,
                turretCount = turrets,
                coreProtected = coreProtected
            )
            _aiBaseAudit.value = audit
            _isAuditingBase.value = false
            triggerHaptic(longVibe = true)
        }
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
        _currentSector.value = sectorIndex
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

    // Attack nearby player's base (Pokemon GO style proximity attack)
    fun attackNearbyPlayerBase(baseId: String) {
        val base = _nearbyPlayerBases.value.find { it.id == baseId } ?: return
        siegeEngine.startNearbyPlayerRaid(base)
        _currentTab.value = AppNavTab.RAID
        triggerHaptic(longVibe = true)
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
