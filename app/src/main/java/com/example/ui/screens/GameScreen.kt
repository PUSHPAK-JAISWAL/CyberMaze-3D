package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Timer
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
import com.example.data.model.BattleCardType
import com.example.data.model.RunnerGameState
import com.example.game.engine.Game3DRenderer
import com.example.ui.theme.CyberAmberWarning
import com.example.ui.theme.CyberBackgroundDark
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyanAccent
import com.example.ui.theme.CyberLaserRed
import com.example.ui.theme.CyberMintLight
import com.example.ui.theme.CyberMintPrimary
import com.example.ui.theme.CyberPurpleNeon
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import kotlinx.coroutines.delay
import kotlin.math.abs

@Composable
fun GameScreen(
    state: RunnerGameState,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onJump: () -> Unit,
    onSlide: () -> Unit,
    onDeployCard: (BattleCardType) -> Unit,
    onRestart: () -> Unit,
    onOpenDeck: () -> Unit
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            // 1. Arcade Runner Top HUD
            RunnerTopHud(state = state)

            Spacer(modifier = Modifier.height(6.dp))

            // 2. 3D Subway Runner Perspective Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.3f)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.5.dp, CyberCardBorder, RoundedCornerShape(20.dp))
            ) {
                RunnerCanvas3D(
                    renderer = renderer,
                    state = state,
                    animTicks = animTicks,
                    onSwipeLeft = onSwipeLeft,
                    onSwipeRight = onSwipeRight,
                    onJump = onJump,
                    onSlide = onSlide
                )

                // Top Right: Multiplier & Distance Badges
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xD00A261C))
                            .border(1.dp, CyberMintPrimary, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${state.multiplier}X MULTIPLIER",
                            color = CyberMintLight,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xD00A261C))
                            .border(1.dp, CyberCyanAccent, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${state.distanceMeters.toInt()}m",
                            color = CyberCyanAccent,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Danger Drone Warning Indicator (Bottom Left)
                if (state.bossDroneDistance < 10f) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xD0380812))
                            .border(1.dp, CyberLaserRed, RoundedCornerShape(10.dp))
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
                                text = "⚠️ DRONE IN PURSUIT! (${String.format("%.1f", state.bossDroneDistance)}m)",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 3. Ticker Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0B241C))
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = state.lastBannerMessage,
                    color = TextPrimaryDark,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 4. Tactical Battle Cards Deck Strip (Clash Royale Power Cards)
            BattleCardDeckRow(
                state = state,
                onDeployCard = onDeployCard
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 5. Ergonomic Touch Control Deck (Subway Surfers Swipe + Virtual Buttons)
            VirtualControlsDeck(
                onSwipeLeft = onSwipeLeft,
                onSwipeRight = onSwipeRight,
                onJump = onJump,
                onSlide = onSlide
            )
        }

        // 6. Game Over Modal
        AnimatedVisibility(
            visible = state.isGameOver,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            GameOverModal(
                state = state,
                onRestart = onRestart,
                onOpenDeck = onOpenDeck
            )
        }
    }
}

@Composable
private fun RunnerCanvas3D(
    renderer: Game3DRenderer,
    state: RunnerGameState,
    animTicks: Long,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onJump: () -> Unit,
    onSlide: () -> Unit
) {
    var accumulatedDx by remember { mutableFloatStateOf(0f) }
    var accumulatedDy by remember { mutableFloatStateOf(0f) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
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

                        val threshold = 28f
                        if (abs(accumulatedDx) >= threshold || abs(accumulatedDy) >= threshold) {
                            if (abs(accumulatedDx) >= abs(accumulatedDy)) {
                                if (accumulatedDx > 0) onSwipeRight() else onSwipeLeft()
                            } else {
                                if (accumulatedDy > 0) onSlide() else onJump()
                            }
                            accumulatedDx = 0f
                            accumulatedDy = 0f
                        }
                    }
                )
            }
            .testTag("runner_canvas_3d")
    ) {
        renderer.renderScene(
            drawScope = this,
            state = state,
            animTicks = animTicks
        )
    }
}

