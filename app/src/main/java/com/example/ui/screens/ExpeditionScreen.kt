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
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.ui.theme.CyberMintLight
import com.example.ui.theme.CyberMintPrimary
import com.example.ui.theme.DepressionDropColor
import com.example.ui.theme.ElevationClimbColor
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun ExpeditionScreen(
    telemetry: MotionTelemetry,
    motionTracker: MotionTracker,
    isGenerating: Boolean,
    generationStatus: String,
    onSynthesizeSector: (theme: String, difficulty: String) -> Unit,
    onStartRun: () -> Unit
) {
    val scrollState = rememberScrollState()
    var selectedDifficulty by remember { mutableStateOf("Cyberpunk") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackgroundDark)
            .verticalScroll(scrollState)
            .padding(14.dp)
            .testTag("expedition_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CyberSectionHeader(
            title = "OUTDOOR EXPEDITION LAB",
            subtitle = "Move outside to power up runner energy, climb elevation & earn chests",
            badgeText = if (telemetry.isTracking) "TRACKING ACTIVE" else "PAUSED"
        )

        // Outdoor Incentives Banner
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFF0F3526),
            borderColor = CyberMintPrimary.copy(alpha = 0.5f),
            contentPadding = 12.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsWalk,
                    contentDescription = null,
                    tint = CyberMintLight,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "WALK OUTSIDE TO RECHARGE",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Every 100 paces recharges +50 Energy & unlocks Expedition Chests in the Deck tab!",
                        color = CyberMintLight.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        // Live Telemetry 2x2 Badges
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
                title = "Distance",
                value = String.format("%.1f", telemetry.totalDistanceMeters),
                unit = "m",
                icon = Icons.Default.Explore,
                accentColor = CyberMintLight,
                modifier = Modifier.weight(1f).testTag("badge_distance")
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CyberMetricBadge(
                title = "Elevation Climb",
                value = String.format("+%.1f", telemetry.elevationGainMeters),
                unit = "m",
                icon = Icons.Default.TrendingUp,
                accentColor = ElevationClimbColor,
                modifier = Modifier.weight(1f).testTag("badge_elevation_gain")
            )
            CyberMetricBadge(
                title = "Depression Drop",
                value = String.format("-%.1f", telemetry.depressionMeters),
                unit = "m",
                icon = Icons.Default.TrendingDown,
                accentColor = DepressionDropColor,
                modifier = Modifier.weight(1f).testTag("badge_depression")
            )
        }

        // Real-Time Vertical Waveform Graph
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = CyberMintPrimary.copy(alpha = 0.4f),
            contentPadding = 12.dp
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
                        .height(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF071912))
                        .border(1.dp, Color(0xFF133B2C), RoundedCornerShape(12.dp))
                ) {
                    ElevationWaveformCanvas(
                        elevationHistory = telemetry.recentElevationHistory
                    )
                }
            }
        }

        // Tracking Controls & Simulation Injectors (Indoor testing)
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFF0C241B),
            contentPadding = 12.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CyberPillButton(
                        text = if (telemetry.isTracking) "PAUSE SENSORS" else "START TRACKING",
                        icon = if (telemetry.isTracking) Icons.Default.Pause else Icons.Default.PlayArrow,
                        onClick = {
                            if (telemetry.isTracking) motionTracker.stopTracking()
                            else motionTracker.startTracking()
                        },
                        isPrimary = !telemetry.isTracking,
                        modifier = Modifier.weight(1f),
                        fontSize = 11.sp
                    )

                    CyberPillButton(
                        text = "RESET METRICS",
                        icon = Icons.Default.Refresh,
                        onClick = { motionTracker.resetSession() },
                        isPrimary = false,
                        modifier = Modifier.weight(1f),
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = "SIMULATE MOTION TELEMETRY (QUICK TEST):",
                    color = TextSecondaryDark,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = { motionTracker.simulateSteps(60, 42f, 12f) },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f).testTag("sim_walk_steps")
                    ) {
                        Text("+60 Steps", fontSize = 10.sp, color = CyberMintLight, fontFamily = FontFamily.Monospace, maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = { motionTracker.simulateElevation(5.5f, 0f) },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f).testTag("sim_climb_elevation")
                    ) {
                        Text("+5.5m Climb", fontSize = 10.sp, color = ElevationClimbColor, fontFamily = FontFamily.Monospace, maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = { motionTracker.simulateElevation(0f, 3.2f) },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f).testTag("sim_drop_depression")
                    ) {
                        Text("-3.2m Drop", fontSize = 10.sp, color = DepressionDropColor, fontFamily = FontFamily.Monospace, maxLines = 1)
                    }
                }
            }
        }

        // AI Sector Synthesizer (Translates outdoor telemetry to 3D runner run)
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFF0F3325),
            borderColor = CyberMintPrimary,
            cornerRadius = 20.dp,
            contentPadding = 14.dp
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = CyberMintPrimary,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "AI EXPEDITION SECTOR SYNTHESIZER",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Compiles elevation (${String.format("%.1f", telemetry.elevationGainMeters)}m), depression (${String.format("%.1f", telemetry.depressionMeters)}m), and ${telemetry.stepCount} real paces into a custom AI Sector Boss Run!",
                    color = TextSecondaryDark,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (isGenerating) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(color = CyberMintPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = generationStatus, color = CyberMintLight, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                } else {
                    Button(
                        onClick = { onSynthesizeSector("Expedition Sector", selectedDifficulty) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberMintPrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(46.dp).testTag("btn_synthesize_expedition")
                    ) {
                        Icon(Icons.Default.RocketLaunch, null, tint = Color(0xFF003822), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SYNTHESIZE & LAUNCH SECTOR",
                            color = Color(0xFF003822),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ElevationWaveformCanvas(
    elevationHistory: List<Float>
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Midline
        drawLine(
            color = Color(0x3300E599),
            start = Offset(0f, h / 2f),
            end = Offset(w, h / 2f),
            strokeWidth = 1f
        )

        if (elevationHistory.size < 2) {
            drawLine(
                color = CyberMintLight,
                start = Offset(0f, h / 2f),
                end = Offset(w, h / 2f),
                strokeWidth = 2f
            )
            return@Canvas
        }

        val stepX = w / (elevationHistory.size - 1)
        val path = Path()

        elevationHistory.forEachIndexed { i, alt ->
            val clamped = alt.coerceIn(-10f, 10f)
            val y = (h / 2f) - (clamped / 10f) * (h * 0.4f)
            val x = i * stepX
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = CyberMintPrimary,
            style = Stroke(width = 2.5f)
        )
    }
}
