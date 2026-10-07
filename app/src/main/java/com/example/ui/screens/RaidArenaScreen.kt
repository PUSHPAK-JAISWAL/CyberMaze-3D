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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextOverflow
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
    aiTacticalIntel: String,
    currentSector: Int = 1,
    maxUnlockedSector: Int = 1,
    onDeployTroop: (type: TroopType, x: Float, y: Float) -> Unit,
    onCastSpell: (spell: TacticalSpell, x: Float, y: Float) -> Unit,
    onStartRaidSector: (Int) -> Unit,
    onOpenBaseEditor: () -> Unit,
    onRequestTacticalIntel: () -> Unit
) {
    val renderer = remember { SiegeRenderer() }
    var animTicks by remember { mutableLongStateOf(0L) }

    var selectedTroop by remember { mutableStateOf<TroopType?>(TroopType.BYTE_BRAWLER) }
    var selectedSpell by remember { mutableStateOf<TacticalSpell?>(null) }
    var selectedSectorIndex by remember(currentSector) { mutableIntStateOf(currentSector) }

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
            // 1. Top HUD (Sector Name, Level Selector, Stars, Destruction %, Timer)
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xFF091F18),
                borderColor = CyberMintPrimary.copy(alpha = 0.5f),
                contentPadding = 8.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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

                        // Timer
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

                    // Sector / Level Progression Row (Selectable sectors 1 to 4)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SECTOR:",
                            color = TextSecondaryDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        for (sec in 1..4) {
                            val isUnlocked = sec <= maxUnlockedSector
                            val isCurrent = selectedSectorIndex == sec
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isCurrent) CyberMintPrimary
                                        else if (isUnlocked) Color(0xFF0F3227)
                                        else Color(0xFF14201B)
                                    )
                                    .border(
                                        1.dp,
                                        if (isCurrent) CyberMintLight
                                        else if (isUnlocked) CyberCardBorder
                                        else Color(0xFF1B2B24),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable(enabled = isUnlocked) {
                                        selectedSectorIndex = sec
                                        onStartRaidSector(sec)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isUnlocked) "LVL $sec" else "🔒 $sec",
                                    color = if (isCurrent) Color(0xFF003822)
                                    else if (isUnlocked) CyberMintLight
                                    else TextMutedDark,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            // 2. AI Tactical Intel Banner (Direct LLM Integration showcase)
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xFF051711),
                borderColor = CyberCyanAccent.copy(alpha = 0.45f),
                contentPadding = 6.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = CyberCyanAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = aiTacticalIntel,
                            color = CyberMintLight,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    CyberPillButton(
                        text = "AI INTEL",
                        icon = Icons.Default.AutoAwesome,
                        onClick = onRequestTacticalIntel,
                        isPrimary = false,
                        horizontalPadding = 8.dp,
                        verticalPadding = 6.dp,
                        fontSize = 10.sp
                    )
                }
            }

            // 3. Elixir Meter
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

            // 4. Main Battle Canvas (Tap to deploy troops/spells!)
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
                                if (state.isVictory && selectedSectorIndex < 4) {
                                    Button(
                                        onClick = {
                                            val next = selectedSectorIndex + 1
                                            selectedSectorIndex = next
                                            onStartRaidSector(next)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CyberMintPrimary),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("NEXT LEVEL ➔", color = Color(0xFF003822), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                } else {
                                    Button(
                                        onClick = { onStartRaidSector(selectedSectorIndex) },
                                        colors = ButtonDefaults.buttonColors(containerColor = CyberMintPrimary),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("PLAY AGAIN", color = Color(0xFF003822), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
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

            // 5. Bottom Troop Deployment Deck (Clean, Uncrowded & Uniform Buttons)
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

                        val shortLabel = when (t) {
                            TroopType.BYTE_BRAWLER -> "BRAWLER"
                            TroopType.GLITCH_SPRINTER -> "SPRINTER"
                            TroopType.EMP_HACKER -> "EMP HACK"
                            TroopType.PHANTOM_DRONE -> "PHANTOM"
                        }

                        val troopIcon = when (t) {
                            TroopType.BYTE_BRAWLER -> Icons.Default.Shield
                            TroopType.GLITCH_SPRINTER -> Icons.Default.DirectionsRun
                            TroopType.EMP_HACKER -> Icons.Default.Bolt
                            TroopType.PHANTOM_DRONE -> Icons.Default.Flight
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(58.dp)
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
                                .padding(horizontal = 4.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = troopIcon,
                                        contentDescription = null,
                                        tint = if (isSelected) CyberMintLight else TextPrimaryDark,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = shortLabel,
                                        color = if (isSelected) CyberMintLight else TextPrimaryDark,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF07241E))
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "${t.elixirCost} ⚡",
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

                // Tactical Spells Row (Consistent Height & Clean Labels)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val spells = TacticalSpell.entries
                    for (sp in spells) {
                        val isSelected = selectedSpell == sp
                        val canAfford = state.elixir >= sp.elixirCost

                        val spellLabel = when (sp) {
                            TacticalSpell.EMP_SURGE -> "EMP SURGE"
                            TacticalSpell.OVERCLOCK -> "OVERCLOCK"
                            TacticalSpell.ORBITAL_BEAM -> "ORBITAL BEAM"
                        }

                        val spellIcon = when (sp) {
                            TacticalSpell.EMP_SURGE -> Icons.Default.Bolt
                            TacticalSpell.OVERCLOCK -> Icons.Default.FlashOn
                            TacticalSpell.ORBITAL_BEAM -> Icons.Default.RocketLaunch
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
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
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = spellIcon,
                                    contentDescription = null,
                                    tint = if (isSelected) CyberCyanAccent else TextSecondaryDark,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = spellLabel,
                                    color = if (isSelected) CyberCyanAccent else TextSecondaryDark,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1
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
