package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.LevelEntity
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberPillButton
import com.example.ui.components.CyberSectionHeader
import com.example.ui.theme.CyberBackgroundDark
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyanAccent
import com.example.ui.theme.CyberLaserRed
import com.example.ui.theme.CyberMintLight
import com.example.ui.theme.CyberMintPrimary
import com.example.ui.theme.CyberSurfaceCard
import com.example.ui.theme.DepressionDropColor
import com.example.ui.theme.ElevationClimbColor
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HolodeckScreen(
    savedLevels: List<LevelEntity>,
    isGenerating: Boolean,
    generationStatus: String,
    onLoadLevel: (LevelEntity) -> Unit,
    onDeleteLevel: (Long) -> Unit,
    onGenerateCustomLevel: (theme: String, difficulty: String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var customThemeText by remember { mutableStateOf("Neon Skyline") }
    var customDifficulty by remember { mutableStateOf("Normal") }

    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackgroundDark)
            .padding(16.dp)
            .testTag("holodeck_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            CyberSectionHeader(
                title = "HOLODECK ARCHIVE",
                subtitle = "Offline cached 3D sectors generated from sensor history",
                badgeText = "${savedLevels.size} SECTORS"
            )
        }

        // Action Bar: Forge New Level
        item {
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xFF0C241B),
                borderColor = CyberMintPrimary.copy(alpha = 0.5f)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SYNTHESIZE NEW SECTOR",
                                color = CyberMintLight,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Build customized 3D labyrinth with LLM prompt",
                                color = TextSecondaryDark,
                                fontSize = 11.sp
                            )
                        }

                        CyberPillButton(
                            text = if (showCreateDialog) "CLOSE" else "FORGE",
                            icon = if (showCreateDialog) null else Icons.Default.Add,
                            onClick = { showCreateDialog = !showCreateDialog },
                            isPrimary = !showCreateDialog,
                            testTag = "btn_toggle_forge"
                        )
                    }

                    if (showCreateDialog) {
                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = customThemeText,
                            onValueChange = { customThemeText = it },
                            label = { Text("Cyberpunk Sector Theme / Concept") },
                            placeholder = { Text("e.g. Orbital Penthouse, Shadow Core") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberMintPrimary,
                                unfocusedBorderColor = CyberCardBorder,
                                focusedTextColor = TextPrimaryDark,
                                unfocusedTextColor = TextPrimaryDark
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("input_custom_theme")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Normal", "Cyberpunk", "Nightmare").forEach { diff ->
                                val isSelected = customDifficulty == diff
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) CyberMintPrimary else Color(0xFF071912))
                                        .border(1.dp, if (isSelected) CyberMintLight else CyberCardBorder, RoundedCornerShape(12.dp))
                                        .clickable { customDifficulty = diff }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = diff,
                                        color = if (isSelected) Color(0xFF003822) else TextSecondaryDark,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (isGenerating) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(color = CyberMintPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = generationStatus, color = CyberMintLight, fontSize = 11.sp)
                            }
                        } else {
                            CyberPillButton(
                                text = "GENERATE 3D SECTOR WITH AI",
                                icon = Icons.Default.AutoAwesome,
                                onClick = {
                                    onGenerateCustomLevel(customThemeText, customDifficulty)
                                    showCreateDialog = false
                                },
                                isPrimary = true,
                                modifier = Modifier.fillMaxWidth().testTag("btn_forge_execute")
                            )
                        }
                    }
                }
            }
        }

        // List of Saved Room Levels
        if (savedLevels.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = TextMutedDark,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "NO SAVED SECTORS YET",
                            color = TextSecondaryDark,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Walk or climb to record movements, then tap 'Synthesize' in Motion Lab!",
                            color = TextMutedDark,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(savedLevels, key = { it.id }) { level ->
                LevelArchiveCard(
                    level = level,
                    formattedDate = dateFormat.format(Date(level.createdAt)),
                    onPlay = { onLoadLevel(level) },
                    onDelete = { onDeleteLevel(level.id) }
                )
            }
        }
    }
}

@Composable
private fun LevelArchiveCard(
    level: LevelEntity,
    formattedDate: String,
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    CyberCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (level.isCleared) CyberMintPrimary.copy(alpha = 0.5f) else CyberCardBorder
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = level.title,
                        color = TextPrimaryDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "$formattedDate • ${level.providerUsed} (${level.modelUsed})",
                        color = TextSecondaryDark,
                        fontSize = 11.sp
                    )
                }

                // Cleared Star Badge
                if (level.isCleared) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F3829))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Cleared",
                            tint = CyberMintPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${level.bestTimeSeconds}s",
                            color = CyberMintLight,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Telemetry Source Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Elevation Source
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF071912))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TrendingUp, null, tint = ElevationClimbColor, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+${String.format("%.1f", level.elevationGainSource)}m climb",
                            color = TextSecondaryDark,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Depression Source
                if (level.depressionSource > 0.3f) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF071912))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingDown, null, tint = DepressionDropColor, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "-${String.format("%.1f", level.depressionSource)}m drop",
                                color = TextSecondaryDark,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Step count
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF071912))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${level.stepCountSource} paces",
                        color = CyberCyanAccent,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp).testTag("btn_delete_level_${level.id}")
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = CyberLaserRed.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                }

                CyberPillButton(
                    text = "DEPLOY IN ARENA",
                    icon = Icons.Default.PlayArrow,
                    onClick = onPlay,
                    isPrimary = true,
                    testTag = "btn_play_level_${level.id}"
                )
            }
        }
    }
}
