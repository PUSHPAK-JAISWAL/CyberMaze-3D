package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
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
import com.example.data.model.RaidBattleState
import com.example.data.model.TacticalSpell
import com.example.data.model.TroopType
import com.example.game.engine.SiegeRenderer
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberPillButton
import com.example.ui.theme.CyberAmberWarning
import com.example.ui.theme.CyberBackgroundDark
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyanAccent
import com.example.ui.theme.CyberLaserRed
import com.example.ui.theme.CyberMintLight
import com.example.ui.theme.CyberMintPrimary
import com.example.ui.theme.CyberPurpleNeon
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import kotlinx.coroutines.delay

@Composable
fun RaidArenaScreen(
    state: RaidBattleState,
    onDeployTroop: (type: TroopType, x: Float, y: Float) -> Unit,
    onCastSpell: (spell: TacticalSpell, x: Float, y: Float) -> Unit,
    onStartRaidSector: (Int) -> Unit,
    onOpenBaseEditor: () -> Unit
) {
    val renderer = remember { SiegeRenderer() }
    var animTicks by remember { mutableLongStateOf(0L) }

    var selectedTroop by remember { mutableStateOf<TroopType?>(TroopType.BYTE_BRAWLER) }
    var selectedSpell by remember { mutableStateOf<TacticalSpell?>(null) }
    var selectedSectorIndex by remember { mutableStateOf(1) }

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
            .padding(10.dp)
            .testTag("raid_arena_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Top HUD (Sector Name, Stars, Destruction %, Timer)
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xFF091F18),
                borderColor = CyberMintPrimary.copy(alpha = 0.5f),
                contentPadding = 8.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = state.sectorName,
                            color = CyberMintLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "DESTRUCTION: ${state.destructionPercent}%",
                                color = if (state.destructionPercent >= 50) CyberMintPrimary else TextSecondaryDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            // 3 Stars Indicator
                            for (s in 1..3) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (state.starsEarned >= s) CyberAmberWarning else Color(0xFF283B33),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Timer & Sector Picker
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = if (state.timeRemainingSeconds < 20f) CyberLaserRed else CyberCyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${state.timeRemainingSeconds.toInt()}s",
                            color = if (state.timeRemainingSeconds < 20f) CyberLaserRed else TextPrimaryDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // 2. Elixir Meter
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = null,
                    tint = CyberCyanAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${String.format("%.1f", state.elixir)} / ${state.maxElixir.toInt()} ⚡",
                    color = CyberCyanAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(8.dp))
                LinearProgressIndicator(
                    progress = { (state.elixir / state.maxElixir).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = CyberCyanAccent,
                    trackColor = Color(0xFF0F3227)
                )
            }

            // 3. Main Battle Canvas (Tap to deploy troops/spells!)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
                    .pointerInput(selectedTroop, selectedSpell, state.battleEnded) {
                        detectTapGestures { offset ->
                            if (!state.battleEnded) {
                                val gridSize = 9
                                val tileSize = (minOf(size.width, size.height) * 0.94f) / gridSize
                                val originX = (size.width - (gridSize * tileSize)) / 2f
                                val originY = (size.height - (gridSize * tileSize)) / 2f

                                val gx = (offset.x - originX) / tileSize
                                val gy = (offset.y - originY) / tileSize

                                if (selectedSpell != null) {
                                    onCastSpell(selectedSpell!!, gx, gy)
                                } else if (selectedTroop != null) {
                                    onDeployTroop(selectedTroop!!, gx, gy)
                                }
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    renderer.renderSiegeGrid(
                        drawScope = this,
                        buildings = state.buildings,
                        troops = state.troops,
                        isDefenseEditorMode = false,
                        animTicks = animTicks
                    )
                }

                // Banner overlay notification
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                        .background(Color(0xCC061711), RoundedCornerShape(6.dp))
                        .border(1.dp, CyberCardBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = state.bannerMessage,
                        color = CyberMintLight,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Battle Ended Modal (Victory or Defeat)
                if (state.battleEnded) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(16.dp)
                            .align(Alignment.Center),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF071C15)),
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp,
                            if (state.isVictory) CyberAmberWarning else CyberLaserRed
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = if (state.isVictory) "🏆 VICTORY!" else "DEFEAT",
                                color = if (state.isVictory) CyberAmberWarning else CyberLaserRed,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )

                            Row {
                                for (s in 1..3) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (state.starsEarned >= s) CyberAmberWarning else TextMutedDark,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Text(
                                text = "DESTRUCTION: ${state.destructionPercent}%",
                                color = TextPrimaryDark,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )

                            if (state.isVictory) {
                                Text(
                                    text = "+${state.bitsLooted} BITS  •  +${state.trophiesWon} TROPHIES",
                                    color = CyberMintPrimary,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onStartRaidSector(selectedSectorIndex) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberMintPrimary),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("PLAY AGAIN", color = Color(0xFF003822), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Button(
                                    onClick = onOpenBaseEditor,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF153328)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("MY BASE", color = CyberMintLight, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // 4. Bottom Troop Deployment Deck (Clash Royale Style cards)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "DEPLOY SQUAD (TAP CARD, THEN TAP MAP PERIMETER):",
                    color = TextSecondaryDark,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val troops = TroopType.entries
                    for (t in troops) {
                        val isSelected = selectedTroop == t && selectedSpell == null
                        val canAfford = state.elixir >= t.elixirCost

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF0F3D2E) else Color(0xFF081C15))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) CyberMintPrimary else if (canAfford) CyberCardBorder else Color(0xFF192A23),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedTroop = t
                                    selectedSpell = null
                                }
                                .padding(vertical = 6.dp, horizontal = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = t.title.take(7),
                                    color = if (isSelected) CyberMintLight else TextPrimaryDark,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FlashOn,
                                        contentDescription = null,
                                        tint = CyberCyanAccent,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = "${t.elixirCost}",
                                        color = CyberCyanAccent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }

                // Tactical Spells Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val spells = TacticalSpell.entries
                    for (sp in spells) {
                        val isSelected = selectedSpell == sp
                        val canAfford = state.elixir >= sp.elixirCost

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF18283B) else Color(0xFF07141E))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) CyberCyanAccent else if (canAfford) Color(0xFF123447) else Color(0xFF10202B),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedSpell = sp
                                    selectedTroop = null
                                }
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = sp.title,
                                    color = if (isSelected) CyberCyanAccent else TextSecondaryDark,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${sp.elixirCost}⚡",
                                    color = CyberCyanAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
