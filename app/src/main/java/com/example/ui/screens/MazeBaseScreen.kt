package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DefenseType
import com.example.data.model.MazeBuilding
import com.example.game.engine.SiegeRenderer
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberPillButton
import com.example.ui.components.CyberSectionHeader
import com.example.ui.theme.CyberAmberWarning
import com.example.ui.theme.CyberBackgroundDark
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyanAccent
import com.example.ui.theme.CyberMintLight
import com.example.ui.theme.CyberMintPrimary
import com.example.ui.theme.CyberPurpleNeon
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import kotlinx.coroutines.delay

@Composable
fun MazeBaseScreen(
    buildings: List<MazeBuilding>,
    userBits: Int,
    onPlaceBuilding: (DefenseType, Int, Int) -> Unit,
    onRemoveBuilding: (Int, Int) -> Unit,
    onSimulateDefense: () -> Unit
) {
    val renderer = remember { SiegeRenderer() }
    var animTicks by remember { mutableLongStateOf(0L) }
    var selectedTool by remember { mutableStateOf<DefenseType?>(DefenseType.NEON_WALL) }
    var isEraseMode by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            animTicks++
        }
    }

    val wallCount = buildings.count { it.type == DefenseType.NEON_WALL }
    val defenseCount = buildings.count { !it.type.isWall && !it.type.isCore }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackgroundDark)
            .padding(10.dp)
            .testTag("maze_base_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Header & Base Stats
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xFF0C241B),
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
                            text = "CYBERMAZE FORTRESS DEFENSE",
                            color = CyberMintLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Walls: $wallCount • Turrets: $defenseCount • Bits: $userBits ⚡",
                            color = TextSecondaryDark,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    CyberPillButton(
                        text = "TEST DEFENSE",
                        icon = Icons.Default.PlayArrow,
                        onClick = onSimulateDefense,
                        isPrimary = true
                    )
                }
            }

            // 2. Interactive 8x8 Grid Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
                    .pointerInput(selectedTool, isEraseMode) {
                        detectTapGestures { offset ->
                            val gridSize = 9
                            val tileSize = (minOf(size.width, size.height) * 0.94f) / gridSize
                            val originX = (size.width - (gridSize * tileSize)) / 2f
                            val originY = (size.height - (gridSize * tileSize)) / 2f

                            val gx = ((offset.x - originX) / tileSize).toInt()
                            val gy = ((offset.y - originY) / tileSize).toInt()

                            if (gx in 1..7 && gy in 1..7) {
                                if (isEraseMode) {
                                    onRemoveBuilding(gx, gy)
                                } else if (selectedTool != null) {
                                    onPlaceBuilding(selectedTool!!, gx, gy)
                                }
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    renderer.renderSiegeGrid(
                        drawScope = this,
                        buildings = buildings,
                        troops = emptyList(),
                        isDefenseEditorMode = true,
                        animTicks = animTicks
                    )
                }

                // Tip banner
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                        .background(Color(0xCC061711), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Tap grid to place / remove. Funnel enemies into your kill zone!",
                        color = CyberMintLight,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // 3. Building Palette
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "SELECT BUILDING TO PLACE:",
                    color = TextSecondaryDark,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val paletteItems = listOf(
                        DefenseType.NEON_WALL,
                        DefenseType.LASER_TURRET,
                        DefenseType.TESLA_PYLON,
                        DefenseType.PLASMA_MORTAR,
                        DefenseType.GLITCH_MINE
                    )

                    for (item in paletteItems) {
                        val isSelected = !isEraseMode && selectedTool == item
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) Color(0xFF0F3D2E) else Color(0xFF081C15))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) CyberMintPrimary else CyberCardBorder,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    isEraseMode = false
                                    selectedTool = item
                                }
                                .padding(vertical = 6.dp, horizontal = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = item.title.split(" ").last(),
                                    color = if (isSelected) CyberMintLight else TextPrimaryDark,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${item.costBits}⚡",
                                    color = CyberCyanAccent,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    // Erase Tool Button
                    Box(
                        modifier = Modifier
                            .weight(0.9f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isEraseMode) Color(0xFF3B1015) else Color(0xFF1E0A0E))
                            .border(
                                width = if (isEraseMode) 2.dp else 1.dp,
                                color = if (isEraseMode) CyberAmberWarning else Color(0xFF38151D),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable {
                                isEraseMode = true
                                selectedTool = null
                            }
                            .padding(vertical = 6.dp, horizontal = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Erase",
                                tint = CyberAmberWarning,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "DEL",
                                color = CyberAmberWarning,
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
