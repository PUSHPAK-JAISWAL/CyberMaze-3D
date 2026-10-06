package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.Direction
import com.example.game.engine.Game3DRenderer
import com.example.game.engine.GamePlayState
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberPillButton
import com.example.ui.theme.CyberAmberWarning
import com.example.ui.theme.CyberBackgroundDark
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyanAccent
import com.example.ui.theme.CyberLaserRed
import com.example.ui.theme.CyberMintDark
import com.example.ui.theme.CyberMintLight
import com.example.ui.theme.CyberMintPrimary
import com.example.ui.theme.CyberSurfaceCard
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    state: GamePlayState,
    onMove: (Direction) -> Unit,
    onJumpAscend: () -> Unit,
    onHack: () -> Unit,
    onRadarPing: () -> Unit,
    onRotateCamera: (Float) -> Unit,
    onRestart: () -> Unit,
    onOpenMotionLab: () -> Unit
) {
    val renderer = remember { Game3DRenderer() }
    var animTicks by remember { mutableLongStateOf(0L) }

    // Continuous 60fps render tick for 3D spinning portal, bobbing drone, and neon pulses
    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            animTicks++
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackgroundDark)
            .testTag("game_screen_root")
    ) {
        val isLandscapeOrWide = maxWidth > 600.dp

        if (isLandscapeOrWide) {
            // Tablet / Landscape Split Layout
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Pane: 3D Viewport
                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxSize()
                ) {
                    GameCanvas3D(
                        renderer = renderer,
                        state = state,
                        animTicks = animTicks,
                        onRotateCamera = onRotateCamera
                    )
                    TopGameHud(
                        state = state,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp)
                    )
                }

                // Right Pane: Control Deck
                Column(
                    modifier = Modifier
                        .weight(1.0f)
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    StatusTicker(state = state)
                    VirtualControlDeck(
                        onMove = onMove,
                        onJump = onJumpAscend,
                        onHack = onHack,
                        onRadar = onRadarPing,
                        onRotate = onRotateCamera
                    )
                }
            }
        } else {
            // Mobile Portrait Adaptive Layout (dynamically responsive)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Top HUD
                TopGameHud(state = state)

                Spacer(modifier = Modifier.height(8.dp))

                // 3D Canvas Box taking adaptive proportional height
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.2f)
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.5.dp, CyberCardBorder, RoundedCornerShape(24.dp))
                ) {
                    GameCanvas3D(
                        renderer = renderer,
                        state = state,
                        animTicks = animTicks,
                        onRotateCamera = onRotateCamera
                    )

                    // Quick Camera Orbit Toolbar (Floating overlay)
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x99000000))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onRotateCamera(-45f) },
                            modifier = Modifier.size(32.dp).testTag("rotate_cam_left")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RotateRight,
                                contentDescription = "Orbit Left",
                                tint = CyberMintLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "${state.camera.yawDegrees.toInt()}°",
                            color = CyberMintLight,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { onRotateCamera(45f) },
                            modifier = Modifier.size(32.dp).testTag("rotate_cam_right")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RotateRight,
                                contentDescription = "Orbit Right",
                                tint = CyberMintLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Tactical Enemy Threat Warning Chip (Floating bottom left)
                    val nearestEnemy = state.currentLevel.enemies.firstOrNull()
                    if (nearestEnemy != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xCC1A050A))
                                .border(1.dp, CyberLaserRed.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(CyberLaserRed)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${nearestEnemy.name}: ${nearestEnemy.lastActionText}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tactical Mission Ticker
                StatusTicker(state = state)

                Spacer(modifier = Modifier.height(8.dp))

                // Adaptive Virtual Controller Deck
                VirtualControlDeck(
                    onMove = onMove,
                    onJump = onJumpAscend,
                    onHack = onHack,
                    onRadar = onRadarPing,
                    onRotate = onRotateCamera,
                    modifier = Modifier.weight(0.9f)
                )
            }
        }

        // Victory / Game Over Overlay Modal
        AnimatedVisibility(
            visible = state.isVictory || state.isGameOver,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            GameOverModal(
                state = state,
                onRestart = onRestart,
                onOpenMotionLab = onOpenMotionLab
            )
        }
    }
}

@Composable
private fun GameCanvas3D(
    renderer: Game3DRenderer,
    state: GamePlayState,
    animTicks: Long,
    onRotateCamera: (Float) -> Unit
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    // Drag horizontally to orbit 3D camera
                    if (dragAmount.x != 0f) {
                        onRotateCamera(dragAmount.x * 0.45f)
                    }
                }
            }
            .testTag("canvas_3d_viewport")
    ) {
        renderer.renderScene(
            drawScope = this,
            level = state.currentLevel,
            playerPos = state.playerPos,
            camera = state.camera,
            animationTicks = animTicks,
            pulseRadarActive = state.isRadarActive
        )
    }
}

