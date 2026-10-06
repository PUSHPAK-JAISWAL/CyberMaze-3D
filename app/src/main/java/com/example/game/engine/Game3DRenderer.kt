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
    val pitchRatio: Float = 0.55f, // 0.55 is isometric 45°, 0.85 is top-down tactical
    val zoom: Float = 1.0f,
    val panX: Float = 0f,
    val panY: Float = 0f
)

sealed class Renderable3D(val depthKey: Float) {
    class TileItem(val tile: LevelTile, depth: Float) : Renderable3D(depth)
    class PlayerItem(val x: Float, val y: Float, val z: Float, val isCloaked: Boolean, val animProgress: Float, depth: Float) : Renderable3D(depth)
    class EnemyItem(val enemy: EnemyData, val renderX: Float, val renderY: Float, val renderZ: Float, depth: Float) : Renderable3D(depth)
}

class Game3DRenderer {

    private var animPlayerX = -1f
    private var animPlayerY = -1f
    private var animPlayerZ = -1f

    private val animEnemyPos = mutableMapOf<String, Triple<Float, Float, Float>>()

    fun renderScene(
        drawScope: DrawScope,
        level: LevelData,
        playerPos: Point3D,
        camera: Camera3D,
        animationTicks: Long,
        pulseRadarActive: Boolean,
        isPlayerCloaked: Boolean = false,
        shockwaveRadius: Float = 0f
    ) {
        val width = drawScope.size.width
        val height = drawScope.size.height

        val centerX = width / 2f + camera.panX
        val centerY = height / 2f + camera.panY

        val baseTileSize = (minOf(width, height) / (level.gridWidth + 3)) * camera.zoom
        val tileW = baseTileSize
        val tileH = baseTileSize * camera.pitchRatio
        val tileDepthZ = baseTileSize * 0.45f

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

            val sx = centerX + (rotX - rotY) * (tileW * 0.72f)
            val sy = centerY + (rotX + rotY) * (tileH * 0.72f) - (z * tileDepthZ)
            return Offset(sx, sy)
        }

        fun computeDepthKey(x: Float, y: Float, z: Float): Float {
            val relX = x - centerGridX
            val relY = y - centerGridY
            val rotY = relX * sinYaw + relY * cosYaw
            val rotX = relX * cosYaw - relY * sinYaw
            return (rotX + rotY) * 100f + z * 5f
        }

        // Smooth position interpolation (lerp)
        if (animPlayerX < 0f) {
            animPlayerX = playerPos.x.toFloat()
            animPlayerY = playerPos.y.toFloat()
            animPlayerZ = playerPos.z.toFloat()
        } else {
            animPlayerX += (playerPos.x.toFloat() - animPlayerX) * 0.35f
            animPlayerY += (playerPos.y.toFloat() - animPlayerY) * 0.35f
            animPlayerZ += (playerPos.z.toFloat() - animPlayerZ) * 0.35f
        }

        // Draw bright, uplifting cyber turquoise-mint atmosphere
        drawCyberGridBackground(drawScope, width, height, animationTicks)

        // Draw Enemy Tactical Vision Cones onto the floor (before objects)
        level.enemies.forEach { enemy ->
            if (!enemy.isStunned) {
                drawEnemyVisionCone(
                    drawScope = drawScope,
                    enemy = enemy,
                    project = ::projectPoint,
                    tileW = tileW,
                    tileH = tileH,
                    animTicks = animationTicks
                )
            }
        }

        // Build list of all 3D items to sort by depth (Painter's algorithm)
        val renderList = mutableListOf<Renderable3D>()

        level.tiles.forEach { tile ->
            renderList.add(Renderable3D.TileItem(tile, computeDepthKey(tile.x.toFloat(), tile.y.toFloat(), tile.z.toFloat())))
        }

        renderList.add(
            Renderable3D.PlayerItem(
                animPlayerX,
                animPlayerY,
                animPlayerZ,
                isPlayerCloaked,
                (animationTicks % 60) / 60f,
                computeDepthKey(animPlayerX, animPlayerY, animPlayerZ) + 1f
            )
        )

