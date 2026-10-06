package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.ui.components.CipherPuzzleDialog
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
import kotlin.math.abs

@Composable
fun GameScreen(
    state: GamePlayState,
    onMove: (Direction) -> Unit,
    onStepTowardTile: (Int, Int) -> Unit = { _, _ -> },
    onJumpVault: () -> Unit,
    onEmpBlast: () -> Unit,
    onCloak: () -> Unit,
    onOpenCipher: () -> Unit,
    onSolveCipher: () -> Unit,
    onCloseCipher: () -> Unit,
    onRadarPing: () -> Unit,
    onRotateCamera: (Float) -> Unit,
    onSetCameraPreset: (pitch: Float, yaw: Float) -> Unit,
    onSetCameraZoom: (Float) -> Unit = {},
    onRestart: () -> Unit,
    onOpenMotionLab: () -> Unit
) {
    val renderer = remember { Game3DRenderer() }
    var animTicks by remember { mutableLongStateOf(0L) }

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
        val isWide = maxWidth > 600.dp

        if (isWide) {
            // Tablet / Landscape Split Layout
            Row(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                Box(modifier = Modifier.weight(1.3f).fillMaxSize()) {
                    GameCanvas3D(
                        renderer = renderer,
                        state = state,
                        animTicks = animTicks,
                        onMove = onMove,
                        onStepTowardTile = onStepTowardTile
                    )
                    TopTacticalHud(
                        state = state,
                        modifier = Modifier.align(Alignment.TopCenter).padding(8.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1.0f).fillMaxSize().padding(start = 12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    TacticalStatusBanner(state = state)
                    SpaciousControlDeck(
                        state = state,
                        onMove = onMove,
                        onJumpVault = onJumpVault,
                        onEmpBlast = onEmpBlast,
                        onCloak = onCloak,
                        onOpenCipher = onOpenCipher,
                        onRadarPing = onRadarPing
                    )
                }
            }
        } else {
            // Adaptive Mobile Layout with clean, comfortable proportions
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                // Top HUD
                TopTacticalHud(state = state)

                Spacer(modifier = Modifier.height(6.dp))

                // 3D Canvas Viewport
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.25f)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.5.dp, CyberCardBorder, RoundedCornerShape(20.dp))
                ) {
                    GameCanvas3D(
                        renderer = renderer,
                        state = state,
                        animTicks = animTicks,
                        onMove = onMove,
                        onStepTowardTile = onStepTowardTile
                    )

                    // Camera Controls Overlay (Top Right: Orbit & Zoom)
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xBB0A261C))
                            .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onRotateCamera(-45f) },
                            modifier = Modifier.size(28.dp).testTag("cam_orbit_left")
                        ) {
                            Icon(Icons.Default.RotateRight, "Orbit Left", tint = CyberMintLight, modifier = Modifier.size(16.dp))
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
                            modifier = Modifier.size(28.dp).testTag("cam_orbit_right")
                        ) {
                            Icon(Icons.Default.RotateRight, "Orbit Right", tint = CyberMintLight, modifier = Modifier.size(16.dp))
                        }
                        Box(
                            modifier = Modifier
                                .clickable { onSetCameraZoom((state.camera.zoom + 0.15f).coerceAtMost(1.8f)) }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Add, "Zoom In", tint = CyberMintLight, modifier = Modifier.size(16.dp))
                        }
                        Box(
                            modifier = Modifier
                                .clickable { onSetCameraZoom((state.camera.zoom - 0.15f).coerceAtLeast(0.6f)) }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Remove, "Zoom Out", tint = CyberMintLight, modifier = Modifier.size(16.dp))
                        }
                    }

                    // View Preset Switchers (Top Left: 3D vs Top)
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xBB0A261C))
                            .border(1.dp, CyberCardBorder, RoundedCornerShape(14.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clickable { onSetCameraPreset(0.55f, 45f) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("3D", color = if (state.camera.pitchRatio < 0.7f) CyberMintPrimary else TextSecondaryDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .clickable { onSetCameraPreset(0.85f, 0f) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("TOP", color = if (state.camera.pitchRatio >= 0.7f) CyberMintPrimary else TextSecondaryDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Gesture hint chip (bottom center)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x990A261C))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SWIPE / TAP TILE TO MOVE",
                            color = CyberMintLight.copy(alpha = 0.75f),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Tactical Drone Warning Chip (Bottom Left)
                    val alertEnemy = state.currentLevel.enemies.find { it.isAlerted }
                        ?: state.currentLevel.enemies.firstOrNull()
                    if (alertEnemy != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(6.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (alertEnemy.isAlerted) Color(0xD02E0A12) else Color(0xBB0A261C))
                                .border(1.dp, if (alertEnemy.isAlerted) CyberLaserRed else CyberCardBorder, RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (alertEnemy.isAlerted) CyberLaserRed else CyberMintLight)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${alertEnemy.name}: ${alertEnemy.lastActionText}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Tactical Status Banner
                TacticalStatusBanner(state = state)

                Spacer(modifier = Modifier.height(6.dp))

                // Spacious Ergonomic Controller Deck
                SpaciousControlDeck(
                    state = state,
                    onMove = onMove,
                    onJumpVault = onJumpVault,
                    onEmpBlast = onEmpBlast,
                    onCloak = onCloak,
                    onOpenCipher = onOpenCipher,
                    onRadarPing = onRadarPing,
                    modifier = Modifier.weight(0.95f)
                )
            }
        }

        // Interactive Hacking Cipher Dialog
        if (state.activeCipherTerminal != null) {
            CipherPuzzleDialog(
                terminal = state.activeCipherTerminal,
                onSuccess = onSolveCipher,
                onDismiss = onCloseCipher
            )
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
    onMove: (Direction) -> Unit,
    onStepTowardTile: (Int, Int) -> Unit
) {
    var accumulatedDx by remember { mutableFloatStateOf(0f) }
    var accumulatedDy by remember { mutableFloatStateOf(0f) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { tapOffset ->
                        val tile = renderer.findTileAtScreenPoint(
                            tapOffset,
                            state.currentLevel,
                            state.camera,
                            size.width.toFloat(),
                            size.height.toFloat()
                        )
                        if (tile != null) {
                            onStepTowardTile(tile.x, tile.y)
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        accumulatedDx = 0f
                        accumulatedDy = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        accumulatedDx += dragAmount.x
                        accumulatedDy += dragAmount.y

                        val threshold = 36f
                        if (abs(accumulatedDx) >= threshold || abs(accumulatedDy) >= threshold) {
                            if (abs(accumulatedDx) >= abs(accumulatedDy)) {
                                if (accumulatedDx > 0) onMove(Direction.EAST) else onMove(Direction.WEST)
                            } else {
                                if (accumulatedDy > 0) onMove(Direction.SOUTH) else onMove(Direction.NORTH)
                            }
                            accumulatedDx = 0f
                            accumulatedDy = 0f
                        }
                    }
                )
            }
            .testTag("canvas_3d_viewport")
    ) {
        renderer.renderScene(
            drawScope = this,
            level = state.currentLevel,
            playerPos = state.playerPos,
            camera = state.camera,
            animationTicks = animTicks,
            pulseRadarActive = state.isRadarActive,
            isPlayerCloaked = state.isCloaked,
            shockwaveRadius = state.shockwaveRadius
        )
    }
}

@Composable
private fun TopTacticalHud(
    state: GamePlayState,
    modifier: Modifier = Modifier
) {
    CyberCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = Color(0xF20F2E23),
        borderColor = CyberMintPrimary.copy(alpha = 0.4f),
        cornerRadius = 16.dp,
        contentPadding = 10.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = state.currentLevel.name.uppercase(),
                        color = CyberMintLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                    Text(
                        text = "POS: [${state.playerPos.x}, ${state.playerPos.y}] • ALT: Tier ${state.playerPos.z} • Moves: ${state.movesCount}",
                        color = TextSecondaryDark,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Inventory Badges: Cores, EMP, Cloak
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cores Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F3829))
                            .border(1.dp, CyberMintPrimary, RoundedCornerShape(10.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, null, tint = CyberMintLight, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${state.coresCollected}/${state.totalCoresInLevel}",
                                color = CyberMintLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // EMP Charges
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0D333B))
                            .border(1.dp, CyberCyanAccent, RoundedCornerShape(10.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FlashOn, null, tint = CyberCyanAccent, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${state.empCharges}",
                                color = CyberCyanAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
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
                    contentDescription = null,
                    tint = if (state.playerHealth > 30) CyberMintPrimary else CyberLaserRed,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                LinearProgressIndicator(
                    progress = { (state.playerHealth.toFloat() / state.maxHealth.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .weight(1f)
                        .height(7.dp)
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
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "${state.score} pts",
                    color = CyberCyanAccent,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun TacticalStatusBanner(
    state: GamePlayState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0B241C))
            .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (state.isCloaked) CyberCyanAccent else CyberMintLight)
            )
            Spacer(modifier = Modifier.width(6.dp))
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
private fun SpaciousControlDeck(
    state: GamePlayState,
    onMove: (Direction) -> Unit,
    onJumpVault: () -> Unit,
    onEmpBlast: () -> Unit,
    onCloak: () -> Unit,
    onOpenCipher: () -> Unit,
    onRadarPing: () -> Unit,
    onSetCameraPreset: (Float, Float) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Tactile Circular D-Pad
        Box(
            modifier = Modifier
                .size(136.dp)
                .clip(CircleShape)
                .background(Color(0xFF0C241B))
                .border(1.dp, CyberCardBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // North
            IconButton(
                onClick = { onMove(Direction.NORTH) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 2.dp)
                    .size(42.dp)
                    .testTag("btn_move_north")
            ) {
                Icon(Icons.Default.KeyboardArrowUp, "North", tint = CyberMintLight, modifier = Modifier.size(28.dp))
            }

            // South
            IconButton(
                onClick = { onMove(Direction.SOUTH) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 2.dp)
                    .size(42.dp)
                    .testTag("btn_move_south")
            ) {
                Icon(Icons.Default.KeyboardArrowDown, "South", tint = CyberMintLight, modifier = Modifier.size(28.dp))
            }

            // West
            IconButton(
                onClick = { onMove(Direction.WEST) },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 2.dp)
                    .size(42.dp)
                    .testTag("btn_move_west")
            ) {
                Icon(Icons.Default.KeyboardArrowLeft, "West", tint = CyberMintLight, modifier = Modifier.size(28.dp))
            }

            // East
            IconButton(
                onClick = { onMove(Direction.EAST) },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 2.dp)
                    .size(42.dp)
                    .testTag("btn_move_east")
            ) {
                Icon(Icons.Default.KeyboardArrowRight, "East", tint = CyberMintLight, modifier = Modifier.size(28.dp))
            }

            // Center Compass Node
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(CyberMintPrimary.copy(alpha = 0.25f))
                    .border(1.dp, CyberMintPrimary, CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Right: Spacious 2x2 Tactical Gadget Buttons
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // VAULT / JUMP
                CyberPillButton(
                    text = "VAULT",
                    icon = Icons.Default.Navigation,
                    onClick = onJumpVault,
                    isPrimary = true,
                    testTag = "btn_jump_ascend",
                    modifier = Modifier.weight(1f),
                    horizontalPadding = 8.dp,
                    verticalPadding = 9.dp
                )

                // EMP SHOCK
                CyberPillButton(
                    text = "EMP (${state.empCharges})",
                    icon = Icons.Default.FlashOn,
                    onClick = onEmpBlast,
                    enabled = state.empCharges > 0,
                    isPrimary = state.empCharges > 0,
                    testTag = "btn_emp_blast",
                    modifier = Modifier.weight(1f),
                    horizontalPadding = 8.dp,
                    verticalPadding = 9.dp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // HACK CIPHER
                CyberPillButton(
                    text = "HACK",
                    icon = Icons.Default.Terminal,
                    onClick = onOpenCipher,
                    isPrimary = false,
                    testTag = "btn_hack_terminal",
                    modifier = Modifier.weight(1f),
                    horizontalPadding = 8.dp,
                    verticalPadding = 9.dp
                )

                // CLOAK / STEALTH
                CyberPillButton(
                    text = if (state.isCloaked) "CLOAKED" else "CLOAK (${state.cloakCharges})",
                    icon = if (state.isCloaked) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    onClick = onCloak,
                    enabled = state.cloakCharges > 0 || state.isCloaked,
                    isPrimary = false,
                    testTag = "btn_cloak",
                    modifier = Modifier.weight(1f),
                    horizontalPadding = 8.dp,
                    verticalPadding = 9.dp
                )
            }

            // Radar Pulse Quick Strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0F3325))
                    .border(1.dp, CyberMintDark, RoundedCornerShape(14.dp))
                    .clickable { onRadarPing() }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Radar, null, tint = CyberMintLight, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SWEEP RADAR SCANNER",
                        color = CyberMintLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
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
        shape = RoundedCornerShape(22.dp),
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            if (state.isVictory) CyberMintPrimary else CyberLaserRed
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (state.isVictory) Icons.Default.CheckCircle else Icons.Default.Dangerous,
                contentDescription = null,
                tint = if (state.isVictory) CyberMintPrimary else CyberLaserRed,
                modifier = Modifier.size(50.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (state.isVictory) "SECTOR CLEARED" else "SYSTEM COMPROMISED",
                color = if (state.isVictory) CyberMintLight else CyberLaserRed,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            if (state.isVictory) {
                Spacer(modifier = Modifier.height(6.dp))
                // 3 Stars Rating Display
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (i in 1..3) {
                        val isStar = i <= state.starsEarned
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (isStar) CyberAmberWarning else Color(0xFF1E4032),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (state.isVictory) {
                    "Extraction successful! Score: ${state.score} pts • ${state.movesCount} moves."
                } else {
                    "Operative shield depleted. Avoid drone vision cones and use EMP blast to stun."
                },
                color = TextSecondaryDark,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onRestart,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).testTag("modal_btn_retry")
                ) {
                    Icon(Icons.Default.Refresh, null, tint = CyberMintLight, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("RETRY", color = CyberMintLight, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                }

                Button(
                    onClick = onOpenMotionLab,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberMintPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1.2f).testTag("modal_btn_motion_lab")
                ) {
                    Text("NEXT SECTOR", color = Color(0xFF003822), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                }
            }
        }
    }
}