@Composable
private fun RunnerTopHud(state: RunnerGameState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xF20F2E23)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberMintPrimary.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score Ticker
                Column {
                    Text(
                        text = "SCORE",
                        color = TextSecondaryDark,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${state.score}",
                        color = CyberMintLight,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Coins / Neon Bits Collected
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F3829))
                        .border(1.dp, CyberAmberWarning, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = "Bits",
                        tint = CyberAmberWarning,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${state.coinsCollected}",
                        color = CyberAmberWarning,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // High Score
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "RECORD",
                        color = TextSecondaryDark,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${state.highScore}",
                        color = CyberCyanAccent,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Energy Bar for Battle Cards
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = CyberCyanAccent,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                LinearProgressIndicator(
                    progress = { (state.energyCharge.toFloat() / state.maxEnergy.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = CyberCyanAccent,
                    trackColor = Color(0xFF1E2F28)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${state.energyCharge}/${state.maxEnergy} NRG",
                    color = CyberCyanAccent,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun BattleCardDeckRow(
    state: RunnerGameState,
    onDeployCard: (BattleCardType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        state.activeDeck.forEach { card ->
            val isReady = state.energyCharge >= card.energyCost && card.currentCooldown <= 0f
            val icon = when (card.type) {
                BattleCardType.EMP_BLAST -> Icons.Default.FlashOn
                BattleCardType.WARP_DRIVE -> Icons.Default.RocketLaunch
                BattleCardType.SHIELD_MATRIX -> Icons.Default.Shield
                BattleCardType.CHRONO_SLOW -> Icons.Default.Timer
                BattleCardType.DRONE_STRIKE -> Icons.Default.Navigation
            }
            val accentColor = when (card.type) {
                BattleCardType.EMP_BLAST -> CyberLaserRed
                BattleCardType.WARP_DRIVE -> CyberPurpleNeon
                BattleCardType.SHIELD_MATRIX -> CyberMintPrimary
                BattleCardType.CHRONO_SLOW -> CyberCyanAccent
                BattleCardType.DRONE_STRIKE -> CyberAmberWarning
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isReady) Color(0xFF0F3526) else Color(0xFF091C15))
                    .border(
                        1.dp,
                        if (isReady) accentColor else CyberCardBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable(enabled = isReady) { onDeployCard(card.type) }
                    .padding(vertical = 6.dp, horizontal = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = icon,
                        contentDescription = card.name,
                        tint = if (isReady) accentColor else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (card.currentCooldown > 0f) "${card.currentCooldown.toInt()}s" else card.name.take(7),
                        color = if (isReady) Color.White else Color.Gray,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun VirtualControlsDeck(
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onJump: () -> Unit,
    onSlide: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Lane Button
        Button(
            onClick = onSwipeLeft,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3526)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.weight(1f).height(46.dp).testTag("btn_lane_left")
        ) {
            Icon(Icons.Default.KeyboardArrowLeft, "Left", tint = CyberMintLight, modifier = Modifier.size(24.dp))
        }

        // Jump Button (Swipe Up)
        Button(
            onClick = onJump,
            colors = ButtonDefaults.buttonColors(containerColor = CyberMintPrimary),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.weight(1.2f).height(46.dp).testTag("btn_runner_jump")
        ) {
            Icon(Icons.Default.KeyboardArrowUp, "Jump", tint = Color(0xFF003822), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("JUMP", color = Color(0xFF003822), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }

        // Slide Button (Swipe Down)
        Button(
            onClick = onSlide,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF133B2C)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.weight(1.2f).height(46.dp).testTag("btn_runner_slide")
        ) {
            Icon(Icons.Default.KeyboardArrowDown, "Slide", tint = CyberCyanAccent, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("SLIDE", color = CyberCyanAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }

        // Right Lane Button
        Button(
            onClick = onSwipeRight,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3526)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.weight(1f).height(46.dp).testTag("btn_lane_right")
        ) {
            Icon(Icons.Default.KeyboardArrowRight, "Right", tint = CyberMintLight, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun GameOverModal(
    state: RunnerGameState,
    onRestart: () -> Unit,
    onOpenDeck: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C241B)),
        shape = RoundedCornerShape(22.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, CyberLaserRed)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Dangerous,
                contentDescription = null,
                tint = CyberLaserRed,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "RUN TERMINATED",
                color = CyberLaserRed,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Hunter Drone intercepted operative.",
                color = TextSecondaryDark,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Score & Coins summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF071912))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("DISTANCE", color = TextSecondaryDark, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("${state.distanceMeters.toInt()}m", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("SCORE", color = TextSecondaryDark, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("${state.score}", color = CyberMintLight, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("BITS", color = TextSecondaryDark, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("+${state.coinsCollected}", color = CyberAmberWarning, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenDeck,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF133B2C)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).testTag("modal_btn_deck")
                ) {
                    Text("CARDS & DECK", color = CyberMintLight, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }

                Button(
                    onClick = onRestart,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberMintPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1.2f).testTag("modal_btn_run_again")
                ) {
                    Icon(Icons.Default.Refresh, null, tint = Color(0xFF003822), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("RUN AGAIN", color = Color(0xFF003822), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                }
            }
        }
    }
}
