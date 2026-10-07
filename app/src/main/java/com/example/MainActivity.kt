package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.GameSettingsEntity
import com.example.data.local.entities.LevelEntity
import com.example.data.model.CardItem
import com.example.data.model.MazeBuilding
import com.example.data.model.NearbyPlayerBase
import com.example.data.model.RaidBattleState
import com.example.data.model.RadarNode
import com.example.data.sensor.MotionTelemetry
import com.example.ui.AppNavTab
import com.example.ui.MainViewModel
import com.example.ui.components.HowToPlayDialog
import com.example.ui.components.UpdateDialog
import com.example.ui.screens.MazeBaseScreen
import com.example.ui.screens.OutdoorRadarScreen
import com.example.ui.screens.RaidArenaScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TroopsDeckScreen
import com.example.ui.theme.CyberBackgroundDark
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyanAccent
import com.example.ui.theme.CyberMazeTheme
import com.example.ui.theme.CyberMintLight
import com.example.ui.theme.CyberMintPrimary
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CyberMazeTheme {
                CyberMazeApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CyberMazeApp(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val raidState by viewModel.raidBattleState.collectAsStateWithLifecycle()
    val playerBase by viewModel.playerBase.collectAsStateWithLifecycle()
    val cards by viewModel.cards.collectAsStateWithLifecycle()
    val radarNodes by viewModel.radarNodes.collectAsStateWithLifecycle()
    val userBits by viewModel.userBits.collectAsStateWithLifecycle()
    val userNanites by viewModel.userNanites.collectAsStateWithLifecycle()
    val trophies by viewModel.trophies.collectAsStateWithLifecycle()
    val telemetry by viewModel.motionTracker.telemetry.collectAsStateWithLifecycle()
    val savedLevels by viewModel.savedLevels.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val apiTestResult by viewModel.apiTestResult.collectAsStateWithLifecycle()
    val updateState by viewModel.updateState.collectAsStateWithLifecycle()

    val aiTacticalIntel by viewModel.aiTacticalIntel.collectAsStateWithLifecycle()
    val aiBaseAudit by viewModel.aiBaseAudit.collectAsStateWithLifecycle()
    val isAuditingBase by viewModel.isAuditingBase.collectAsStateWithLifecycle()
    val isHowToPlayOpen by viewModel.isHowToPlayOpen.collectAsStateWithLifecycle()
    val currentSector by viewModel.currentSector.collectAsStateWithLifecycle()
    val maxUnlockedSector by viewModel.maxUnlockedSector.collectAsStateWithLifecycle()
    val nearbyPlayerBases by viewModel.nearbyPlayerBases.collectAsStateWithLifecycle()

    // In-App GitHub Update Dialog
    UpdateDialog(
        updateState = updateState,
        onStartDownload = { info -> viewModel.startUpdateDownload(info) },
        onInstallNow = { file -> viewModel.installDownloadedApk(file) },
        onSaveToDownloads = { file -> viewModel.saveUpdateToDownloads(file) },
        onDismiss = { viewModel.dismissUpdateDialog() }
    )

    // How to Play Manual Dialog
    HowToPlayDialog(
        isOpen = isHowToPlayOpen,
        onDismiss = { viewModel.closeHowToPlay() }
    )

    // Back handler: return to Raid tab
    BackHandler(enabled = currentTab != AppNavTab.RAID) {
        viewModel.selectTab(AppNavTab.RAID)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth > 600.dp

        if (isWide) {
            // Adaptive Tablet / Foldable Landscape Layout with NavigationRail
            Row(modifier = Modifier.fillMaxSize().background(CyberBackgroundDark)) {
                NavigationRail(
                    containerColor = Color(0xFF071712),
                    contentColor = CyberMintLight,
                    modifier = Modifier.fillMaxHeight().border(1.dp, CyberCardBorder)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyberMintPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("3D", color = Color(0xFF003822), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                    NavigationRailItem(
                        selected = currentTab == AppNavTab.RAID,
                        onClick = { viewModel.selectTab(AppNavTab.RAID) },
                        icon = { Icon(Icons.Default.SportsEsports, contentDescription = "Raid") },
                        label = { Text("Raid") },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = Color(0xFF003822),
                            selectedTextColor = CyberMintLight,
                            indicatorColor = CyberMintPrimary
                        ),
                        modifier = Modifier.testTag("rail_raid")
                    )
                    NavigationRailItem(
                        selected = currentTab == AppNavTab.MAZE,
                        onClick = { viewModel.selectTab(AppNavTab.MAZE) },
                        icon = { Icon(Icons.Default.Shield, contentDescription = "My Maze") },
                        label = { Text("Base") },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = Color(0xFF003822),
                            selectedTextColor = CyberMintLight,
                            indicatorColor = CyberMintPrimary
                        ),
                        modifier = Modifier.testTag("rail_maze")
                    )
                    NavigationRailItem(
                        selected = currentTab == AppNavTab.DECK,
                        onClick = { viewModel.selectTab(AppNavTab.DECK) },
                        icon = { Icon(Icons.Default.Bolt, contentDescription = "Deck") },
                        label = { Text("Deck") },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = Color(0xFF003822),
                            selectedTextColor = CyberMintLight,
                            indicatorColor = CyberMintPrimary
                        ),
                        modifier = Modifier.testTag("rail_deck")
                    )
                    NavigationRailItem(
                        selected = currentTab == AppNavTab.OUTDOOR,
                        onClick = { viewModel.selectTab(AppNavTab.OUTDOOR) },
                        icon = { Icon(Icons.Default.Explore, contentDescription = "Radar") },
                        label = { Text("Radar") },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = Color(0xFF003822),
                            selectedTextColor = CyberMintLight,
                            indicatorColor = CyberMintPrimary
                        ),
                        modifier = Modifier.testTag("rail_radar")
                    )
                    NavigationRailItem(
                        selected = currentTab == AppNavTab.SETTINGS,
                        onClick = { viewModel.selectTab(AppNavTab.SETTINGS) },
                        icon = { Icon(Icons.Default.Tune, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = Color(0xFF003822),
                            selectedTextColor = CyberMintLight,
                            indicatorColor = CyberMintPrimary
                        ),
                        modifier = Modifier.testTag("rail_settings")
                    )
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    ScreenContent(
                        currentTab = currentTab,
                        viewModel = viewModel,
                        raidState = raidState,
                        playerBase = playerBase,
                        cards = cards,
                        radarNodes = radarNodes,
                        userBits = userBits,
                        userNanites = userNanites,
                        trophies = trophies,
                        telemetry = telemetry,
                        savedLevels = savedLevels,
                        settings = settings,
                        apiTestResult = apiTestResult,
                        aiTacticalIntel = aiTacticalIntel,
                        aiBaseAudit = aiBaseAudit,
                        isAuditingBase = isAuditingBase,
                        currentSector = currentSector,
                        maxUnlockedSector = maxUnlockedSector,
                        nearbyPlayerBases = nearbyPlayerBases
                    )
                }
            }
        } else {
            // Adaptive Mobile Portrait Layout with Bottom NavigationBar
            Scaffold(
                containerColor = CyberBackgroundDark,
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(CyberMintPrimary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "CYBERMAZE 3D",
                                    color = TextPrimaryDark,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                            }
                        },
                        actions = {
                            // How to Play Help Button
                            IconButton(
                                onClick = { viewModel.openHowToPlay() },
                                modifier = Modifier.testTag("appbar_how_to_play")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HelpOutline,
                                    contentDescription = "How to Play Guide",
                                    tint = CyberCyanAccent
                                )
                            }

                            // Currency & Trophies Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0C241B))
                                    .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "$userBits ⚡ • $trophies 🏆",
                                        color = CyberMintLight,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (currentTab == AppNavTab.RAID) {
                                IconButton(
                                    onClick = { viewModel.startRaid(currentSector) },
                                    modifier = Modifier.testTag("appbar_restart_raid")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Restart Raid",
                                        tint = CyberMintLight
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = CyberBackgroundDark,
                            titleContentColor = TextPrimaryDark
                        )
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = Color(0xFF071912),
                        contentColor = CyberMintLight,
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .border(1.dp, CyberCardBorder)
                            .testTag("main_bottom_nav")
                    ) {
                        NavigationBarItem(
                            selected = currentTab == AppNavTab.RAID,
                            onClick = { viewModel.selectTab(AppNavTab.RAID) },
                            icon = { Icon(Icons.Default.SportsEsports, contentDescription = "Raid") },
                            label = { Text("Raid", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF003822),
                                selectedTextColor = CyberMintLight,
                                unselectedIconColor = TextSecondaryDark,
                                unselectedTextColor = TextSecondaryDark,
                                indicatorColor = CyberMintPrimary
                            ),
                            modifier = Modifier.testTag("tab_raid")
                        )
                        NavigationBarItem(
                            selected = currentTab == AppNavTab.MAZE,
                            onClick = { viewModel.selectTab(AppNavTab.MAZE) },
                            icon = { Icon(Icons.Default.Shield, contentDescription = "My Maze") },
                            label = { Text("Base", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF003822),
                                selectedTextColor = CyberMintLight,
                                unselectedIconColor = TextSecondaryDark,
                                unselectedTextColor = TextSecondaryDark,
                                indicatorColor = CyberMintPrimary
                            ),
                            modifier = Modifier.testTag("tab_maze")
                        )
                        NavigationBarItem(
                            selected = currentTab == AppNavTab.DECK,
                            onClick = { viewModel.selectTab(AppNavTab.DECK) },
                            icon = { Icon(Icons.Default.Bolt, contentDescription = "Deck") },
                            label = { Text("Deck", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF003822),
                                selectedTextColor = CyberMintLight,
                                unselectedIconColor = TextSecondaryDark,
                                unselectedTextColor = TextSecondaryDark,
                                indicatorColor = CyberMintPrimary
                            ),
                            modifier = Modifier.testTag("tab_deck")
                        )
                        NavigationBarItem(
                            selected = currentTab == AppNavTab.OUTDOOR,
                            onClick = { viewModel.selectTab(AppNavTab.OUTDOOR) },
                            icon = { Icon(Icons.Default.Explore, contentDescription = "Radar") },
                            label = { Text("Radar", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF003822),
                                selectedTextColor = CyberMintLight,
                                unselectedIconColor = TextSecondaryDark,
                                unselectedTextColor = TextSecondaryDark,
                                indicatorColor = CyberMintPrimary
                            ),
                            modifier = Modifier.testTag("tab_radar")
                        )
                        NavigationBarItem(
                            selected = currentTab == AppNavTab.SETTINGS,
                            onClick = { viewModel.selectTab(AppNavTab.SETTINGS) },
                            icon = { Icon(Icons.Default.Tune, contentDescription = "Settings") },
                            label = { Text("Settings", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF003822),
                                selectedTextColor = CyberMintLight,
                                unselectedIconColor = TextSecondaryDark,
                                unselectedTextColor = TextSecondaryDark,
                                indicatorColor = CyberMintPrimary
                            ),
                            modifier = Modifier.testTag("tab_settings")
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    ScreenContent(
                        currentTab = currentTab,
                        viewModel = viewModel,
                        raidState = raidState,
                        playerBase = playerBase,
                        cards = cards,
                        radarNodes = radarNodes,
                        userBits = userBits,
                        userNanites = userNanites,
                        trophies = trophies,
                        telemetry = telemetry,
                        savedLevels = savedLevels,
                        settings = settings,
                        apiTestResult = apiTestResult,
                        aiTacticalIntel = aiTacticalIntel,
                        aiBaseAudit = aiBaseAudit,
                        isAuditingBase = isAuditingBase,
                        currentSector = currentSector,
                        maxUnlockedSector = maxUnlockedSector,
                        nearbyPlayerBases = nearbyPlayerBases
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreenContent(
    currentTab: AppNavTab,
    viewModel: MainViewModel,
    raidState: RaidBattleState,
    playerBase: List<MazeBuilding>,
    cards: List<CardItem>,
    radarNodes: List<RadarNode>,
    userBits: Int,
    userNanites: Int,
    trophies: Int,
    telemetry: MotionTelemetry,
    savedLevels: List<LevelEntity>,
    settings: GameSettingsEntity,
    apiTestResult: String?,
    aiTacticalIntel: String,
    aiBaseAudit: String,
    isAuditingBase: Boolean,
    currentSector: Int,
    maxUnlockedSector: Int,
    nearbyPlayerBases: List<NearbyPlayerBase> = emptyList()
) {
    when (currentTab) {
        AppNavTab.RAID -> {
            RaidArenaScreen(
                state = raidState,
                aiTacticalIntel = aiTacticalIntel,
                currentSector = currentSector,
                maxUnlockedSector = maxUnlockedSector,
                onDeployTroop = { type, x, y -> viewModel.deployTroop(type, x, y) },
                onCastSpell = { spell, x, y -> viewModel.castSpell(spell, x, y) },
                onStartRaidSector = { sector -> viewModel.startRaid(sector) },
                onOpenBaseEditor = { viewModel.selectTab(AppNavTab.MAZE) },
                onRequestTacticalIntel = { viewModel.requestTacticalIntel() }
            )
        }
        AppNavTab.MAZE -> {
            MazeBaseScreen(
                buildings = playerBase,
                userBits = userBits,
                aiBaseAudit = aiBaseAudit,
                isAuditing = isAuditingBase,
                onPlaceBuilding = { type, x, y -> viewModel.placeBuilding(type, x, y) },
                onRemoveBuilding = { x, y -> viewModel.removeBuilding(x, y) },
                onSimulateDefense = { viewModel.startDefenseTest() },
                onRequestAudit = { viewModel.requestBaseAudit() }
            )
        }
        AppNavTab.DECK -> {
            TroopsDeckScreen(
                cards = cards,
                userBits = userBits,
                userNanites = userNanites,
                trophies = trophies,
                onUpgradeCard = { cardId -> viewModel.upgradeCard(cardId) },
                onStartRaid = { viewModel.selectTab(AppNavTab.RAID) }
            )
        }
        AppNavTab.OUTDOOR -> {
            OutdoorRadarScreen(
                telemetry = telemetry,
                motionTracker = viewModel.motionTracker,
                radarNodes = radarNodes,
                nearbyBases = nearbyPlayerBases,
                onClaimNode = { nodeId -> viewModel.claimRadarNode(nodeId) },
                onTriggerReconDrone = { viewModel.triggerReconDrone() },
                onAttackNearbyBase = { baseId -> viewModel.attackNearbyPlayerBase(baseId) },
                onStartRaid = { viewModel.selectTab(AppNavTab.RAID) }
            )
        }
        AppNavTab.SETTINGS -> {
            SettingsScreen(
                settings = settings,
                apiTestResult = apiTestResult,
                cachedLevelsCount = savedLevels.size,
                onSaveSettings = { newS -> viewModel.saveSettings(newS) },
                onTestApiConnection = { prov, key, mod, url ->
                    viewModel.testApiConnection(prov, key, mod, url)
                },
                onClearTestResult = { viewModel.clearApiTestResult() },
                onClearMovementLogs = { viewModel.clearMovementLogs() },
                onCheckForUpdates = { viewModel.checkForUpdates(silent = false) }
            )
        }
    }
}
