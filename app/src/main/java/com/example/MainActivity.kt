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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewInAr
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
import com.example.ui.AppNavTab
import com.example.ui.MainViewModel
import com.example.ui.components.UpdateDialog
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HolodeckScreen
import com.example.ui.screens.MotionLabScreen
import com.example.ui.screens.SettingsScreen
import com.example.update.UpdateState
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
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val telemetry by viewModel.motionTracker.telemetry.collectAsStateWithLifecycle()
    val savedLevels by viewModel.savedLevels.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGeneratingLevel.collectAsStateWithLifecycle()
    val genStatus by viewModel.generationMessage.collectAsStateWithLifecycle()
    val apiTestResult by viewModel.apiTestResult.collectAsStateWithLifecycle()
    val updateState by viewModel.updateState.collectAsStateWithLifecycle()

    // In-App Update Dialog
    UpdateDialog(
        updateState = updateState,
        onStartDownload = { info -> viewModel.startUpdateDownload(info) },
        onInstallNow = { file -> viewModel.installDownloadedApk(file) },
        onSaveToDownloads = { file -> viewModel.saveUpdateToDownloads(file) },
        onDismiss = { viewModel.dismissUpdateDialog() }
    )

    // Back handler: pop sub-screens back to Arena
    BackHandler(enabled = currentTab != AppNavTab.ARENA) {
        viewModel.selectTab(AppNavTab.ARENA)
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
                    Spacer(modifier = Modifier.height(24.dp))

                    NavigationRailItem(
                        selected = currentTab == AppNavTab.ARENA,
                        onClick = { viewModel.selectTab(AppNavTab.ARENA) },
                        icon = { Icon(Icons.Default.SportsEsports, contentDescription = "Arena") },
                        label = { Text("Arena") },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = Color(0xFF003822),
                            selectedTextColor = CyberMintLight,
                            indicatorColor = CyberMintPrimary
                        )
                    )
                    NavigationRailItem(
                        selected = currentTab == AppNavTab.MOTION_LAB,
                        onClick = { viewModel.selectTab(AppNavTab.MOTION_LAB) },
                        icon = { Icon(Icons.Default.Sensors, contentDescription = "Motion") },
                        label = { Text("Motion") },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = Color(0xFF003822),
                            selectedTextColor = CyberMintLight,
                            indicatorColor = CyberMintPrimary
                        )
                    )
                    NavigationRailItem(
                        selected = currentTab == AppNavTab.HOLODECK,
                        onClick = { viewModel.selectTab(AppNavTab.HOLODECK) },
                        icon = { Icon(Icons.Default.ViewInAr, contentDescription = "Holodeck") },
                        label = { Text("Levels") },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = Color(0xFF003822),
                            selectedTextColor = CyberMintLight,
                            indicatorColor = CyberMintPrimary
                        )
                    )
                    NavigationRailItem(
                        selected = currentTab == AppNavTab.SETTINGS,
                        onClick = { viewModel.selectTab(AppNavTab.SETTINGS) },
                        icon = { Icon(Icons.Default.Tune, contentDescription = "Settings") },
                        label = { Text("BYOK") },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = Color(0xFF003822),
                            selectedTextColor = CyberMintLight,
                            indicatorColor = CyberMintPrimary
                        )
                    )
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    ScreenContent(
                        currentTab = currentTab,
                        viewModel = viewModel,
                        gameState = gameState,
                        telemetry = telemetry,
                        savedLevels = savedLevels,
                        settings = settings,
                        isGenerating = isGenerating,
                        genStatus = genStatus,
                        apiTestResult = apiTestResult
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
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                            }
                        },
                        actions = {
                            // Live sensor indicator badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0C241B))
                                    .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Sensors,
                                        contentDescription = null,
                                        tint = if (telemetry.isTracking) CyberMintPrimary else TextMutedDark,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${telemetry.stepCount}p • +${String.format("%.1f", telemetry.elevationGainMeters)}m",
                                        color = CyberMintLight,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (currentTab == AppNavTab.ARENA) {
                                IconButton(
                                    onClick = { viewModel.restartCurrentLevel() },
                                    modifier = Modifier.testTag("appbar_restart_level")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Restart Level",
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
                            selected = currentTab == AppNavTab.ARENA,
                            onClick = { viewModel.selectTab(AppNavTab.ARENA) },
                            icon = { Icon(Icons.Default.SportsEsports, contentDescription = "Arena") },
                            label = { Text("Arena", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF003822),
                                selectedTextColor = CyberMintLight,
                                unselectedIconColor = TextSecondaryDark,
                                unselectedTextColor = TextSecondaryDark,
                                indicatorColor = CyberMintPrimary
                            ),
                            modifier = Modifier.testTag("tab_arena")
                        )
                        NavigationBarItem(
                            selected = currentTab == AppNavTab.MOTION_LAB,
                            onClick = { viewModel.selectTab(AppNavTab.MOTION_LAB) },
                            icon = { Icon(Icons.Default.Sensors, contentDescription = "Motion") },
                            label = { Text("Motion", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF003822),
                                selectedTextColor = CyberMintLight,
                                unselectedIconColor = TextSecondaryDark,
                                unselectedTextColor = TextSecondaryDark,
                                indicatorColor = CyberMintPrimary
                            ),
                            modifier = Modifier.testTag("tab_motion")
                        )
                        NavigationBarItem(
                            selected = currentTab == AppNavTab.HOLODECK,
                            onClick = { viewModel.selectTab(AppNavTab.HOLODECK) },
                            icon = { Icon(Icons.Default.ViewInAr, contentDescription = "Holodeck") },
                            label = { Text("Levels", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF003822),
                                selectedTextColor = CyberMintLight,
                                unselectedIconColor = TextSecondaryDark,
                                unselectedTextColor = TextSecondaryDark,
                                indicatorColor = CyberMintPrimary
                            ),
                            modifier = Modifier.testTag("tab_holodeck")
                        )
                        NavigationBarItem(
                            selected = currentTab == AppNavTab.SETTINGS,
                            onClick = { viewModel.selectTab(AppNavTab.SETTINGS) },
                            icon = { Icon(Icons.Default.Tune, contentDescription = "Settings") },
                            label = { Text("BYOK", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
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
                        gameState = gameState,
                        telemetry = telemetry,
                        savedLevels = savedLevels,
                        settings = settings,
                        isGenerating = isGenerating,
                        genStatus = genStatus,
                        apiTestResult = apiTestResult
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
    gameState: com.example.game.engine.GamePlayState,
    telemetry: com.example.data.sensor.MotionTelemetry,
    savedLevels: List<com.example.data.local.entities.LevelEntity>,
    settings: com.example.data.local.entities.GameSettingsEntity,
    isGenerating: Boolean,
    genStatus: String,
    apiTestResult: String?
) {
    when (currentTab) {
        AppNavTab.ARENA -> {
            GameScreen(
                state = gameState,
                onMove = { dir -> viewModel.movePlayer(dir) },
                onJumpAscend = { viewModel.jumpAscendClimb() },
                onHack = { viewModel.hackTerminal() },
                onRadarPing = { viewModel.triggerRadarPing() },
                onRotateCamera = { delta -> viewModel.rotateCamera(delta) },
                onRestart = { viewModel.restartCurrentLevel() },
                onOpenMotionLab = { viewModel.selectTab(AppNavTab.MOTION_LAB) }
            )
        }
        AppNavTab.MOTION_LAB -> {
            MotionLabScreen(
                telemetry = telemetry,
                motionTracker = viewModel.motionTracker,
                isGenerating = isGenerating,
                generationStatus = genStatus,
                onSynthesizeLevel = { theme, diff ->
                    viewModel.generateLevelFromSensors(theme, diff)
                }
            )
        }
        AppNavTab.HOLODECK -> {
            HolodeckScreen(
                savedLevels = savedLevels,
                isGenerating = isGenerating,
                generationStatus = genStatus,
                onLoadLevel = { lvl -> viewModel.loadSavedLevel(lvl) },
                onDeleteLevel = { id -> viewModel.deleteLevel(id) },
                onGenerateCustomLevel = { theme, diff ->
                    viewModel.generateLevelFromSensors(theme, diff)
                }
            )
        }
        AppNavTab.SETTINGS -> {
            SettingsScreen(
                settings = settings,
                apiTestResult = apiTestResult,
                cachedLevelsCount = savedLevels.size,
                onSaveSettings = { newS -> viewModel.updateSettings(newS) },
                onTestApiConnection = { prov, key, mod, url ->
                    viewModel.testApiConnection(prov, key, mod, url)
                },
                onClearTestResult = { viewModel.clearApiTestResult() },
                onClearMovementLogs = { viewModel.clearMovementHistory() },
                onCheckForUpdates = { viewModel.checkForUpdates(silent = false) }
            )
        }
    }
}
