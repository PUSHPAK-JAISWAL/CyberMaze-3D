package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NearbyPlayerBase
import com.example.data.model.RadarNode
import com.example.data.sensor.MotionTelemetry
import com.example.data.sensor.MotionTracker
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberMetricBadge
import com.example.ui.components.CyberPillButton
import com.example.ui.components.CyberSectionHeader
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun OutdoorRadarScreen(
    telemetry: MotionTelemetry,
    motionTracker: MotionTracker,
    radarNodes: List<RadarNode>,
    nearbyBases: List<NearbyPlayerBase> = emptyList(),
    onClaimNode: (String) -> Unit,
    onTriggerReconDrone: () -> Unit,
    onAttackNearbyBase: (String) -> Unit,
    onStartRaid: () -> Unit
) {
    val scrollState = rememberScrollState()
    var sweepAngle by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(20)
            sweepAngle = (sweepAngle + 3f) % 360f
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackgroundDark)
            .verticalScroll(scrollState)
            .padding(12.dp)
            .testTag("outdoor_radar_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CyberSectionHeader(
            title = "REAL-WORLD OUTDOOR RADAR",
            subtitle = "Physical motion tracker: walking outside uncovers Darknet Nodes & charges satellite strikes",
            badgeText = if (telemetry.isTracking) "PEDOMETER ACTIVE" else "PAUSED"
        )

        // 1. Live Motion Telemetry Badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CyberMetricBadge(
                title = "STEPS",
                value = "${telemetry.stepCount}",
                unit = "WALKED",
                icon = Icons.Default.DirectionsWalk,
                accentColor = CyberMintPrimary,
                modifier = Modifier.weight(1f)
            )
            CyberMetricBadge(
                title = "ALTITUDE",
                value = String.format("+%.1f", telemetry.elevationGainMeters),
                unit = "M",
                icon = Icons.Default.TrendingUp,
                accentColor = CyberCyanAccent,
                modifier = Modifier.weight(1f)
            )
            CyberMetricBadge(
                title = "DISTANCE",
                value = String.format("%.0f", telemetry.totalDistanceMeters),
                unit = "M",
                icon = Icons.Default.Explore,
                accentColor = CyberAmberWarning,
                modifier = Modifier.weight(1f)
            )
        }

        // 2. WHY GO OUTSIDE? Core Compulsion & Reward Incentives Card
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFF092017),
            borderColor = CyberMintPrimary.copy(alpha = 0.6f),
            contentPadding = 12.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = CyberMintPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WHY GO OUTSIDE? TACTICAL ADVANTAGES:",
                        color = CyberMintLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "⚡ 2× Bits Per Step",
                            color = CyberMintLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Physical steps directly fund troop and turret upgrades faster than raids.",
                            color = TextSecondaryDark,
                            fontSize = 10.sp
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🛰️ Altitude Strike",
                            color = CyberCyanAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Climbing hills/stairs charges the Orbital Ion Cannon for massive raid airstrikes.",
                            color = TextSecondaryDark,
                            fontSize = 10.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "💎 Exclusive Nanites",
                            color = CyberPurpleNeon,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Quantum Nanites can ONLY be discovered at physical outdoor radar nodes.",
                            color = TextSecondaryDark,
                            fontSize = 10.sp
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "📦 Rare Blueprints",
                            color = CyberAmberWarning,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Walk to high-tier Darknet Vaults to crack open guaranteed troop blueprints.",
                            color = TextSecondaryDark,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // 3. Animated Circular Cyber Radar Scanner
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFF04120D),
            borderColor = CyberMintPrimary.copy(alpha = 0.5f),
            contentPadding = 16.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .border(2.dp, CyberMintPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val maxR = size.width / 2f

                        // Concentric Radar Rings
                        drawCircle(color = Color(0xFF0A2B1F), radius = maxR * 0.75f, center = center, style = Stroke(width = 1f))
                        drawCircle(color = Color(0xFF0A2B1F), radius = maxR * 0.50f, center = center, style = Stroke(width = 1f))
                        drawCircle(color = Color(0xFF0A2B1F), radius = maxR * 0.25f, center = center, style = Stroke(width = 1f))

                        // Crosshairs
                        drawLine(color = Color(0xFF0D3627), start = Offset(0f, center.y), end = Offset(size.width, center.y), strokeWidth = 1f)
                        drawLine(color = Color(0xFF0D3627), start = Offset(center.x, 0f), end = Offset(center.x, size.height), strokeWidth = 1f)

                        // Sweeping radar beam line
                        val rad = (sweepAngle * PI / 180f).toFloat()
                        val beamTip = Offset(
                            center.x + (cos(rad) * maxR),
                            center.y + (sin(rad) * maxR)
                        )
                        drawLine(
                            color = CyberMintPrimary.copy(alpha = 0.7f),
                            start = center,
                            end = beamTip,
                            strokeWidth = 2.5f
                        )

                        // Draw Radar Blips for Nodes
                        for (node in radarNodes) {
                            val blipRad = (node.angleDegrees * PI / 180f).toFloat()
                            val distFactor = (node.distanceMeters / 600f).coerceIn(0.2f, 0.85f)
                            val blipPos = Offset(
                                center.x + (cos(blipRad) * maxR * distFactor),
                                center.y + (sin(blipRad) * maxR * distFactor)
                            )

                            val blipColor = if (node.isUnlocked) CyberMintPrimary else CyberAmberWarning
                            drawCircle(color = blipColor, radius = 5.5f, center = blipPos)
                            drawCircle(color = blipColor.copy(alpha = 0.4f), radius = 9f, center = blipPos, style = Stroke(width = 1f))
                        }

                        // Draw Radar Blips for Nearby Player Bases (Pokemon GO proximity markers)
                        for (base in nearbyBases) {
                            val blipRad = (base.angleDegrees * PI / 180f).toFloat()
                            val distFactor = (base.distanceMeters / 600f).coerceIn(0.25f, 0.88f)
                            val blipPos = Offset(
                                center.x + (cos(blipRad) * maxR * distFactor),
                                center.y + (sin(blipRad) * maxR * distFactor)
                            )

                            drawCircle(color = CyberLaserRed, radius = 6.5f, center = blipPos)
                            drawCircle(color = CyberLaserRed.copy(alpha = 0.45f), radius = 11f, center = blipPos, style = Stroke(width = 1.5f))
                        }
                    }

                    // Center user blip
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(CyberCyanAccent)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIVE SENSOR RADAR SCANNING...",
                        color = CyberMintLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    // Virtual Recon Drone Button (Indoor / Testing fallback)
                    CyberPillButton(
                        text = "RECON DRONE (+150m)",
                        icon = Icons.Default.RocketLaunch,
                        onClick = onTriggerReconDrone,
                        isPrimary = false
                    )
                }
            }
        }

        // 4. Outdoor Syndicate Nodes to Claim
        Text(
            text = "DETECTED OUTDOOR CACHES (PHYSICALLY WALK TO DECRYPT):",
            color = TextSecondaryDark,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )

        for (node in radarNodes) {
            val isUnlocked = telemetry.stepCount >= node.requiredSteps || node.isUnlocked
            val progress = (telemetry.stepCount.toFloat() / node.requiredSteps.toFloat()).coerceIn(0f, 1f)

            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xFF071912),
                borderColor = if (isUnlocked && !node.isClaimed) CyberMintPrimary else CyberCardBorder,
                contentPadding = 12.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                imageVector = if (node.isClaimed) Icons.Default.CheckCircle else if (isUnlocked) Icons.Default.CardGiftcard else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (node.isClaimed) TextMutedDark else if (isUnlocked) CyberMintPrimary else CyberAmberWarning,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = node.title,
                                    color = TextPrimaryDark,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${node.distanceMeters}m • ${node.requiredSteps} steps",
                                    color = TextSecondaryDark,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Rewards
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "+${node.bitsReward}⚡ +${node.nanitesReward}💎",
                                color = CyberCyanAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                softWrap = false
                            )
                            Text(
                                text = node.blueprintReward,
                                color = CyberAmberWarning,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                softWrap = false
                            )
                        }
                    }

                    // Step Progress Bar
                    if (!node.isClaimed) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = CyberMintPrimary,
                            trackColor = Color(0xFF0F261D)
                        )
                    }

                    // Claim Button
                    if (isUnlocked && !node.isClaimed) {
                        Button(
                            onClick = { onClaimNode(node.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberMintPrimary),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Text(
                                text = "DECRYPT & CLAIM OUTDOOR LOOT",
                                color = Color(0xFF003822),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // 5. Pokemon GO Proximity Player Bases: Nearby Players Detected in Physical Vicinity!
        if (nearbyBases.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SportsKabaddi,
                    contentDescription = null,
                    tint = CyberLaserRed,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "NEARBY PLAYERS IN PHYSICAL PROXIMITY (POKÉMON GO STYLE):",
                    color = CyberLaserRed,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "When other players running CyberMaze are physically near you, their custom CyberMazes ping your radar! Breach their fortress to loot their bits & trophies:",
                color = TextSecondaryDark,
                fontSize = 10.sp,
                lineHeight = 14.sp
            )

            for (base in nearbyBases) {
                CyberCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFF1F0B10),
                    borderColor = CyberLaserRed.copy(alpha = 0.6f),
                    contentPadding = 12.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF3B121C)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = CyberLaserRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = base.architectName,
                                        color = TextPrimaryDark,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "${base.rankTitle} • ${base.distanceMeters}m away • ${base.trophyCount} 🏆",
                                        color = TextSecondaryDark,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "LOOT: +${base.lootableBits}⚡",
                                    color = CyberMintLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${base.buildings.size} Defenses",
                                    color = CyberAmberWarning,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Attack Base Action Button
                        Button(
                            onClick = { onAttackNearbyBase(base.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberLaserRed),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("attack_nearby_base_${base.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsKabaddi,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "INFILTRATE & ATTACK THIS PLAYER'S BASE",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