@Composable
private fun TopGameHud(
    state: GamePlayState,
    modifier: Modifier = Modifier
) {
    CyberCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = CyberSurfaceCard.copy(alpha = 0.95f),
        borderColor = CyberMintPrimary.copy(alpha = 0.4f),
        cornerRadius = 18.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = state.currentLevel.name.uppercase(),
                        color = CyberMintLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                    Text(
                        text = "POS: [${state.playerPos.x}, ${state.playerPos.y}] • ALT: Tier ${state.playerPos.z}",
                        color = TextSecondaryDark,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Cores Count Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F3829))
                        .border(1.dp, CyberMintPrimary, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Energy Cores",
                            tint = CyberMintLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${state.coresCollected}/${state.totalCoresInLevel}",
                            color = CyberMintLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Player Health Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Shield Health",
                    tint = if (state.playerHealth > 30) CyberMintPrimary else CyberLaserRed,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                LinearProgressIndicator(
                    progress = { (state.playerHealth.toFloat() / state.maxHealth.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (state.playerHealth > 30) CyberMintPrimary else CyberLaserRed,
                    trackColor = Color(0xFF1E2F28)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${state.playerHealth} HP",
                    color = TextPrimaryDark,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${state.elapsedSeconds}s",
                    color = CyberCyanAccent,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun StatusTicker(
    state: GamePlayState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0A1F18))
            .border(1.dp, CyberMintDark.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (state.isGameOver) CyberLaserRed else CyberMintLight)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = state.statusMessage,
                color = TextPrimaryDark,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun VirtualControlDeck(
    onMove: (Direction) -> Unit,
    onJump: () -> Unit,
    onHack: () -> Unit,
    onRadar: () -> Unit,
    onRotate: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Directional Pad (Virtual D-Pad with 4 directional buttons)
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(Color(0xFF0C241B))
                .border(1.dp, CyberCardBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // North / Up
            IconButton(
                onClick = { onMove(Direction.NORTH) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp)
                    .size(42.dp)
                    .testTag("btn_move_north")
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Move North",
                    tint = CyberMintLight,
                    modifier = Modifier.size(30.dp)
                )
            }

            // South / Down
            IconButton(
                onClick = { onMove(Direction.SOUTH) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
                    .size(42.dp)
                    .testTag("btn_move_south")
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Move South",
                    tint = CyberMintLight,
                    modifier = Modifier.size(30.dp)
                )
            }

            // West / Left
            IconButton(
                onClick = { onMove(Direction.WEST) },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp)
                    .size(42.dp)
                    .testTag("btn_move_west")
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Move West",
                    tint = CyberMintLight,
                    modifier = Modifier.size(30.dp)
                )
            }

            // East / Right
            IconButton(
                onClick = { onMove(Direction.EAST) },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp)
                    .size(42.dp)
                    .testTag("btn_move_east")
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Move East",
                    tint = CyberMintLight,
                    modifier = Modifier.size(30.dp)
                )
            }

            // Center Compass Pivot
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(CyberMintPrimary.copy(alpha = 0.25f))
                    .border(1.dp, CyberMintPrimary, CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Tactical Action Buttons (Jump/Ascend, Hack Terminal, Radar Ping)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Jump / Ascend Elevation Ledge
            CyberPillButton(
                text = "ASCEND JUMP",
                icon = Icons.Default.Navigation,
                onClick = onJump,
                isPrimary = true,
                testTag = "btn_jump_ascend",
                modifier = Modifier.fillMaxWidth()
            )

            // Hack Terminal
            CyberPillButton(
                text = "HACK LOCK",
                icon = Icons.Default.LockOpen,
                onClick = onHack,
                isPrimary = false,
                testTag = "btn_hack_terminal",
                modifier = Modifier.fillMaxWidth()
            )

            // Radar Pulse
            CyberPillButton(
                text = "PULSE RADAR",
                icon = Icons.Default.Radar,
                onClick = onRadar,
                isPrimary = false,
                testTag = "btn_radar_ping",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun GameOverModal(
    state: GamePlayState,
    onRestart: () -> Unit,
    onOpenMotionLab: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B241C)),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            if (state.isVictory) CyberMintPrimary else CyberLaserRed
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (state.isVictory) Icons.Default.CheckCircle else Icons.Default.Dangerous,
                contentDescription = null,
                tint = if (state.isVictory) CyberMintPrimary else CyberLaserRed,
                modifier = Modifier.size(56.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (state.isVictory) "SECTOR CLEARED" else "SYSTEM CRITICAL",
                color = if (state.isVictory) CyberMintLight else CyberLaserRed,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (state.isVictory) {
                    "Exit portal accessed! All movement vectors synchronized successfully."
                } else {
                    "Operative shield depleted by drone ambush or sensory depression hazards."
                },
                color = TextSecondaryDark,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Run Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatColumn(label = "TIME", value = "${state.elapsedSeconds}s")
                StatColumn(label = "MOVES", value = "${state.movesCount}")
                StatColumn(label = "CORES", value = "${state.coresCollected}/${state.totalCoresInLevel}")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onRestart,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF163C2E)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f).testTag("modal_btn_retry")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = CyberMintLight)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "RETRY", color = CyberMintLight, fontFamily = FontFamily.Monospace)
                }

                Button(
                    onClick = onOpenMotionLab,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberMintPrimary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f).testTag("modal_btn_motion_lab")
                ) {
                    Text(text = "WALK LAB", color = Color(0xFF003822), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = TextSecondaryDark, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Text(text = value, color = TextPrimaryDark, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}
