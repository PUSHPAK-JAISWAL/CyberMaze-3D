package com.example.game.engine

import com.example.data.model.BattleCard
import com.example.data.model.BattleCardType
import com.example.data.model.CollectibleItem
import com.example.data.model.CollectibleType
import com.example.data.model.ObstacleItem
import com.example.data.model.ObstacleType
import com.example.data.model.RunnerGameState
import com.example.data.model.RunnerLane
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.random.Random

class GameSession(initialHighScore: Long = 0) {

    private val defaultCards = listOf(
        BattleCard(
            id = "emp_blast",
            type = BattleCardType.EMP_BLAST,
            name = "EMP Shock",
            description = "Obliterates obstacles ahead and repels the Hunter Drone",
            energyCost = 25,
            cooldownSeconds = 8f
        ),
        BattleCard(
            id = "warp_drive",
            type = BattleCardType.WARP_DRIVE,
            name = "Warp Drive",
            description = "Hyper-speed invincibility & coin vacuum for 5 seconds",
            energyCost = 45,
            cooldownSeconds = 15f
        ),
        BattleCard(
            id = "shield_matrix",
            type = BattleCardType.SHIELD_MATRIX,
            name = "Cyber Shield",
            description = "Deploys a protective barrier absorbing 1 crash",
            energyCost = 20,
            cooldownSeconds = 6f
        ),
        BattleCard(
            id = "chrono_slow",
            type = BattleCardType.CHRONO_SLOW,
            name = "Chrono Slow",
            description = "Slows matrix speed by 50% for 5 seconds for easy dodges",
            energyCost = 30,
            cooldownSeconds = 10f
        )
    )

    private val _state = MutableStateFlow(
        RunnerGameState(
            activeDeck = defaultCards,
            highScore = initialHighScore
        )
    )
    val state: StateFlow<RunnerGameState> = _state.asStateFlow()

    private var nextObstacleZ = 25f
    private var nextItemId: Long = 1
    private var jumpVelocityY = 0f
    private val gravity = -24f

    init {
        spawnInitialTrackPatterns()
    }

    fun resetRun() {
        val currentHighScore = maxOf(_state.value.highScore, _state.value.score)
        nextObstacleZ = 25f
        jumpVelocityY = 0f

        _state.value = RunnerGameState(
            activeDeck = defaultCards.map { it.copy(currentCooldown = 0f) },
            highScore = currentHighScore
        )
        spawnInitialTrackPatterns()
    }

    private fun spawnInitialTrackPatterns() {
        val initialObstacles = mutableListOf<ObstacleItem>()
        val initialCollectibles = mutableListOf<CollectibleItem>()

        var z = 20f
        for (i in 0 until 8) {
            z += Random.nextInt(18, 30).toFloat()
            val lane = RunnerLane.fromIndex(Random.nextInt(-1, 2))
            val obsType = when (Random.nextInt(0, 4)) {
                0 -> ObstacleType.LOW_BARRIER
                1 -> ObstacleType.HIGH_LASER
                2 -> ObstacleType.GLITCH_PIT
                else -> ObstacleType.ELEVATED_RAMP
            }
            initialObstacles.add(
                ObstacleItem(
                    id = nextItemId++,
                    lane = lane,
                    zDistance = z,
                    type = obsType
                )
            )

            // Spawn neon bits in other lanes
            val otherLane = if (lane == RunnerLane.CENTER) RunnerLane.LEFT else RunnerLane.CENTER
            for (coinStep in 0..3) {
                initialCollectibles.add(
                    CollectibleItem(
                        id = nextItemId++,
                        lane = otherLane,
                        zDistance = z - 6f + (coinStep * 3f),
                        type = CollectibleType.NEON_BIT
                    )
                )
            }
        }
        nextObstacleZ = z + 20f

        _state.value = _state.value.copy(
            obstacles = initialObstacles,
            collectibles = initialCollectibles
        )
    }

    fun swipeLeft(onAction: () -> Unit) {
        val cur = _state.value
        if (cur.isGameOver) return
        val newLane = when (cur.targetLane) {
            RunnerLane.RIGHT -> RunnerLane.CENTER
            RunnerLane.CENTER -> RunnerLane.LEFT
            RunnerLane.LEFT -> RunnerLane.LEFT
        }
        if (newLane != cur.targetLane) {
            _state.value = cur.copy(targetLane = newLane)
            onAction()
        }
    }

