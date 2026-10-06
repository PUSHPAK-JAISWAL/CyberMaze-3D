package com.example.game.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.data.model.EnemyData
import com.example.data.model.EnemyType
import com.example.data.model.LevelData
import com.example.data.model.LevelTile
import com.example.data.model.Point3D
import com.example.data.model.TileType
import com.example.ui.theme.CyberAmberWarning
import com.example.ui.theme.CyberCyanAccent
import com.example.ui.theme.CyberLaserRed
import com.example.ui.theme.CyberMintDark
import com.example.ui.theme.CyberMintLight
import com.example.ui.theme.CyberMintPrimary
import com.example.ui.theme.CyberPurpleNeon
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class Camera3D(
    val yawDegrees: Float = 45f,
    val pitchRatio: Float = 0.55f, // Controls vertical compression of isometric plane
    val zoom: Float = 1.0f,
    val panX: Float = 0f,
    val panY: Float = 0f
)

sealed class Renderable3D(val depthKey: Float) {
    class TileItem(val tile: LevelTile, depth: Float) : Renderable3D(depth)
    class PlayerItem(val pos: Point3D, val animProgress: Float, depth: Float) : Renderable3D(depth)
    class EnemyItem(val enemy: EnemyData, depth: Float) : Renderable3D(depth)
}

class Game3DRenderer {

    fun renderScene(
        drawScope: DrawScope,
        level: LevelData,
        playerPos: Point3D,
        camera: Camera3D,
        animationTicks: Long,
        pulseRadarActive: Boolean
    ) {
        val width = drawScope.size.width
        val height = drawScope.size.height

        val centerX = width / 2f + camera.panX
        val centerY = height / 2f + camera.panY

        // Dynamic tile sizing based on screen dimensions and camera zoom
        val baseTileSize = (minOf(width, height) / (level.gridWidth + 3)) * camera.zoom
        val tileW = baseTileSize
        val tileH = baseTileSize * camera.pitchRatio
        val tileDepthZ = baseTileSize * 0.45f // Elevation height extrusion per Z level

        val yawRad = (camera.yawDegrees * PI / 180.0).toFloat()
        val cosYaw = cos(yawRad)
        val sinYaw = sin(yawRad)

        val centerGridX = (level.gridWidth - 1) / 2f
        val centerGridY = (level.gridHeight - 1) / 2f

        fun projectPoint(x: Float, y: Float, z: Float): Offset {
            val relX = x - centerGridX
            val relY = y - centerGridY

            val rotX = relX * cosYaw - relY * sinYaw
            val rotY = relX * sinYaw + relY * cosYaw

            // Isometric 2D screen projection
            val sx = centerX + (rotX - rotY) * (tileW * 0.72f)
            val sy = centerY + (rotX + rotY) * (tileH * 0.72f) - (z * tileDepthZ)
            return Offset(sx, sy)
        }

        fun computeDepthKey(x: Float, y: Float, z: Float): Float {
            val relX = x - centerGridX
            val relY = y - centerGridY
            val rotY = relX * sinYaw + relY * cosYaw
            val rotX = relX * cosYaw - relY * sinYaw
            // Primary sort by rotated Y + rotX, secondary by elevation Z
            return (rotX + rotY) * 100f + z * 5f
        }

        // Draw ambient cyberpunk grid horizon
        drawCyberGridBackground(drawScope, width, height, animationTicks)

        // Build list of all 3D items to sort by depth (Painter's algorithm)
        val renderList = mutableListOf<Renderable3D>()

        level.tiles.forEach { tile ->
            renderList.add(Renderable3D.TileItem(tile, computeDepthKey(tile.x.toFloat(), tile.y.toFloat(), tile.z.toFloat())))
        }

        renderList.add(
            Renderable3D.PlayerItem(
                playerPos,
                (animationTicks % 60) / 60f,
                computeDepthKey(playerPos.x.toFloat(), playerPos.y.toFloat(), playerPos.z.toFloat()) + 1f
            )
        )

        level.enemies.forEach { enemy ->
            renderList.add(
                Renderable3D.EnemyItem(
                    enemy,
                    computeDepthKey(enemy.x.toFloat(), enemy.y.toFloat(), enemy.z.toFloat()) + 0.8f
                )
            )
        }

        // Sort ascending by depthKey
        renderList.sortBy { it.depthKey }

        // Render sorted 3D items
        renderList.forEach { item ->
            when (item) {
                is Renderable3D.TileItem -> {
                    drawTile(
                        drawScope = drawScope,
                        tile = item.tile,
                        project = ::projectPoint,
                        tileW = tileW,
                        tileH = tileH,
                        tileDepthZ = tileDepthZ,
                        animTicks = animationTicks,
                        radarActive = pulseRadarActive
                    )
                }
                is Renderable3D.PlayerItem -> {
                    drawPlayer(
                        drawScope = drawScope,
                        playerPos = item.pos,
                        project = ::projectPoint,
                        baseSize = baseTileSize,
                        animTicks = animationTicks
                    )
                }
                is Renderable3D.EnemyItem -> {
                    drawEnemy(
                        drawScope = drawScope,
                        enemy = item.enemy,
                        project = ::projectPoint,
                        baseSize = baseTileSize,
                        animTicks = animationTicks,
                        radarActive = pulseRadarActive
                    )
                }
            }
        }
    }

