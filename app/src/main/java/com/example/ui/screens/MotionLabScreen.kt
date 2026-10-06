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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.North
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.theme.CyberMintDark
import com.example.ui.theme.CyberMintLight
import com.example.ui.theme.CyberMintPrimary
import com.example.ui.theme.CyberSurfaceCard
import com.example.ui.theme.DepressionDropColor
import com.example.ui.theme.ElevationClimbColor
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MotionLabScreen(
    telemetry: MotionTelemetry,
    motionTracker: MotionTracker,
    isGenerating: Boolean,
    generationStatus: String,
    onSynthesizeLevel: (theme: String, difficulty: String) -> Unit
) {
    val scrollState = rememberScrollState()
    var selectedDifficulty by remember { mutableStateOf("Normal") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackgroundDark)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("motion_lab_screen")
    ) {
        // Header
        CyberSectionHeader(
            title = "MOTION SENSOR LAB",
            subtitle = "Real-time elevation, depression & spatial trajectory telemetry",
            badgeText = if (telemetry.isTracking) "ACTIVE" else "PAUSED"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Live Telemetry 2x2 Grid (Elevation Gain, Depression, Steps, Distance)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CyberMetricBadge(
                title = "Elevation Gain",
                value = String.format("+%.1f", telemetry.elevationGainMeters),
                unit = "m",
                icon = Icons.Default.TrendingUp,
                accentColor = ElevationClimbColor,
                modifier = Modifier.weight(1f).testTag("badge_elevation_gain")
            )
            CyberMetricBadge(
                title = "Depression",
                value = String.format("-%.1f", telemetry.depressionMeters),
                unit = "m",
                icon = Icons.Default.TrendingDown,
                accentColor = DepressionDropColor,
                modifier = Modifier.weight(1f).testTag("badge_depression")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CyberMetricBadge(
                title = "Total Paces",
                value = "${telemetry.stepCount}",
                unit = "steps",
                icon = Icons.Default.DirectionsWalk,
                accentColor = CyberCyanAccent,
                modifier = Modifier.weight(1f).testTag("badge_steps")
            )
            CyberMetricBadge(
                title = "Total Span",
                value = String.format("%.1f", telemetry.totalDistanceMeters),
                unit = "m",
                icon = Icons.Default.Explore,
                accentColor = CyberMintLight,
                modifier = Modifier.weight(1f).testTag("badge_distance")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Real-Time Vertical Elevation & Depression Waveform Graph
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = CyberMintPrimary.copy(alpha = 0.4f)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VERTICAL SENSOR PROFILE",
                        color = CyberMintLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Heading: ${telemetry.currentHeadingDegrees.toInt()}°",
                        color = TextSecondaryDark,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF071912))
                        .border(1.dp, Color(0xFF133B2C), RoundedCornerShape(12.dp))
                ) {
                    ElevationWaveformCanvas(
                        elevationHistory = telemetry.recentElevationHistory,
                        currentHeading = telemetry.currentHeadingDegrees
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Sensors: ${if (telemetry.hasBarometer) "Barometer (Hardware)" else "Barometer (Estimated)"} • Gyro • Linear Accel",
                        color = TextSecondaryDark,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${telemetry.durationSeconds}s elapsed",
                        color = CyberCyanAccent,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Session Control & Sensor Calibration / Simulation Tools (Crucial for emulators and indoors)
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFF0C241B)
        ) {
            Column {
                Text(
                    text = "SESSION CONTROLS & TEST SIMULATOR",
                    color = CyberMintLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Track live phone sensors or simulate stair climbing & pacing for testing.",
                    color = TextSecondaryDark,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CyberPillButton(
                        text = if (telemetry.isTracking) "PAUSE SENSOR" else "RESUME SENSOR",
                        icon = if (telemetry.isTracking) Icons.Default.Pause else Icons.Default.PlayArrow,
                        onClick = {
                            if (telemetry.isTracking) motionTracker.stopTracking()
                            else motionTracker.startTracking()
                        },
                        isPrimary = !telemetry.isTracking,
                        modifier = Modifier.weight(1f).testTag("btn_toggle_tracking")
                    )

                    CyberPillButton(
                        text = "RESET METRICS",
                        icon = Icons.Default.Refresh,
                        onClick = { motionTracker.resetSession() },
                        isPrimary = false,
                        modifier = Modifier.weight(1f).testTag("btn_reset_tracking")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Calibration / Simulation Quick Injectors
                Text(
                    text = "SIMULATE MOTION TELEMETRY (QUICK TEST):",
                    color = TextSecondaryDark,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = { motionTracker.simulateSteps(45, 32f, 8f) },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        modifier = Modifier.weight(1f).testTag("sim_walk_steps")
                    ) {
                        Text("+45 Steps", fontSize = 10.sp, color = CyberMintLight, fontFamily = FontFamily.Monospace, maxLines = 1, softWrap = false)
                    }

                    OutlinedButton(
                        onClick = { motionTracker.simulateElevation(4.2f, 0f) },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        modifier = Modifier.weight(1f).testTag("sim_climb_elevation")
                    ) {
                        Text("+4.2m Climb", fontSize = 10.sp, color = ElevationClimbColor, fontFamily = FontFamily.Monospace, maxLines = 1, softWrap = false)
                    }

                    OutlinedButton(
                        onClick = { motionTracker.simulateElevation(0f, 2.5f) },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        modifier = Modifier.weight(1f).testTag("sim_drop_depression")
                    ) {
                        Text("-2.5m Pit", fontSize = 10.sp, color = DepressionDropColor, fontFamily = FontFamily.Monospace, maxLines = 1, softWrap = false)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Level Synthesis CTA Card
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFF0F3325),
            borderColor = CyberMintPrimary,
            cornerRadius = 24.dp
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = CyberMintPrimary,
                    modifier = Modifier.size(36.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "SYNTHESIZE 3D PUZZLE LEVEL",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Compiles elevation (${String.format("%.1f", telemetry.elevationGainMeters)}m), depression (${String.format("%.1f", telemetry.depressionMeters)}m), and ${telemetry.stepCount} paces into an AI-structured 3D cyber labyrinth.",
                    color = TextSecondaryDark,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Difficulty selector pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    listOf("Normal", "Cyberpunk", "Nightmare").forEach { diff ->
                        val isSelected = selectedDifficulty == diff
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) CyberMintPrimary else Color(0xFF0C241B))
                                .border(1.dp, if (isSelected) CyberMintLight else CyberCardBorder, RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .testTag("difficulty_$diff")
                        ) {
                            Text(
                                text = diff.uppercase(),
                                color = if (isSelected) Color(0xFF003822) else TextSecondaryDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isGenerating) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = CyberMintPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = generationStatus,
                            color = CyberMintLight,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    CyberPillButton(
                        text = "BUILD 3D LEVEL FROM SENSORS",
                        icon = Icons.Default.AutoAwesome,
                        onClick = {
                            onSynthesizeLevel("Sector ${telemetry.stepCount}", selectedDifficulty)
                        },
                        isPrimary = true,
                        modifier = Modifier.fillMaxWidth().testTag("btn_build_level_from_sensors")
                    )
                }
            }
        }
    }
}

@Composable
private fun ElevationWaveformCanvas(
    elevationHistory: List<Float>,
    currentHeading: Float
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Background cyber grid lines
        for (i in 1..4) {
            val y = h * (i / 5f)
            drawLine(
                color = Color(0x1F00E599),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f
            )
        }

        // Draw compass arrow in the corner
        val compassCenter = Offset(w - 30.dp.toPx(), 28.dp.toPx())
        val rad = (currentHeading * PI / 180f).toFloat()
        val arrowTip = Offset(
            compassCenter.x + sin(rad) * 16.dp.toPx(),
            compassCenter.y - cos(rad) * 16.dp.toPx()
        )
        drawCircle(color = Color(0x3300E599), radius = 18.dp.toPx(), center = compassCenter)
        drawLine(color = CyberMintLight, start = compassCenter, end = arrowTip, strokeWidth = 2.5f)

        if (elevationHistory.isEmpty()) {
            // Baseline flat line
            drawLine(
                color = CyberMintPrimary,
                start = Offset(0f, h / 2f),
                end = Offset(w, h / 2f),
                strokeWidth = 2f
            )
            return@Canvas
        }

        val minAlt = elevationHistory.minOrNull() ?: 0f
        val maxAlt = elevationHistory.maxOrNull() ?: 10f
        val range = (maxAlt - minAlt).coerceAtLeast(1f)

        val path = Path()
        val stepX = w / (elevationHistory.size - 1).coerceAtLeast(1)

        elevationHistory.forEachIndexed { i, alt ->
            val px = i * stepX
            val normalized = (alt - minAlt) / range
            val py = h - (normalized * (h * 0.7f) + (h * 0.15f))
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }

        // Waveform stroke
        drawPath(
            path = path,
            color = CyberMintLight,
            style = Stroke(width = 3f)
        )
    }
}
