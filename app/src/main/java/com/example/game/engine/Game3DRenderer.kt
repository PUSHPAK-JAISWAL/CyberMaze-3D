package com.example.game.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.data.model.CollectibleItem
import com.example.data.model.CollectibleType
import com.example.data.model.ObstacleItem
import com.example.data.model.ObstacleType
import com.example.data.model.RunnerGameState
import com.example.ui.theme.CyberAmberWarning
import com.example.ui.theme.CyberCyanAccent
import com.example.ui.theme.CyberLaserRed
import com.example.ui.theme.CyberMintDark
import com.example.ui.theme.CyberMintLight
import com.example.ui.theme.CyberMintPrimary
import com.example.ui.theme.CyberPurpleNeon
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class Game3DRenderer {

    fun renderScene(
        drawScope: DrawScope,
        state: RunnerGameState,
        animTicks: Long
    ) {
        val width = drawScope.size.width
        val height = drawScope.size.height

        val centerX = width / 2f
        val horizonY = height * 0.28f
        val trackBottomY = height * 0.88f
        val maxViewDistance = 75f

        // Helper 3D Projection to 2D Screen
        fun project(laneX: Float, zDistance: Float, altitudeY: Float = 0f): Offset {
            val t = (zDistance / maxViewDistance).coerceIn(0f, 1f)
            val perspectiveScale = 1f - (t * 0.82f)
            val sy = horizonY + (trackBottomY - horizonY) * (1f - t) - (altitudeY * perspectiveScale * 90f)
            val laneSpread = (width * 0.32f) * perspectiveScale
            val sx = centerX + (laneX * laneSpread)
            return Offset(sx, sy)
        }

        // 1. Cyber Skyline & Neon Grid Background
        drawSkylineAndGrid(drawScope, width, height, horizonY, trackBottomY, animTicks, state.distanceMeters)

        // 2. 3D Highway Lanes Track
        draw3DLanes(drawScope, centerX, horizonY, trackBottomY, width, state.distanceMeters)

        // 3. Collectibles on Track
        state.collectibles.filter { it.zDistance in 0f..maxViewDistance }.sortedByDescending { it.zDistance }.forEach { col ->
            drawCollectible(drawScope, col, ::project, animTicks)
        }

        // 4. Obstacles on Track
        state.obstacles.filter { it.zDistance in -2f..maxViewDistance }.sortedByDescending { it.zDistance }.forEach { obs ->
            drawObstacle(drawScope, obs, ::project, animTicks)
        }

        // 5. Pursuing Hunter Boss Drone (Behind player)
        drawHunterDrone(drawScope, centerX, horizonY, trackBottomY, state.bossDroneDistance, animTicks)

        // 6. The Operative (Player Runner)
        drawPlayerOperative(drawScope, state, ::project, animTicks)

        // 7. Warp Speed Lines (if active)
        if (state.isWarpActive) {
            drawSpeedLines(drawScope, width, height, centerX, horizonY, animTicks)
        }
    }

    private fun drawSkylineAndGrid(
        drawScope: DrawScope,
        width: Float,
        height: Float,
        horizonY: Float,
        trackBottomY: Float,
        animTicks: Long,
        distanceMeters: Float
    ) {
        // Dark Cyber Sky
        drawScope.drawRect(
            brush = Brush.verticalGradient(
                listOf(
                    Color(0xFF040E0A),
                    Color(0xFF071912),
                    Color(0xFF0C241B)
                ),
                startY = 0f,
                endY = horizonY
            ),
            size = Size(width, horizonY)
        )

        // Distant Cyber Skyscrapers & Neon Spines
        val numBuildings = 9
        for (i in 0 until numBuildings) {
            val bWidth = width / numBuildings
            val bHeight = 40f + (sin((i * 1.7f) + 1f) * 35f).coerceAtLeast(15f)
            val bx = i * bWidth
            val by = horizonY - bHeight

            val buildingColor = if (i % 2 == 0) Color(0xFF081C15) else Color(0xFF061510)
            drawScope.drawRect(
                color = buildingColor,
                topLeft = Offset(bx + 4f, by),
                size = Size(bWidth - 8f, bHeight)
            )

            // Neon Window Sparks
            val glowColor = if (i % 3 == 0) CyberMintPrimary.copy(alpha = 0.5f) else CyberCyanAccent.copy(alpha = 0.4f)
            drawScope.drawCircle(
                color = glowColor,
                radius = 2f,
                center = Offset(bx + bWidth / 2f, by + 12f)
            )
        }

        // Pulsing Neon Horizon Line
        drawScope.drawLine(
            color = CyberMintLight.copy(alpha = 0.7f),
            start = Offset(0f, horizonY),
            end = Offset(width, horizonY),
            strokeWidth = 2.5f
        )
    }

    private fun draw3DLanes(
        drawScope: DrawScope,
        centerX: Float,
        horizonY: Float,
        trackBottomY: Float,
        width: Float,
        distanceMeters: Float
    ) {
        // Ground Mesh under track
        drawScope.drawRect(
            color = Color(0xFF05140F),
            topLeft = Offset(0f, horizonY),
            size = Size(width, drawScope.size.height - horizonY)
        )

        val bottomTrackWidth = width * 0.94f
        val topTrackWidth = width * 0.16f

        val leftTop = Offset(centerX - topTrackWidth / 2f, horizonY)
        val rightTop = Offset(centerX + topTrackWidth / 2f, horizonY)
        val rightBottom = Offset(centerX + bottomTrackWidth / 2f, trackBottomY)
        val leftBottom = Offset(centerX - bottomTrackWidth / 2f, trackBottomY)

        // 3D Highway Road Surface
        val roadPath = Path().apply {
            moveTo(leftTop.x, leftTop.y)
            lineTo(rightTop.x, rightTop.y)
            lineTo(rightBottom.x, rightBottom.y)
            lineTo(leftBottom.x, leftBottom.y)
            close()
        }
        drawScope.drawPath(
            path = roadPath,
            brush = Brush.verticalGradient(
                listOf(Color(0xFF092018), Color(0xFF0D2D22), Color(0xFF071912)),
                startY = horizonY,
                endY = trackBottomY
            )
        )

        // Outer Neon Guardrails
        drawScope.drawLine(color = CyberMintPrimary, start = leftTop, end = leftBottom, strokeWidth = 3f)
        drawScope.drawLine(color = CyberMintPrimary, start = rightTop, end = rightBottom, strokeWidth = 3f)

        // Lane Dividers (-1, 0, 1 -> 2 divider lines)
        for (divider in listOf(-0.33f, 0.33f)) {
            val dTop = Offset(centerX + (topTrackWidth / 2f) * divider, horizonY)
            val dBottom = Offset(centerX + (bottomTrackWidth / 2f) * divider, trackBottomY)
            drawScope.drawLine(
                color = Color(0x6600E599),
                start = dTop,
                end = dBottom,
                strokeWidth = 1.5f
            )
        }

        // Horizontal Moving Grid Ties (creates high speed illusion)
        val offset = (distanceMeters % 10f) / 10f
        for (i in 1..10) {
            val frac = ((i.toFloat() / 10f) + offset) % 1f
            val t = frac * frac // Perspective compression toward horizon
            val y = horizonY + (trackBottomY - horizonY) * t
            val currentW = topTrackWidth + (bottomTrackWidth - topTrackWidth) * t
            val lx = centerX - currentW / 2f
            val rx = centerX + currentW / 2f

            drawScope.drawLine(
                color = Color(0x3300E599),
                start = Offset(lx, y),
                end = Offset(rx, y),
                strokeWidth = 1.2f
            )
        }
    }

    private fun drawCollectible(
        drawScope: DrawScope,
        item: CollectibleItem,
        project: (Float, Float, Float) -> Offset,
        animTicks: Long
    ) {
        val pos = project(item.lane.index.toFloat(), item.zDistance, item.elevationOffset + 0.3f)
        val t = (item.zDistance / 75f).coerceIn(0f, 1f)
        val scale = (1f - (t * 0.8f)) * 18f

        when (item.type) {
            CollectibleType.NEON_BIT -> {
                // Spinning Gold Cyber Bit
                val spin = (animTicks * 8f) % 360f
                val rad = (spin * PI / 180f).toFloat()
                val radius = scale * 0.6f

                drawScope.drawCircle(
                    color = CyberAmberWarning.copy(alpha = 0.4f),
                    radius = radius * 1.5f,
                    center = pos
                )
                drawScope.drawCircle(
                    color = CyberAmberWarning,
                    radius = radius,
                    center = pos
                )
                drawScope.drawCircle(
                    color = Color.White,
                    radius = radius * 0.4f,
                    center = Offset(pos.x + cos(rad) * radius * 0.4f, pos.y + sin(rad) * radius * 0.4f)
                )
            }
            CollectibleType.ENERGY_CORE -> {
                // Pulsing Cyan Battery Core
                drawScope.drawCircle(color = CyberCyanAccent.copy(alpha = 0.45f), radius = scale * 0.9f, center = pos)
                drawScope.drawCircle(color = CyberCyanAccent, radius = scale * 0.6f, center = pos)
                drawScope.drawCircle(color = Color.White, radius = scale * 0.3f, center = pos)
            }
            CollectibleType.MAGNET -> {
                drawScope.drawCircle(color = CyberPurpleNeon, radius = scale * 0.7f, center = pos, style = Stroke(width = 3f))
                drawScope.drawCircle(color = Color.White, radius = scale * 0.3f, center = pos)
            }
            CollectibleType.CYBER_SHIELD -> {
                drawScope.drawCircle(color = CyberMintLight, radius = scale * 0.8f, center = pos, style = Stroke(width = 3f))
                drawScope.drawCircle(color = Color(0x6600E599), radius = scale * 0.5f, center = pos)
            }
            CollectibleType.MULTIPLIER_CHIP -> {
                drawScope.drawRect(
                    color = CyberLaserRed,
                    topLeft = Offset(pos.x - scale * 0.5f, pos.y - scale * 0.5f),
                    size = Size(scale, scale)
                )
            }
        }
    }

    private fun drawObstacle(
        drawScope: DrawScope,
        obs: ObstacleItem,
        project: (Float, Float, Float) -> Offset,
        animTicks: Long
    ) {
        if (obs.isDestroyed) {
            // Shattered particle burst
            val center = project(obs.lane.index.toFloat(), obs.zDistance, 0.4f)
            drawScope.drawCircle(color = CyberLaserRed.copy(alpha = 0.6f), radius = 16f, center = center)
            drawScope.drawCircle(color = CyberMintLight, radius = 6f, center = center)
            return
        }

        val t = (obs.zDistance / 75f).coerceIn(0f, 1f)
        val scale = (1f - (t * 0.8f))

        when (obs.type) {
            ObstacleType.LOW_BARRIER -> {
                // Jump over barrier (Subway Surfers hurdle)
                val base = project(obs.lane.index.toFloat(), obs.zDistance, 0f)
                val top = project(obs.lane.index.toFloat(), obs.zDistance, 0.85f)
                val bWidth = (drawScope.size.width * 0.22f) * scale
                val bHeight = abs(top.y - base.y).coerceAtLeast(12f)

                // Barrier Face
                drawScope.drawRect(
                    color = Color(0xFF1E0A10),
                    topLeft = Offset(base.x - bWidth / 2f, top.y),
                    size = Size(bWidth, bHeight)
                )
                drawScope.drawRect(
                    color = CyberLaserRed,
                    topLeft = Offset(base.x - bWidth / 2f, top.y),
                    size = Size(bWidth, bHeight),
                    style = Stroke(width = 2.5f)
                )

                // Hazard Diagonal Stripe
                drawScope.drawLine(
                    color = CyberAmberWarning,
                    start = Offset(base.x - bWidth / 2f + 4f, top.y + 4f),
                    end = Offset(base.x + bWidth / 2f - 4f, base.y - 4f),
                    strokeWidth = 2f
                )
            }
            ObstacleType.HIGH_LASER -> {
                // Slide under laser gate (overhead energy beam)
                val beamPos = project(obs.lane.index.toFloat(), obs.zDistance, 1.1f)
                val bWidth = (drawScope.size.width * 0.26f) * scale

                // Posts on sides
                val postHeight = 35f * scale
                drawScope.drawLine(
                    color = CyberCyanAccent,
                    start = Offset(beamPos.x - bWidth / 2f, beamPos.y + postHeight),
                    end = Offset(beamPos.x - bWidth / 2f, beamPos.y),
                    strokeWidth = 3f
                )
                drawScope.drawLine(
                    color = CyberCyanAccent,
                    start = Offset(beamPos.x + bWidth / 2f, beamPos.y + postHeight),
                    end = Offset(beamPos.x + bWidth / 2f, beamPos.y),
                    strokeWidth = 3f
                )

                // High Laser Beam (must slide underneath)
                drawScope.drawLine(
                    color = CyberLaserRed,
                    start = Offset(beamPos.x - bWidth / 2f, beamPos.y),
                    end = Offset(beamPos.x + bWidth / 2f, beamPos.y),
                    strokeWidth = 4f
                )
                drawScope.drawLine(
                    color = Color.White,
                    start = Offset(beamPos.x - bWidth / 2f, beamPos.y),
                    end = Offset(beamPos.x + bWidth / 2f, beamPos.y),
                    strokeWidth = 1.5f
                )
            }
            ObstacleType.GLITCH_PIT -> {
                // Digital void trench
                val pitPos = project(obs.lane.index.toFloat(), obs.zDistance, 0f)
                val pWidth = (drawScope.size.width * 0.24f) * scale
                val pLength = 22f * scale

                drawScope.drawOval(
                    color = Color(0xFF000504),
                    topLeft = Offset(pitPos.x - pWidth / 2f, pitPos.y - pLength / 2f),
                    size = Size(pWidth, pLength)
                )
                drawScope.drawOval(
                    color = CyberPurpleNeon,
                    topLeft = Offset(pitPos.x - pWidth / 2f, pitPos.y - pLength / 2f),
                    size = Size(pWidth, pLength),
                    style = Stroke(width = 2f)
                )
            }
            ObstacleType.ELEVATED_RAMP -> {
                // Neon ascending skyway ramp
                val rampBase = project(obs.lane.index.toFloat(), obs.zDistance, 0f)
                val rampTop = project(obs.lane.index.toFloat(), obs.zDistance + 4f, 1.2f)
                val rWidth = (drawScope.size.width * 0.25f) * scale

                val rampPath = Path().apply {
                    moveTo(rampBase.x - rWidth / 2f, rampBase.y)
                    lineTo(rampBase.x + rWidth / 2f, rampBase.y)
                    lineTo(rampTop.x + rWidth * 0.4f, rampTop.y)
                    lineTo(rampTop.x - rWidth * 0.4f, rampTop.y)
                    close()
                }
                drawScope.drawPath(rampPath, color = Color(0xFF133B2C))
                drawScope.drawPath(rampPath, color = CyberMintLight, style = Stroke(width = 2.5f))
            }
            ObstacleType.SECURITY_DRONE -> {
                val dronePos = project(obs.lane.index.toFloat(), obs.zDistance, 0.7f)
                drawScope.drawCircle(color = CyberLaserRed, radius = 14f * scale, center = dronePos)
                drawScope.drawCircle(color = Color.White, radius = 5f * scale, center = dronePos)
            }
        }
    }

    private fun drawHunterDrone(
        drawScope: DrawScope,
        centerX: Float,
        horizonY: Float,
        trackBottomY: Float,
        droneDistanceMeters: Float,
        animTicks: Long
    ) {
        // Drone is hovering behind the player (e.g. 5m to 16m)
        val dangerFraction = (1f - (droneDistanceMeters / 18f)).coerceIn(0f, 1f)
        val droneScreenY = trackBottomY + 15f - (dangerFraction * 65f)
        val droneScale = 1f + (dangerFraction * 0.6f)

        val bobbing = sin(animTicks * 0.18f) * 6f
        val droneCenter = Offset(centerX, droneScreenY + bobbing)

        // Threat Spotlight onto track
        val spotLightColor = if (dangerFraction > 0.5f) Color(0x55FF3366) else Color(0x33FFB703)
        drawScope.drawOval(
            color = spotLightColor,
            topLeft = Offset(centerX - 80f * droneScale, trackBottomY - 40f),
            size = Size(160f * droneScale, 50f)
        )

        // Hunter Drone Hull
        val droneWidth = 70f * droneScale
        val droneHeight = 24f * droneScale
        drawScope.drawOval(
            color = Color(0xFF1F0B13),
            topLeft = Offset(droneCenter.x - droneWidth / 2f, droneCenter.y - droneHeight / 2f),
            size = Size(droneWidth, droneHeight)
        )
        drawScope.drawOval(
            color = if (dangerFraction > 0.5f) CyberLaserRed else CyberAmberWarning,
            topLeft = Offset(droneCenter.x - droneWidth / 2f, droneCenter.y - droneHeight / 2f),
            size = Size(droneWidth, droneHeight),
            style = Stroke(width = 2.5f)
        )

        // Red Central Threat Eye
        drawScope.drawCircle(
            color = CyberLaserRed,
            radius = 7f * droneScale,
            center = droneCenter
        )
        drawScope.drawCircle(
            color = Color.White,
            radius = 3f * droneScale,
            center = droneCenter
        )

        // Side Thruster Flames
        drawScope.drawCircle(color = CyberCyanAccent, radius = 4f * droneScale, center = Offset(droneCenter.x - droneWidth * 0.45f, droneCenter.y + 4f))
        drawScope.drawCircle(color = CyberCyanAccent, radius = 4f * droneScale, center = Offset(droneCenter.x + droneWidth * 0.45f, droneCenter.y + 4f))
    }

    private fun drawPlayerOperative(
        drawScope: DrawScope,
        state: RunnerGameState,
        project: (Float, Float, Float) -> Offset,
        animTicks: Long
    ) {
        val playerScreenPos = project(state.lanePosition, 0f, state.playerY)
        val bobbing = if (state.isJumping || state.isSliding) 0f else sin(animTicks * 0.45f) * 3f
        val pos = Offset(playerScreenPos.x, playerScreenPos.y + bobbing)

        // 1. Dynamic Floor Shadow
        val shadowWidth = if (state.isSliding) 55f else 36f
        val shadowY = project(state.lanePosition, 0f, 0f).y
        drawScope.drawOval(
            color = Color(0x6600E599),
            topLeft = Offset(pos.x - shadowWidth / 2f, shadowY - 8f),
            size = Size(shadowWidth, 14f)
        )

        if (state.isSliding) {
            // Sliding Pose: Low cyber capsule gliding horizontally
            val slideW = 54f
            val slideH = 18f
            drawScope.drawRoundRect(
                color = CyberMintDark,
                topLeft = Offset(pos.x - slideW / 2f, pos.y - slideH),
                size = Size(slideW, slideH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
            )
            drawScope.drawRoundRect(
                color = CyberMintLight,
                topLeft = Offset(pos.x - slideW / 2f, pos.y - slideH),
                size = Size(slideW, slideH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
                style = Stroke(width = 2f)
            )
            // Visor
            drawScope.drawOval(
                color = CyberCyanAccent,
                topLeft = Offset(pos.x + 8f, pos.y - slideH + 4f),
                size = Size(14f, 8f)
            )
            // Spark trail on road
            drawScope.drawCircle(color = CyberAmberWarning, radius = 3f, center = Offset(pos.x - 22f, pos.y - 2f))
            drawScope.drawCircle(color = Color.White, radius = 2f, center = Offset(pos.x - 28f, pos.y - 4f))
        } else {
            // Running / Jumping Pose
            val charW = 30f
            val charH = 50f
            val bodyTop = pos.y - charH

            // Torso
            drawScope.drawRoundRect(
                color = CyberMintDark,
                topLeft = Offset(pos.x - charW / 2f, bodyTop + 14f),
                size = Size(charW, 26f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
            )
            drawScope.drawRoundRect(
                color = CyberMintPrimary,
                topLeft = Offset(pos.x - charW / 2f, bodyTop + 14f),
                size = Size(charW, 26f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),
                style = Stroke(width = 2f)
            )

            // Neon Cyber Helmet
            drawScope.drawCircle(color = Color(0xFF0F3829), radius = 13f, center = Offset(pos.x, bodyTop + 8f))
            drawScope.drawCircle(color = CyberMintLight, radius = 13f, center = Offset(pos.x, bodyTop + 8f), style = Stroke(width = 1.8f))

            // Visor (Glowing Cyan)
            drawScope.drawOval(
                color = CyberCyanAccent,
                topLeft = Offset(pos.x - 9f, bodyTop + 5f),
                size = Size(18f, 8f)
            )

            // Running Legs / Jump Boosters
            if (state.isJumping) {
                // Jet Thrusters firing downwards
                drawScope.drawCircle(color = CyberCyanAccent, radius = 6f, center = Offset(pos.x - 8f, pos.y))
                drawScope.drawCircle(color = CyberCyanAccent, radius = 6f, center = Offset(pos.x + 8f, pos.y))
                drawScope.drawCircle(color = Color.White, radius = 3f, center = Offset(pos.x - 8f, pos.y + 3f))
                drawScope.drawCircle(color = Color.White, radius = 3f, center = Offset(pos.x + 8f, pos.y + 3f))
            } else {
                // Running feet
                val legCycle = sin(animTicks * 0.5f) * 10f
                drawScope.drawCircle(color = CyberMintLight, radius = 5f, center = Offset(pos.x - 8f, pos.y + legCycle))
                drawScope.drawCircle(color = CyberMintLight, radius = 5f, center = Offset(pos.x + 8f, pos.y - legCycle))
            }
        }

        // 2. Shield Matrix Bubble (if armed)
        if (state.hasShield) {
            drawScope.drawCircle(
                color = CyberMintLight,
                radius = 38f,
                center = Offset(pos.x, pos.y - 25f),
                style = Stroke(width = 2.5f)
            )
            drawScope.drawCircle(
                color = Color(0x3300E599),
                radius = 38f,
                center = Offset(pos.x, pos.y - 25f)
            )
        }
    }

    private fun drawSpeedLines(
        drawScope: DrawScope,
        width: Float,
        height: Float,
        centerX: Float,
        horizonY: Float,
        animTicks: Long
    ) {
        val numLines = 14
        for (i in 0 until numLines) {
            val angle = (i * (360f / numLines) + (animTicks * 12f)) % 360f
            val rad = (angle * PI / 180f).toFloat()
            val startDist = 60f + ((animTicks * 15f + i * 30f) % 180f)
            val endDist = startDist + 50f

            val sx = centerX + cos(rad) * startDist
            val sy = horizonY + sin(rad) * startDist
            val ex = centerX + cos(rad) * endDist
            val ey = horizonY + sin(rad) * endDist

            drawScope.drawLine(
                color = CyberCyanAccent.copy(alpha = 0.6f),
                start = Offset(sx, sy),
                end = Offset(ex, ey),
                strokeWidth = 2f
            )
        }
    }
}