    private fun drawCyberGridBackground(drawScope: DrawScope, width: Float, height: Float, animTicks: Long) {
        // Deep cyber obsidian backdrop
        drawScope.drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF040D0A),
                    Color(0xFF071B14),
                    Color(0xFF0B291E)
                )
            )
        )

        // Subtle ambient scanning laser lines
        val pulseY = ((animTicks * 2.5f) % height)
        drawScope.drawLine(
            color = Color(0x1A00E599),
            start = Offset(0f, pulseY),
            end = Offset(width, pulseY),
            strokeWidth = 2f
        )
    }

    private fun drawTile(
        drawScope: DrawScope,
        tile: LevelTile,
        project: (Float, Float, Float) -> Offset,
        tileW: Float,
        tileH: Float,
        tileDepthZ: Float,
        animTicks: Long,
        radarActive: Boolean
    ) {
        val x = tile.x.toFloat()
        val y = tile.y.toFloat()
        val z = tile.z.toFloat()

        // 4 corners of isometric diamond on top face
        val pNorth = project(x, y - 0.48f, z)
        val pEast = project(x + 0.48f, y, z)
        val pSouth = project(x, y + 0.48f, z)
        val pWest = project(x - 0.48f, y, z)

        // Base extrusion drop (to give real 3D solid slab height)
        val slabHeight = when {
            tile.type == TileType.WALL -> tileDepthZ * 2.2f
            tile.type == TileType.ELEVATION_RAMP || z > 0 -> tileDepthZ * (z + 0.6f)
            tile.type == TileType.DEPRESSION_PIT -> tileDepthZ * 0.2f
            else -> tileDepthZ * 0.45f
        }

        val pSouthBase = Offset(pSouth.x, pSouth.y + slabHeight)
        val pEastBase = Offset(pEast.x, pEast.y + slabHeight)
        val pWestBase = Offset(pWest.x, pWest.y + slabHeight)

        // Choose color palette based on tile type
        val (topColor, leftSideColor, rightSideColor, outlineColor) = when (tile.type) {
            TileType.WALL -> Quad(
                Color(0xFF0A2219),
                Color(0xFF061610),
                Color(0xFF030D09),
                CyberMintDark
            )
            TileType.ELEVATION_RAMP -> Quad(
                Color(0xFF104A36),
                Color(0xFF0C3829),
                Color(0xFF08261C),
                CyberMintPrimary
            )
            TileType.DEPRESSION_PIT -> Quad(
                Color(0xFF280B12),
                Color(0xFF1C070D),
                Color(0xFF140509),
                CyberLaserRed
            )
            TileType.EXIT_PORTAL -> Quad(
                Color(0xFF0F3B40),
                Color(0xFF092529),
                Color(0xFF06181A),
                CyberCyanAccent
            )
            TileType.TERMINAL -> Quad(
                Color(0xFF263A1D),
                Color(0xFF1C2B15),
                Color(0xFF141F10),
                CyberAmberWarning
            )
            TileType.POWER_CORE -> Quad(
                Color(0xFF0C3023),
                Color(0xFF082319),
                Color(0xFF051711),
                CyberMintLight
            )
            TileType.FLOOR -> Quad(
                Color(0xFF091E16),
                Color(0xFF061510),
                Color(0xFF040E0A),
                Color(0xFF1A4737)
            )
            else -> Quad(
                Color(0xFF091E16),
                Color(0xFF061510),
                Color(0xFF040E0A),
                Color(0xFF1A4737)
            )
        }

        // 1. Draw 3D South-West Side (Left side extrusion)
        val leftPath = Path().apply {
            moveTo(pWest.x, pWest.y)
            lineTo(pSouth.x, pSouth.y)
            lineTo(pSouthBase.x, pSouthBase.y)
            lineTo(pWestBase.x, pWestBase.y)
            close()
        }
        drawScope.drawPath(leftPath, color = leftSideColor, style = Fill)
        drawScope.drawPath(leftPath, color = Color(0x3300E599), style = Stroke(width = 1f))

        // 2. Draw 3D South-East Side (Right side extrusion)
        val rightPath = Path().apply {
            moveTo(pSouth.x, pSouth.y)
            lineTo(pEast.x, pEast.y)
            lineTo(pEastBase.x, pEastBase.y)
            lineTo(pSouthBase.x, pSouthBase.y)
            close()
        }
        drawScope.drawPath(rightPath, color = rightSideColor, style = Fill)
        drawScope.drawPath(rightPath, color = Color(0x2200E599), style = Stroke(width = 1f))

        // 3. Draw 3D Top Diamond Face
        val topPath = Path().apply {
            moveTo(pNorth.x, pNorth.y)
            lineTo(pEast.x, pEast.y)
            lineTo(pSouth.x, pSouth.y)
            lineTo(pWest.x, pWest.y)
            close()
        }
        drawScope.drawPath(topPath, color = topColor, style = Fill)
        drawScope.drawPath(
            topPath,
            color = if (radarActive) CyberMintLight else outlineColor,
            style = Stroke(width = if (radarActive) 2.2f else 1.2f)
        )

        // 4. Draw Tile Decorators (Pillars, Cores, Terminal Holograms, Portal Vortex)
        val centerTop = project(x, y, z)

        when (tile.type) {
            TileType.WALL -> {
                // Cyber circuit pillar
                drawScope.drawLine(
                    color = CyberMintLight,
                    start = centerTop,
                    end = Offset(centerTop.x, centerTop.y - tileDepthZ * 0.8f),
                    strokeWidth = 3f
                )
            }
            TileType.POWER_CORE -> {
                if (!tile.isCollected) {
                    val floatOffset = sin((animTicks + x * 10) * 0.1f) * 6f
                    val coreCenter = Offset(centerTop.x, centerTop.y - tileDepthZ * 0.7f + floatOffset)

                    // Outer neon pulse ring
                    drawScope.drawCircle(
                        color = Color(0x4D00E599),
                        radius = tileW * 0.22f,
                        center = coreCenter
                    )
                    // Inner glowing energy core
                    drawScope.drawCircle(
                        color = CyberMintLight,
                        radius = tileW * 0.12f,
                        center = coreCenter
                    )
                    drawScope.drawCircle(
                        color = Color.White,
                        radius = tileW * 0.06f,
                        center = coreCenter
                    )
                }
            }
            TileType.TERMINAL -> {
                val termPos = Offset(centerTop.x, centerTop.y - tileDepthZ * 0.6f)
                // Holographic terminal console
                drawScope.drawCircle(
                    color = if (tile.isDeactivated) CyberMintDark else CyberAmberWarning,
                    radius = tileW * 0.16f,
                    center = termPos,
                    style = Stroke(width = 2.5f)
                )
                drawScope.drawLine(
                    color = CyberAmberWarning,
                    start = Offset(termPos.x - 6f, termPos.y),
                    end = Offset(termPos.x + 6f, termPos.y),
                    strokeWidth = 2f
                )
            }
            TileType.EXIT_PORTAL -> {
                val spinAngle = (animTicks * 4f) % 360f
                val rad = (spinAngle * PI / 180f).toFloat()
                val portalCenter = Offset(centerTop.x, centerTop.y - tileDepthZ * 0.8f)

                // Concentric swirling cyber vortex
                drawScope.drawCircle(
                    color = CyberCyanAccent,
                    radius = tileW * 0.28f,
                    center = portalCenter,
                    style = Stroke(width = 2.5f)
                )
                drawScope.drawCircle(
                    color = CyberMintLight,
                    radius = tileW * 0.18f,
                    center = portalCenter,
                    style = Stroke(width = 1.8f)
                )
                // Cross vortex flare
                val dx = cos(rad) * tileW * 0.25f
                val dy = sin(rad) * tileW * 0.25f
                drawScope.drawLine(
                    color = Color.White,
                    start = Offset(portalCenter.x - dx, portalCenter.y - dy),
                    end = Offset(portalCenter.x + dx, portalCenter.y + dy),
                    strokeWidth = 2f
                )
            }
            TileType.DEPRESSION_PIT -> {
                // Hazard warning lines inside sunken pit
                drawScope.drawLine(
                    color = CyberLaserRed,
                    start = Offset(pWest.x + 6f, pWest.y),
                    end = Offset(pEast.x - 6f, pEast.y),
                    strokeWidth = 1.5f
                )
            }
            TileType.ELEVATION_RAMP -> {
                // Tier elevation level badges
                if (z > 0) {
                    drawScope.drawLine(
                        color = Color(0x6600E599),
                        start = pWest,
                        end = pEast,
                        strokeWidth = 1f
                    )
                }
            }
            else -> Unit
        }
    }

    private fun drawPlayer(
        drawScope: DrawScope,
        playerPos: Point3D,
        project: (Float, Float, Float) -> Offset,
        baseSize: Float,
        animTicks: Long
    ) {
        val center = project(playerPos.x.toFloat(), playerPos.y.toFloat(), playerPos.z.toFloat())
        val bobbing = sin(animTicks * 0.15f) * 3f
        val playerCenter = Offset(center.x, center.y - baseSize * 0.55f + bobbing)

        // 1. Hover shadow on tile surface
        drawScope.drawOval(
            color = Color(0x6600E599),
            topLeft = Offset(center.x - baseSize * 0.22f, center.y - baseSize * 0.11f),
            size = androidx.compose.ui.geometry.Size(baseSize * 0.44f, baseSize * 0.22f)
        )

        // 2. Operative Outer Cyber Shield Ring
        drawScope.drawCircle(
            color = Color(0x3300E599),
            radius = baseSize * 0.28f,
            center = playerCenter
        )

        // 3. Operative Body (Sleek Cyber Capsule)
        drawScope.drawCircle(
            color = CyberMintDark,
            radius = baseSize * 0.18f,
            center = playerCenter
        )

        // 4. Operative Neon Visor (Glowing Mint)
        drawScope.drawOval(
            color = CyberMintLight,
            topLeft = Offset(playerCenter.x - baseSize * 0.12f, playerCenter.y - baseSize * 0.07f),
            size = androidx.compose.ui.geometry.Size(baseSize * 0.24f, baseSize * 0.1f)
        )

        // 5. Visor Glint
        drawScope.drawCircle(
            color = Color.White,
            radius = baseSize * 0.04f,
            center = Offset(playerCenter.x + baseSize * 0.03f, playerCenter.y - baseSize * 0.02f)
        )
    }

    private fun drawEnemy(
        drawScope: DrawScope,
        enemy: EnemyData,
        project: (Float, Float, Float) -> Offset,
        baseSize: Float,
        animTicks: Long,
        radarActive: Boolean
    ) {
        val center = project(enemy.x.toFloat(), enemy.y.toFloat(), enemy.z.toFloat())
        val bobbing = sin((animTicks + enemy.x * 20) * 0.12f) * 4f
        val droneCenter = Offset(center.x, center.y - baseSize * 0.65f + bobbing)

        val (enemyColor, auraColor) = when (enemy.type) {
            EnemyType.HUNTER -> Pair(CyberLaserRed, Color(0x4DFF3366))
            EnemyType.SENTINEL -> Pair(CyberAmberWarning, Color(0x4DFFB703))
            EnemyType.PHANTOM -> Pair(CyberPurpleNeon, Color(0x4DBD00FF))
            EnemyType.STALKER -> Pair(Color(0xFFFF6B35), Color(0x4DFF6B35))
        }

        // 1. Red threat scanner cone projected onto floor
        val scannerTarget = Offset(center.x + sin(animTicks * 0.08f) * (baseSize * 0.4f), center.y + baseSize * 0.2f)
        drawScope.drawLine(
            color = auraColor,
            start = droneCenter,
            end = scannerTarget,
            strokeWidth = 2f
        )
        drawScope.drawCircle(
            color = auraColor,
            radius = baseSize * 0.18f,
            center = scannerTarget
        )

        // 2. Drone Pulsing Aura
        drawScope.drawCircle(
            color = auraColor,
            radius = baseSize * 0.3f,
            center = droneCenter
        )

        // 3. Drone Mechanical Chassis (Diamond / Octagon polygon)
        val dronePath = Path().apply {
            moveTo(droneCenter.x, droneCenter.y - baseSize * 0.16f)
            lineTo(droneCenter.x + baseSize * 0.16f, droneCenter.y)
            lineTo(droneCenter.x, droneCenter.y + baseSize * 0.16f)
            lineTo(droneCenter.x - baseSize * 0.16f, droneCenter.y)
            close()
        }
        drawScope.drawPath(dronePath, color = Color(0xFF1E0A10), style = Fill)
        drawScope.drawPath(dronePath, color = enemyColor, style = Stroke(width = 2.2f))

        // 4. Red Threat Eye Core
        drawScope.drawCircle(
            color = enemyColor,
            radius = baseSize * 0.07f,
            center = droneCenter
        )
        drawScope.drawCircle(
            color = Color.White,
            radius = baseSize * 0.03f,
            center = droneCenter
        )
    }

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