    fun swipeRight(onAction: () -> Unit) {
        val cur = _state.value
        if (cur.isGameOver) return
        val newLane = when (cur.targetLane) {
            RunnerLane.LEFT -> RunnerLane.CENTER
            RunnerLane.CENTER -> RunnerLane.RIGHT
            RunnerLane.RIGHT -> RunnerLane.RIGHT
        }
        if (newLane != cur.targetLane) {
            _state.value = cur.copy(targetLane = newLane)
            onAction()
        }
    }

    fun jump(onAction: () -> Unit) {
        val cur = _state.value
        if (cur.isGameOver) return
        if (cur.playerY <= 0.05f) {
            jumpVelocityY = 9.5f
            _state.value = cur.copy(
                isJumping = true,
                isSliding = false,
                slideRemainingSeconds = 0f
            )
            onAction()
        }
    }

    fun slide(onAction: () -> Unit) {
        val cur = _state.value
        if (cur.isGameOver) return
        // If jumping, dive downward fast
        if (cur.isJumping) {
            jumpVelocityY = -15f
        }
        _state.value = cur.copy(
            isSliding = true,
            slideRemainingSeconds = 0.75f
        )
        onAction()
    }

    fun deployCard(cardType: BattleCardType, onSuccess: () -> Unit, onFail: () -> Unit) {
        val cur = _state.value
        if (cur.isGameOver) return
        val card = cur.activeDeck.find { it.type == cardType } ?: return

        if (cur.energyCharge < card.energyCost || card.currentCooldown > 0f) {
            onFail()
            return
        }

        var newObstacles = cur.obstacles
        var hasShield = cur.hasShield
        var isWarp = cur.isWarpActive
        var warpTime = cur.warpRemainingSeconds
        var isChrono = cur.isChronoSlowActive
        var chronoTime = cur.chronoRemainingSeconds
        var newDroneDist = cur.bossDroneDistance
        var banner = "Tactical Card Dispatched!"

        when (cardType) {
            BattleCardType.EMP_BLAST -> {
                // Destroy all obstacles within 50m and repel Hunter drone
                newObstacles = newObstacles.map { obs ->
                    if (obs.zDistance in 0f..50f) obs.copy(isDestroyed = true) else obs
                }
                newDroneDist = (newDroneDist + 12f).coerceAtMost(22f)
                banner = "⚡ EMP OVERDRIVE! Path cleared & Drone repelled!"
            }
            BattleCardType.WARP_DRIVE -> {
                isWarp = true
                warpTime = 5.5f
                banner = "🚀 WARP DRIVE ACTIVE! Hyper-speed invincibility!"
            }
            BattleCardType.SHIELD_MATRIX -> {
                hasShield = true
                banner = "🛡️ CYBER SHIELD ARMED! 1 crash protected."
            }
            BattleCardType.CHRONO_SLOW -> {
                isChrono = true
                chronoTime = 5.0f
                banner = "⏱️ CHRONO SLOW ENGAGED! Matrix time dilation."
            }
            BattleCardType.DRONE_STRIKE -> {
                // Blast the 3 closest obstacles
                var count = 0
                newObstacles = newObstacles.map { obs ->
                    if (!obs.isDestroyed && obs.zDistance > 0f && count < 3) {
                        count++
                        obs.copy(isDestroyed = true)
                    } else obs
                }
                banner = "🛸 ALLY DRONE STRIKE! 3 obstacles obliterated."
            }
        }

        val updatedDeck = cur.activeDeck.map {
            if (it.type == cardType) it.copy(currentCooldown = it.cooldownSeconds) else it
        }

        _state.value = cur.copy(
            energyCharge = cur.energyCharge - card.energyCost,
            obstacles = newObstacles,
            hasShield = hasShield,
            isWarpActive = isWarp,
            warpRemainingSeconds = warpTime,
            isChronoSlowActive = isChrono,
            chronoRemainingSeconds = chronoTime,
            bossDroneDistance = newDroneDist,
            activeDeck = updatedDeck,
            lastBannerMessage = banner
        )
        onSuccess()
    }