        level.enemies.forEach { enemy ->
            val prev = animEnemyPos[enemy.name] ?: Triple(enemy.x.toFloat(), enemy.y.toFloat(), enemy.z.toFloat())
            val nx = prev.first + (enemy.x.toFloat() - prev.first) * 0.35f
            val ny = prev.second + (enemy.y.toFloat() - prev.second) * 0.35f
            val nz = prev.third + (enemy.z.toFloat() - prev.third) * 0.35f
            animEnemyPos[enemy.name] = Triple(nx, ny, nz)

            renderList.add(
                Renderable3D.EnemyItem(
                    enemy,
                    nx,
                    ny,
                    nz,
                    computeDepthKey(nx, ny, nz) + 0.8f
                )
            )
        }

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
                        renderX = item.x,
                        renderY = item.y,
                        renderZ = item.z,
                        isCloaked = item.isCloaked,
                        project = ::projectPoint,
                        baseSize = baseTileSize,
                        animTicks = animationTicks
                    )
                }
                is Renderable3D.EnemyItem -> {
                    drawEnemy(
                        drawScope = drawScope,
                        enemy = item.enemy,
                        renderX = item.renderX,
                        renderY = item.renderY,
                        renderZ = item.renderZ,
                        project = ::projectPoint,
                        baseSize = baseTileSize,
                        animTicks = animationTicks
                    )
                }
            }
        }

        // Draw EMP expanding shockwave ring if triggered
        if (shockwaveRadius > 0f) {
            val playerScreen = projectPoint(playerPos.x.toFloat(), playerPos.y.toFloat(), playerPos.z.toFloat())
            drawScope.drawCircle(
                color = Color(0x6600E5FF),
                radius = shockwaveRadius * baseTileSize * 1.5f,
                center = playerScreen,
                style = Stroke(width = 4f)
            )
        }
    }

    private fun drawCyberGridBackground(drawScope: DrawScope, width: Float, height: Float, animTicks: Long) {
        // Uplifting luminous cyber turquoise-mint gradient
        drawScope.drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0F3A2E),
                    Color(0xFF175745),
                    Color(0xFF22755E)
                )
            )
        )

        // Luminous scanning laser lines
        val pulseY = ((animTicks * 2f) % height)
        drawScope.drawLine(
            color = Color(0x3338EFAB),
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

        val pNorth = project(x, y - 0.48f, z)
        val pEast = project(x + 0.48f, y, z)
        val pSouth = project(x, y + 0.48f, z)
        val pWest = project(x - 0.48f, y, z)

        val slabHeight = when {
            tile.type == TileType.WALL -> tileDepthZ * 2.2f
            tile.type == TileType.ELEVATION_RAMP || z > 0 -> tileDepthZ * (z + 0.6f)
            tile.type == TileType.DEPRESSION_PIT -> tileDepthZ * 0.2f
            else -> tileDepthZ * 0.45f
        }

        val pSouthBase = Offset(pSouth.x, pSouth.y + slabHeight)
        val pEastBase = Offset(pEast.x, pEast.y + slabHeight)
        val pWestBase = Offset(pWest.x, pWest.y + slabHeight)

        val (topColor, leftSideColor, rightSideColor, outlineColor) = when (tile.type) {
            TileType.WALL -> Quad(Color(0xFF1B5945), Color(0xFF134535), Color(0xFF0E3327), CyberMintLight)
            TileType.ELEVATION_RAMP -> Quad(Color(0xFF2BA17F), Color(0xFF218266), Color(0xFF18664F), Color(0xFF80FFD4))
            TileType.DEPRESSION_PIT -> Quad(Color(0xFF4D2027), Color(0xFF38161B), Color(0xFF260D11), CyberLaserRed)
            TileType.EXIT_PORTAL -> Quad(Color(0xFF207582), Color(0xFF175761), Color(0xFF103E45), CyberCyanAccent)
            TileType.TERMINAL -> Quad(Color(0xFF456333), Color(0xFF354D26), Color(0xFF25381A), CyberAmberWarning)
            TileType.PRESSURE_SWITCH -> Quad(
                if (tile.isActivated) Color(0xFF1C6348) else Color(0xFF1A5043),
                Color(0xFF15483B),
                Color(0xFF0E382C),
                if (tile.isActivated) CyberMintPrimary else CyberCyanAccent
            )
            TileType.LASER_GATE -> Quad(
                if (tile.isDeactivated) Color(0xFF184D3D) else Color(0xFF451A22),
                Color(0xFF134033),
                Color(0xFF0D3328),
                if (tile.isDeactivated) CyberMintDark else CyberLaserRed
            )
            TileType.POWER_CORE -> Quad(Color(0xFF258567), Color(0xFF1C6B52), Color(0xFF14523E), Color(0xFF70FFCC))
            TileType.FLOOR -> Quad(Color(0xFF1E6952), Color(0xFF165240), Color(0xFF103E30), Color(0xFF38EFAB))
        }

        // Left Side Extrusion
        val leftPath = Path().apply {
            moveTo(pWest.x, pWest.y)
            lineTo(pSouth.x, pSouth.y)
            lineTo(pSouthBase.x, pSouthBase.y)
            lineTo(pWestBase.x, pWestBase.y)
            close()
        }
        drawScope.drawPath(leftPath, color = leftSideColor, style = Fill)
        drawScope.drawPath(leftPath, color = Color(0x3300E599), style = Stroke(width = 1f))

        // Right Side Extrusion
        val rightPath = Path().apply {
            moveTo(pSouth.x, pSouth.y)
            lineTo(pEast.x, pEast.y)
            lineTo(pEastBase.x, pEastBase.y)
            lineTo(pSouthBase.x, pSouthBase.y)
            close()
        }
        drawScope.drawPath(rightPath, color = rightSideColor, style = Fill)
        drawScope.drawPath(rightPath, color = Color(0x2200E599), style = Stroke(width = 1f))

        // Top Diamond Face
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

        // Decorators
        val centerTop = project(x, y, z)

        when (tile.type) {
            TileType.WALL -> {
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

                    drawScope.drawCircle(color = Color(0x5500E599), radius = tileW * 0.22f, center = coreCenter)
                    drawScope.drawCircle(color = CyberMintLight, radius = tileW * 0.12f, center = coreCenter)
                    drawScope.drawCircle(color = Color.White, radius = tileW * 0.06f, center = coreCenter)
                }
            }
            TileType.PRESSURE_SWITCH -> {
                drawScope.drawCircle(
                    color = if (tile.isActivated) CyberMintPrimary else CyberCyanAccent,
                    radius = tileW * 0.18f,
                    center = centerTop,
                    style = Stroke(width = 2.5f)
                )
                if (tile.isActivated) {
                    drawScope.drawCircle(color = CyberMintLight, radius = tileW * 0.1f, center = centerTop)
                }
            }
            TileType.LASER_GATE -> {
                if (!tile.isDeactivated) {
                    // Pulsing Red Laser Fence
                    val post1 = Offset(pWest.x, pWest.y - tileDepthZ * 0.6f)
                    val post2 = Offset(pEast.x, pEast.y - tileDepthZ * 0.6f)
                    drawScope.drawLine(color = CyberLaserRed, start = post1, end = post2, strokeWidth = 3.5f)
                    drawScope.drawLine(color = Color.White, start = post1, end = post2, strokeWidth = 1.2f)
                } else {
                    // Deactivated Green Fence
                    val post1 = Offset(pWest.x, pWest.y - tileDepthZ * 0.4f)
                    val post2 = Offset(pEast.x, pEast.y - tileDepthZ * 0.4f)
                    drawScope.drawLine(color = Color(0x5500C882), start = post1, end = post2, strokeWidth = 1.5f)
                }
            }
            TileType.TERMINAL -> {
                val termPos = Offset(centerTop.x, centerTop.y - tileDepthZ * 0.6f)
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

                drawScope.drawCircle(color = CyberCyanAccent, radius = tileW * 0.28f, center = portalCenter, style = Stroke(width = 2.5f))
                drawScope.drawCircle(color = CyberMintLight, radius = tileW * 0.18f, center = portalCenter, style = Stroke(width = 1.8f))
                val dx = cos(rad) * tileW * 0.25f
                val dy = sin(rad) * tileW * 0.25f
                drawScope.drawLine(color = Color.White, start = Offset(portalCenter.x - dx, portalCenter.y - dy), end = Offset(portalCenter.x + dx, portalCenter.y + dy), strokeWidth = 2f)
            }
            TileType.DEPRESSION_PIT -> {
                drawScope.drawLine(color = CyberLaserRed, start = Offset(pWest.x + 6f, pWest.y), end = Offset(pEast.x - 6f, pEast.y), strokeWidth = 1.5f)
            }
            else -> Unit
        }
    }

    private fun drawEnemyVisionCone(
        drawScope: DrawScope,
        enemy: EnemyData,
        project: (Float, Float, Float) -> Offset,
        tileW: Float,
        tileH: Float,
        animTicks: Long
    ) {
        val origin = project(enemy.x.toFloat(), enemy.y.toFloat(), enemy.z.toFloat())
        val coneColor = if (enemy.isAlerted) Color(0x33FF3366) else Color(0x22FFB703)
        val borderColor = if (enemy.isAlerted) Color(0x66FF3366) else Color(0x44FFB703)

        val target = when (enemy.type) {
            EnemyType.HUNTER -> project(enemy.x.toFloat(), enemy.y.toFloat() + 1.8f, enemy.z.toFloat())
            EnemyType.SENTINEL -> {
                val rot = (animTicks * 2f % 360f) * PI / 180f
                val dx = cos(rot).toFloat() * 2f
                val dy = sin(rot).toFloat() * 2f
                project(enemy.x.toFloat() + dx, enemy.y.toFloat() + dy, enemy.z.toFloat())
            }
            else -> project(enemy.x.toFloat() + 1.2f, enemy.y.toFloat() + 1.2f, enemy.z.toFloat())
        }

        val conePath = Path().apply {
            moveTo(origin.x, origin.y)
            lineTo(target.x - tileW * 0.35f, target.y)
            lineTo(target.x + tileW * 0.35f, target.y)
            close()
        }
        drawScope.drawPath(conePath, color = coneColor, style = Fill)
        drawScope.drawPath(conePath, color = borderColor, style = Stroke(width = 1.2f))
    }

    private fun drawPlayer(
        drawScope: DrawScope,
        renderX: Float,
        renderY: Float,
        renderZ: Float,
        isCloaked: Boolean,
        project: (Float, Float, Float) -> Offset,
        baseSize: Float,
        animTicks: Long
    ) {
        val center = project(renderX, renderY, renderZ)
        val bobbing = sin(animTicks * 0.15f) * 3f
        val playerCenter = Offset(center.x, center.y - baseSize * 0.55f + bobbing)

        val alpha = if (isCloaked) 0.45f else 1.0f

        // 1. Hover shadow on tile surface
        drawScope.drawOval(
            color = Color(0x6600E599).copy(alpha = alpha * 0.4f),
            topLeft = Offset(center.x - baseSize * 0.22f, center.y - baseSize * 0.11f),
            size = androidx.compose.ui.geometry.Size(baseSize * 0.44f, baseSize * 0.22f)
        )

        // 2. Operative Outer Cyber Shield Ring
        drawScope.drawCircle(
            color = (if (isCloaked) CyberCyanAccent else CyberMintPrimary).copy(alpha = alpha * 0.35f),
            radius = baseSize * 0.28f,
            center = playerCenter
        )

        // 3. Operative Body (Sleek Cyber Capsule)
        drawScope.drawCircle(
            color = CyberMintDark.copy(alpha = alpha),
            radius = baseSize * 0.18f,
            center = playerCenter
        )

        // 4. Operative Neon Visor
        drawScope.drawOval(
            color = (if (isCloaked) CyberCyanAccent else CyberMintLight).copy(alpha = alpha),
            topLeft = Offset(playerCenter.x - baseSize * 0.12f, playerCenter.y - baseSize * 0.07f),
            size = androidx.compose.ui.geometry.Size(baseSize * 0.24f, baseSize * 0.1f)
        )

        // 5. Visor Glint
        drawScope.drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = baseSize * 0.04f,
            center = Offset(playerCenter.x + baseSize * 0.03f, playerCenter.y - baseSize * 0.02f)
        )

        // Cloak Shimmer Indicator
        if (isCloaked) {
            drawScope.drawCircle(
                color = CyberCyanAccent,
                radius = baseSize * 0.34f,
                center = playerCenter,
                style = Stroke(width = 1.5f)
            )
        }
    }

    private fun drawEnemy(
        drawScope: DrawScope,
        enemy: EnemyData,
        renderX: Float,
        renderY: Float,
        renderZ: Float,
        project: (Float, Float, Float) -> Offset,
        baseSize: Float,
        animTicks: Long
    ) {
        val center = project(renderX, renderY, renderZ)
        val bobbing = sin((animTicks + enemy.x * 20) * 0.12f) * 4f
        val droneCenter = Offset(center.x, center.y - baseSize * 0.65f + bobbing)

        val isAlert = enemy.isAlerted
        val isStun = enemy.isStunned

        val (enemyColor, auraColor) = when {
            isStun -> Pair(CyberAmberWarning, Color(0x66FFB703))
            isAlert -> Pair(CyberLaserRed, Color(0x66FF3366))
            else -> when (enemy.type) {
                EnemyType.HUNTER -> Pair(Color(0xFFFF5252), Color(0x44FF5252))
                EnemyType.SENTINEL -> Pair(CyberAmberWarning, Color(0x44FFB703))
                EnemyType.PHANTOM -> Pair(CyberPurpleNeon, Color(0x449D4EDD))
                EnemyType.STALKER -> Pair(Color(0xFFFF7A00), Color(0x44FF7A00))
            }
        }

        // 1. Drone Active Vision Spotlight on Floor
        if (!isStun) {
            val scanAngle = sin(animTicks * 0.08f) * (baseSize * 0.35f)
            val scannerTarget = Offset(center.x + scanAngle, center.y + baseSize * 0.35f)
            drawScope.drawLine(color = auraColor, start = droneCenter, end = scannerTarget, strokeWidth = 2f)
            drawScope.drawCircle(color = auraColor, radius = baseSize * 0.22f, center = scannerTarget)
        }

        // 2. Drone Pulsing Aura
        drawScope.drawCircle(color = auraColor, radius = baseSize * 0.3f, center = droneCenter)

        // 3. Drone Mechanical Chassis
        val dronePath = Path().apply {
            moveTo(droneCenter.x, droneCenter.y - baseSize * 0.16f)
            lineTo(droneCenter.x + baseSize * 0.16f, droneCenter.y)
            lineTo(droneCenter.x, droneCenter.y + baseSize * 0.16f)
            lineTo(droneCenter.x - baseSize * 0.16f, droneCenter.y)
            close()
        }
        drawScope.drawPath(dronePath, color = Color(0xFF1E0A10), style = Fill)
        drawScope.drawPath(dronePath, color = enemyColor, style = Stroke(width = 2.2f))

        // 4. Central Threat Core
        drawScope.drawCircle(color = enemyColor, radius = baseSize * 0.07f, center = droneCenter)
        drawScope.drawCircle(color = Color.White, radius = baseSize * 0.03f, center = droneCenter)

        // Alert Indicator '!'
        if (isAlert && !isStun) {
            val alertPos = Offset(droneCenter.x, droneCenter.y - baseSize * 0.3f)
            drawScope.drawCircle(color = CyberLaserRed, radius = 7f, center = alertPos)
            drawScope.drawCircle(color = Color.White, radius = 4f, center = alertPos)
        }

        // Stun Electrical Sparks
        if (isStun) {
            val sparkOffset1 = Offset(droneCenter.x - 12f, droneCenter.y - 10f)
            val sparkOffset2 = Offset(droneCenter.x + 12f, droneCenter.y + 10f)
            drawScope.drawCircle(color = CyberAmberWarning, radius = 4f, center = sparkOffset1)
            drawScope.drawCircle(color = Color.White, radius = 2f, center = sparkOffset1)
            drawScope.drawCircle(color = CyberAmberWarning, radius = 4f, center = sparkOffset2)
        }
    }

    fun findTileAtScreenPoint(
        tapPoint: Offset,
        level: LevelData,
        camera: Camera3D,
        width: Float,
        height: Float
    ): LevelTile? {
        val centerX = width / 2f + camera.panX
        val centerY = height / 2f + camera.panY

        val baseTileSize = (minOf(width, height) / (level.gridWidth + 3)) * camera.zoom
        val tileW = baseTileSize
        val tileH = baseTileSize * camera.pitchRatio
        val tileDepthZ = baseTileSize * 0.45f

        val yawRad = (camera.yawDegrees * PI / 180.0).toFloat()
        val cosYaw = cos(yawRad)
        val sinYaw = sin(yawRad)

        val centerGridX = (level.gridWidth - 1) / 2f
        val centerGridY = (level.gridHeight - 1) / 2f

        fun project(x: Float, y: Float, z: Float): Offset {
            val relX = x - centerGridX
            val relY = y - centerGridY
            val rotX = relX * cosYaw - relY * sinYaw
            val rotY = relX * sinYaw + relY * cosYaw
            val sx = centerX + (rotX - rotY) * (tileW * 0.72f)
            val sy = centerY + (rotX + rotY) * (tileH * 0.72f) - (z * tileDepthZ)
            return Offset(sx, sy)
        }

        return level.tiles.minByOrNull { tile ->
            val p = project(tile.x.toFloat(), tile.y.toFloat(), tile.z.toFloat())
            (p.x - tapPoint.x) * (p.x - tapPoint.x) + (p.y - tapPoint.y) * (p.y - tapPoint.y)
        }?.takeIf { tile ->
            val p = project(tile.x.toFloat(), tile.y.toFloat(), tile.z.toFloat())
            val distSq = (p.x - tapPoint.x) * (p.x - tapPoint.x) + (p.y - tapPoint.y) * (p.y - tapPoint.y)
            distSq < (tileW * 0.65f) * (tileW * 0.65f)
        }
    }

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
