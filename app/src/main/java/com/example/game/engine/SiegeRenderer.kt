package com.example.game.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.data.model.ActiveTroop
import com.example.data.model.DefenseType
import com.example.data.model.MazeBuilding
import com.example.data.model.RaidBattleState
import com.example.data.model.TroopType
import com.example.ui.theme.CyberAmberWarning
import com.example.ui.theme.CyberCyanAccent
import com.example.ui.theme.CyberLaserRed
import com.example.ui.theme.CyberMintLight
import com.example.ui.theme.CyberMintPrimary
import com.example.ui.theme.CyberPurpleNeon
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

class SiegeRenderer {

    fun renderSiegeGrid(
        drawScope: DrawScope,
        buildings: List<MazeBuilding>,
        troops: List<ActiveTroop>,
        selectedBuildingType: DefenseType? = null,
        selectedTile: Pair<Int, Int>? = null,
        isDefenseEditorMode: Boolean = false,
        animTicks: Long = 0L
    ) {
        val width = drawScope.size.width
        val height = drawScope.size.height

        val gridSize = 9 // 0..8
        val tileSize = (minOf(width, height) * 0.94f) / gridSize
        val originX = (width - (gridSize * tileSize)) / 2f
        val originY = (height - (gridSize * tileSize)) / 2f

        // Helper: Convert Grid coordinates to Canvas Screen Offset
        fun toScreen(gx: Float, gy: Float): Offset {
            return Offset(originX + (gx * tileSize), originY + (gy * tileSize))
        }

        // 1. Draw Cyber Circuit Board Background
        drawScope.drawRect(
            color = Color(0xFF030E0A),
            topLeft = Offset(originX - 6f, originY - 6f),
            size = Size((gridSize * tileSize) + 12f, (gridSize * tileSize) + 12f)
        )
        drawScope.drawRect(
            color = CyberMintPrimary.copy(alpha = 0.35f),
            topLeft = Offset(originX - 6f, originY - 6f),
            size = Size((gridSize * tileSize) + 12f, (gridSize * tileSize) + 12f),
            style = Stroke(width = 2f)
        )

        // 2. Draw Grid Lines
        for (i in 0..gridSize) {
            val hStart = toScreen(0f, i.toFloat())
            val hEnd = toScreen(gridSize.toFloat(), i.toFloat())
            drawScope.drawLine(
                color = Color(0xFF0D2D22),
                start = hStart,
                end = hEnd,
                strokeWidth = 1f
            )

            val vStart = toScreen(i.toFloat(), 0f)
            val vEnd = toScreen(i.toFloat(), gridSize.toFloat())
            drawScope.drawLine(
                color = Color(0xFF0D2D22),
                start = vStart,
                end = vEnd,
                strokeWidth = 1f
            )
        }

        // 3. Highlight Selected Tile in Editor Mode
        if (selectedTile != null) {
            val (sx, sy) = selectedTile
            val tileTopLeft = toScreen(sx.toFloat(), sy.toFloat())
            drawScope.drawRect(
                color = CyberCyanAccent.copy(alpha = 0.25f),
                topLeft = tileTopLeft,
                size = Size(tileSize, tileSize)
            )
            drawScope.drawRect(
                color = CyberCyanAccent,
                topLeft = tileTopLeft,
                size = Size(tileSize, tileSize),
                style = Stroke(width = 2.5f)
            )
        }

        // 4. Draw Buildings (Walls, Core Server, Turrets, Mortars)
        for (b in buildings) {
            if (b.isDestroyed && !isDefenseEditorMode) continue

            val center = toScreen(b.gridX + 0.5f, b.gridY + 0.5f)
            val halfTile = tileSize * 0.42f

            when (b.type) {
                DefenseType.CORE_SERVER -> {
                    // Quantum Core Server: Large pulsating cyber citadel
                    val pulse = (sin(animTicks * 0.08f) * 4f)
                    drawScope.drawCircle(
                        brush = Brush.radialGradient(
                            listOf(CyberCyanAccent.copy(alpha = 0.8f), Color(0xFF003D2E))
                        ),
                        radius = halfTile + pulse,
                        center = center
                    )
                    drawScope.drawCircle(
                        color = CyberMintLight,
                        radius = halfTile * 0.6f,
                        center = center
                    )
                    drawScope.drawCircle(
                        color = Color(0xFF00FFE0),
                        radius = halfTile + pulse,
                        center = center,
                        style = Stroke(width = 2f)
                    )
                }

                DefenseType.NEON_WALL -> {
                    // Neon Barrier: High tech block with glowing border
                    val wallRect = Size(tileSize * 0.88f, tileSize * 0.88f)
                    val wallPos = Offset(center.x - wallRect.width / 2f, center.y - wallRect.height / 2f)

                    drawScope.drawRect(
                        color = if (b.isDestroyed) Color(0xFF1E1E1E) else Color(0xFF092E21),
                        topLeft = wallPos,
                        size = wallRect
                    )
                    drawScope.drawRect(
                        color = if (b.isDestroyed) Color.Gray else CyberMintPrimary,
                        topLeft = wallPos,
                        size = wallRect,
                        style = Stroke(width = 2.5f)
                    )
                }

                DefenseType.LASER_TURRET -> {
                    // Pulse Laser Turret: Circular base with targeting barrel
                    drawScope.drawCircle(
                        color = Color(0xFF123428),
                        radius = halfTile * 0.85f,
                        center = center
                    )
                    drawScope.drawCircle(
                        color = if (b.isStunned) CyberAmberWarning else CyberLaserRed,
                        radius = halfTile * 0.5f,
                        center = center
                    )
                    drawScope.drawCircle(
                        color = CyberMintPrimary,
                        radius = halfTile * 0.85f,
                        center = center,
                        style = Stroke(width = 2f)
                    )

                    // Laser Sight beam if targeting
                    val barrelAngle = (animTicks * 0.05f)
                    val barrelTip = Offset(
                        center.x + (cos(barrelAngle) * halfTile * 1.1f),
                        center.y + (sin(barrelAngle) * halfTile * 1.1f)
                    )
                    drawScope.drawLine(
                        color = if (b.isStunned) CyberAmberWarning else CyberLaserRed,
                        start = center,
                        end = barrelTip,
                        strokeWidth = 3.5f
                    )
                }

                DefenseType.TESLA_PYLON -> {
                    // Tesla Coil
                    drawScope.drawCircle(
                        color = Color(0xFF0C241B),
                        radius = halfTile * 0.8f,
                        center = center
                    )
                    drawScope.drawCircle(
                        color = CyberCyanAccent,
                        radius = halfTile * 0.45f,
                        center = center
                    )
                    drawScope.drawCircle(
                        color = CyberCyanAccent,
                        radius = halfTile * (0.8f + (sin(animTicks * 0.15f) * 0.15f)),
                        center = center,
                        style = Stroke(width = 1.5f)
                    )
                }

                DefenseType.PLASMA_MORTAR -> {
                    // Plasma Mortar
                    drawScope.drawCircle(
                        color = Color(0xFF181F26),
                        radius = halfTile * 0.85f,
                        center = center
                    )
                    drawScope.drawCircle(
                        color = CyberPurpleNeon,
                        radius = halfTile * 0.55f,
                        center = center
                    )
                    drawScope.drawCircle(
                        color = CyberPurpleNeon,
                        radius = halfTile * 0.85f,
                        center = center,
                        style = Stroke(width = 2f)
                    )
                }

                DefenseType.GLITCH_MINE -> {
                    // Hidden Trap (revealed in editor)
                    drawScope.drawCircle(
                        color = CyberAmberWarning.copy(alpha = 0.5f),
                        radius = halfTile * 0.5f,
                        center = center
                    )
                    drawScope.drawLine(
                        color = CyberAmberWarning,
                        start = Offset(center.x - 6f, center.y),
                        end = Offset(center.x + 6f, center.y),
                        strokeWidth = 2f
                    )
                    drawScope.drawLine(
                        color = CyberAmberWarning,
                        start = Offset(center.x, center.y - 6f),
                        end = Offset(center.x, center.y + 6f),
                        strokeWidth = 2f
                    )
                }
            }

            // Health bar above building
            if (b.currentHp < b.maxHp && !b.isDestroyed) {
                val hpPct = (b.currentHp / b.maxHp).coerceIn(0f, 1f)
                val barW = tileSize * 0.8f
                val barH = 4f
                val barLeft = center.x - barW / 2f
                val barTop = center.y - halfTile - 8f

                drawScope.drawRect(
                    color = Color.Black,
                    topLeft = Offset(barLeft, barTop),
                    size = Size(barW, barH)
                )
                drawScope.drawRect(
                    color = if (hpPct > 0.4f) CyberMintPrimary else CyberLaserRed,
                    topLeft = Offset(barLeft, barTop),
                    size = Size(barW * hpPct, barH)
                )
            }
        }

        // 5. Draw Active Troops (Units deployed in raid)
        for (t in troops) {
            if (t.isDead) continue

            val pos = toScreen(t.posX, t.posY)
            val radius = tileSize * 0.35f

            when (t.type) {
                TroopType.BYTE_BRAWLER -> {
                    // Heavy Tank: Cyan/Green large shield unit
                    drawScope.drawCircle(
                        color = Color(0xFF004D36),
                        radius = radius * 1.2f,
                        center = pos
                    )
                    drawScope.drawCircle(
                        color = CyberMintPrimary,
                        radius = radius * 0.9f,
                        center = pos
                    )
                }

                TroopType.GLITCH_SPRINTER -> {
                    // Rapid runner: Amber lightning spark
                    drawScope.drawCircle(
                        color = CyberAmberWarning,
                        radius = radius * 0.75f,
                        center = pos
                    )
                }

                TroopType.EMP_HACKER -> {
                    // Ranged Hacker: Cyan beam unit
                    drawScope.drawCircle(
                        color = Color(0xFF003A47),
                        radius = radius,
                        center = pos
                    )
                    drawScope.drawCircle(
                        color = CyberCyanAccent,
                        radius = radius * 0.6f,
                        center = pos
                    )
                }

                TroopType.PHANTOM_DRONE -> {
                    // Aerial Drone: Floating purple hover unit
                    drawScope.drawCircle(
                        color = Color(0xFF2E083D),
                        radius = radius * 1.1f,
                        center = pos
                    )
                    drawScope.drawCircle(
                        color = CyberPurpleNeon,
                        radius = radius * 0.7f,
                        center = pos
                    )
                }
            }

            // Health bar above troop
            val hpPct = (t.currentHp / t.maxHp).coerceIn(0f, 1f)
            val barW = tileSize * 0.6f
            val barH = 3.5f
            val barLeft = pos.x - barW / 2f
            val barTop = pos.y - radius - 7f

            drawScope.drawRect(
                color = Color.Black,
                topLeft = Offset(barLeft, barTop),
                size = Size(barW, barH)
            )
            drawScope.drawRect(
                color = if (hpPct > 0.35f) CyberMintLight else CyberLaserRed,
                topLeft = Offset(barLeft, barTop),
                size = Size(barW * hpPct, barH)
            )
        }

        // 6. Draw Attack Laser Beams between firing defenses and troops
        for (b in buildings) {
            if (b.isDestroyed || b.isStunned || b.type.isWall || b.type.isTrap) continue
            val bCenter = toScreen(b.gridX + 0.5f, b.gridY + 0.5f)

            // Find closest troop in range
            val target = troops.filter { !it.isDead }.minByOrNull {
                hypot(it.posX - b.gridX, it.posY - b.gridY)
            }
            if (target != null) {
                val dist = hypot(target.posX - b.gridX, target.posY - b.gridY)
                if (dist <= b.type.rangeTiles && b.attackCooldown < 0.3f) {
                    val tCenter = toScreen(target.posX, target.posY)
                    val beamColor = when (b.type) {
                        DefenseType.LASER_TURRET -> CyberLaserRed
                        DefenseType.TESLA_PYLON -> CyberCyanAccent
                        DefenseType.PLASMA_MORTAR -> CyberPurpleNeon
                        else -> CyberMintPrimary
                    }
                    drawScope.drawLine(
                        color = beamColor,
                        start = bCenter,
                        end = tCenter,
                        strokeWidth = 2.5f
                    )
                }
            }
        }
    }
}