    fun tickFrame(dt: Float, onCrash: () -> Unit, onStumble: () -> Unit, onCoin: () -> Unit) {
        val cur = _state.value
        if (cur.isGameOver) return

        val speedMultiplier = when {
            cur.isWarpActive -> 1.7f
            cur.isChronoSlowActive -> 0.5f
            else -> 1.0f
        }
        val effectiveSpeed = (cur.speedMps + (cur.distanceMeters * 0.005f).coerceAtMost(10f)) * speedMultiplier
        val stepDist = effectiveSpeed * dt

        // 1. Smooth Lane Interpolation
        val targetX = cur.targetLane.index.toFloat()
        val newLanePos = cur.lanePosition + (targetX - cur.lanePosition) * (14f * dt).coerceAtMost(1f)

        // 2. Jump Physics
        var newPlayerY = cur.playerY
        var newIsJumping = cur.isJumping
        if (cur.isJumping || cur.playerY > 0f) {
            jumpVelocityY += gravity * dt
            newPlayerY = (cur.playerY + jumpVelocityY * dt).coerceAtLeast(0f)
            if (newPlayerY <= 0f) {
                newPlayerY = 0f
                jumpVelocityY = 0f
                newIsJumping = false
            }
        }

        // 3. Slide Timer
        var newSlideTime = (cur.slideRemainingSeconds - dt).coerceAtLeast(0f)
        var newIsSliding = newSlideTime > 0f

        // 4. Power-up Timers
        val newWarpTime = (cur.warpRemainingSeconds - dt).coerceAtLeast(0f)
        val newWarpActive = newWarpTime > 0f

        val newChronoTime = (cur.chronoRemainingSeconds - dt).coerceAtLeast(0f)
        val newChronoActive = newChronoTime > 0f

        val newMagnetTime = (cur.magnetRemainingSeconds - dt).coerceAtLeast(0f)
        val newMagnetActive = newMagnetTime > 0f

        // 5. Card Cooldowns
        val updatedDeck = cur.activeDeck.map {
            if (it.currentCooldown > 0f) {
                it.copy(currentCooldown = (it.currentCooldown - dt).coerceAtLeast(0f))
            } else it
        }

        // 6. Boss Drone pursuit logic
        // Drone recovers distance if player is running cleanly (drone falls back to ~16m)
        var newDroneDist = cur.bossDroneDistance
        if (newDroneDist < 16f) {
            newDroneDist += dt * 1.5f
        }

        // 7. Advance Obstacles & Check Collisions
        var hasShield = cur.hasShield
        var stumble = cur.stumbleCount
        var gameOver = false
        var banner = cur.lastBannerMessage

        val updatedObstacles = cur.obstacles.mapNotNull { obs ->
            val newZ = obs.zDistance - stepDist
            if (newZ < -8f) {
                null // Despawn behind player
            } else {
                val updatedObs = obs.copy(zDistance = newZ)

                // Check collision if obstacle is in player lane and near player (z in [-0.5f, 1.2f])
                val inSameLane = abs(newLanePos - obs.lane.index) < 0.65f
                val nearPlayer = newZ in -0.5f..1.2f

                if (inSameLane && nearPlayer && !obs.isCleared && !obs.isDestroyed) {
                    if (newWarpActive) {
                        // Warp destroys obstacle
                        updatedObs.copy(isDestroyed = true, isCleared = true)
                    } else {
                        // Check if dodge succeeded
                        val avoided = when (obs.type) {
                            ObstacleType.LOW_BARRIER, ObstacleType.GLITCH_PIT -> newPlayerY > 0.6f
                            ObstacleType.HIGH_LASER -> newIsSliding
                            ObstacleType.ELEVATED_RAMP -> newPlayerY > 0.4f || newLanePos == obs.lane.index.toFloat()
                            ObstacleType.SECURITY_DRONE -> false
                        }

                        if (avoided) {
                            updatedObs.copy(isCleared = true)
                        } else {
                            // Collision occurred!
                            if (hasShield) {
                                hasShield = false
                                banner = "🛡️ SHIELD BROKEN! Crash absorbed."
                                onStumble()
                                updatedObs.copy(isDestroyed = true, isCleared = true)
                            } else {
                                stumble++
                                if (stumble >= 2 || newDroneDist < 6f) {
                                    gameOver = true
                                    banner = "🚨 BUSTED BY HUNTER DRONE! Sector run terminated."
                                    onCrash()
                                    updatedObs.copy(isCleared = true)
                                } else {
                                    newDroneDist = 4.5f // Drone surges forward!
                                    banner = "⚠️ STUMBLE! Drone right behind you! Don't trip again!"
                                    onStumble()
                                    updatedObs.copy(isCleared = true)
                                }
                            }
                        }
                    }
                } else updatedObs
            }
        }.toMutableList()

        // 8. Advance Collectibles & Check Pickups
        var newCoins = cur.coinsCollected
        var newScore = cur.score + (stepDist * cur.multiplier).toLong()
        var newEnergy = cur.energyCharge
        var magnetActive = newMagnetActive
        var magnetTimer = newMagnetTime
        var shieldActive = hasShield

        val updatedCollectibles = cur.collectibles.mapNotNull { col ->
            val newZ = col.zDistance - stepDist
            if (newZ < -5f) null
            else {
                // If magnet active or warp active, pull coin toward player lane
                val targetLane = if (magnetActive || newWarpActive) newLanePos else col.lane.index.toFloat()
                val laneDiff = abs(newLanePos - targetLane)
                val isPicked = (!col.isCollected && laneDiff < 0.7f && newZ in -0.5f..1.5f)

                if (isPicked) {
                    when (col.type) {
                        CollectibleType.NEON_BIT -> {
                            newCoins += 1
                            newScore += 50 * cur.multiplier
                            onCoin()
                        }
                        CollectibleType.ENERGY_CORE -> {
                            newEnergy = (newEnergy + 20).coerceAtMost(cur.maxEnergy)
                            onCoin()
                        }
                        CollectibleType.MULTIPLIER_CHIP -> {
                            newScore += 200
                        }
                        CollectibleType.MAGNET -> {
                            magnetActive = true
                            magnetTimer = 8f
                            banner = "🧲 COIN MAGNET ACTIVE! Vacuuming neon bits!"
                        }
                        CollectibleType.CYBER_SHIELD -> {
                            shieldActive = true
                            banner = "🛡️ SHIELD MATRIX PICKUP EQUIPPED!"
                        }
                    }
                    null // Collected, remove
                } else {
                    col.copy(zDistance = newZ)
                }
            }
        }.toMutableList()

        // 9. Procedural Track Extension (Infinite Spawner)
        val maxObsZ = updatedObstacles.maxOfOrNull { it.zDistance } ?: 0f
        if (maxObsZ < 70f) {
            val spawnZ = maxOf(maxObsZ + Random.nextInt(16, 26).toFloat(), 60f)
            val lane = RunnerLane.fromIndex(Random.nextInt(-1, 2))
            val type = when (Random.nextInt(0, 5)) {
                0, 1 -> ObstacleType.LOW_BARRIER
                2 -> ObstacleType.HIGH_LASER
                3 -> ObstacleType.GLITCH_PIT
                else -> ObstacleType.ELEVATED_RAMP
            }
            updatedObstacles.add(
                ObstacleItem(
                    id = nextItemId++,
                    lane = lane,
                    zDistance = spawnZ,
                    type = type
                )
            )

            // Spawn companion coin arcs
            val coinLane = if (lane == RunnerLane.CENTER) RunnerLane.RIGHT else RunnerLane.CENTER
            for (c in 0..4) {
                updatedCollectibles.add(
                    CollectibleItem(
                        id = nextItemId++,
                        lane = coinLane,
                        zDistance = spawnZ - 8f + (c * 2.8f),
                        type = if (c == 2 && Random.nextFloat() < 0.25f) CollectibleType.ENERGY_CORE else CollectibleType.NEON_BIT
                    )
                )
            }
        }

        _state.value = cur.copy(
            lanePosition = newLanePos,
            playerY = newPlayerY,
            isJumping = newIsJumping,
            isSliding = newIsSliding,
            slideRemainingSeconds = newSlideTime,
            hasShield = shieldActive,
            isMagnetActive = magnetActive,
            magnetRemainingSeconds = magnetTimer,
            isWarpActive = newWarpActive,
            warpRemainingSeconds = newWarpTime,
            isChronoSlowActive = newChronoActive,
            chronoRemainingSeconds = newChronoTime,
            distanceMeters = cur.distanceMeters + stepDist,
            score = newScore,
            coinsCollected = newCoins,
            energyCharge = newEnergy,
            bossDroneDistance = newDroneDist,
            stumbleCount = stumble,
            obstacles = updatedObstacles,
            collectibles = updatedCollectibles,
            activeDeck = updatedDeck,
            isGameOver = gameOver,
            highScore = maxOf(cur.highScore, newScore),
            lastBannerMessage = banner
        )
    }
}
