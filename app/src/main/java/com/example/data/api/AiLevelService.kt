package com.example.data.api

import com.example.data.local.entities.GameSettingsEntity
import com.example.data.model.EnemyData
import com.example.data.model.EnemyType
import com.example.data.model.LevelData
import com.example.data.model.LevelTile
import com.example.data.model.Point3D
import com.example.data.model.TerminalData
import com.example.data.model.TileType
import com.example.data.sensor.MotionTelemetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class AiLevelService {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateLevelWithAiOrFallback(
        settings: GameSettingsEntity,
        telemetry: MotionTelemetry,
        themeTitle: String = "Cyber Matrix",
        difficulty: String = "Normal"
    ): Result<LevelData> = withContext(Dispatchers.IO) {
        // If no API key is provided, gracefully use high-fidelity procedural generation based on the sensor telemetry
        if (settings.apiKey.isBlank()) {
            return@withContext Result.success(
                generateProceduralLevel(telemetry, difficulty, "Synthesized via local neural synthesizer (No API key set).")
            )
        }

        try {
            val systemPrompt = """
                You are a Cyberpunk 3D Game Level Generator AI.
                You receive real-world sensor motion data from a user's mobile device:
                - Elevation gain climbed (meters)
                - Depression/descended depth (meters)
                - Step count & directional distance (meters)
                
                Generate a 3D isometric puzzle maze level adhering strictly to the JSON schema below.
                The level must have:
                - gridSize between 7 and 9.
                - Elevated tiers (z: 1 or 2) reflecting the user's elevation climbed.
                - Depression pits (hazard holes or lower pits with z: -1 or 0) reflecting depression descended.
                - 1 to 3 dynamic enemies placed at strategic coordinates.
                - 1 or 2 hacking terminals that unlock gates.
                - 2 to 4 power cores to collect.
                - playerSpawn point.
                - exitPortal point on an elevated or depression sector.
                
                Output ONLY raw valid JSON with no markdown formatting or commentary.
                JSON structure:
                {
                  "levelName": "Sector Neon-9",
                  "description": "Short flavor text",
                  "gridSize": 8,
                  "elevatedCoords": [{"x": 2, "y": 3, "z": 1}, {"x": 2, "y": 4, "z": 2}],
                  "depressionCoords": [{"x": 4, "y": 4}],
                  "wallCoords": [{"x": 1, "y": 2}, {"x": 3, "y": 5}],
                  "enemies": [
                    {
                      "id": "e1",
                      "name": "Grid Stalker",
                      "type": "HUNTER",
                      "x": 6,
                      "y": 2,
                      "z": 0,
                      "aggression": 0.7,
                      "behaviorDescription": "Flanks player based on elevation",
                      "dialogue": "Intruder detected in neural grid."
                    }
                  ],
                  "terminals": [
                    {
                      "id": "t1",
                      "x": 4,
                      "y": 1,
                      "z": 1,
                      "requiredCores": 2,
                      "securityLevel": 2,
                      "puzzlePrompt": "OVERRIDE MAINFRAME LOCK"
                    }
                  ],
                  "powerCores": [{"x": 1, "y": 6, "z": 0}, {"x": 5, "y": 3, "z": 1}],
                  "playerSpawn": {"x": 0, "y": 7, "z": 0},
                  "exitPortal": {"x": 7, "y": 0, "z": 1},
                  "aiBriefing": "Synthesized from real sensor telemetry."
                }
            """.trimIndent()

            val userPrompt = """
                Generate a $difficulty level named '$themeTitle'.
                Sensor Telemetry Input:
                - Elevation Gain Climbed: ${telemetry.elevationGainMeters} meters
                - Elevation Depression Descended: ${telemetry.depressionMeters} meters
                - Step Count: ${telemetry.stepCount} steps
                - Forward/Lateral Distance: ${telemetry.forwardDistanceMeters}m forward, ${telemetry.lateralDistanceMeters}m lateral
                - Compass Heading: ${telemetry.currentHeadingDegrees} degrees
                Create an exciting 3D labyrinth matching these sensor vectors!
            """.trimIndent()

            val responseBody = callLlmProvider(settings, systemPrompt, userPrompt)
            val parsedLevel = parseJsonToLevel(responseBody, telemetry)
            Result.success(parsedLevel)
        } catch (e: Exception) {
            // Fallback to procedural generation with error notice
            val fallback = generateProceduralLevel(
                telemetry,
                difficulty,
                "Local synthesizer fallback: ${e.localizedMessage ?: "API request timed out or failed"}"
            )
            Result.success(fallback)
        }
    }

    suspend fun queryEnemyTacticalBehavior(
        settings: GameSettingsEntity,
        enemy: EnemyData,
        playerPos: Point3D,
        turnNumber: Int
    ): Pair<Point3D, String> = withContext(Dispatchers.IO) {
        if (settings.apiKey.isBlank() || !settings.dynamicAiEnemyEnabled) {
            return@withContext computeHeuristicEnemyMove(enemy, playerPos)
        }

        try {
            val systemPrompt = """
                You are the AI brain of a hostile combat drone named '${enemy.name}' (${enemy.type}) in a 3D cyber grid.
                Player is at (${playerPos.x}, ${playerPos.y}, elevation ${playerPos.z}).
                Enemy is currently at (${enemy.x}, ${enemy.y}, elevation ${enemy.z}).
                Aggression level: ${enemy.aggression}.
                Provide a single step tactical movement delta (dx: -1, 0, or 1; dy: -1, 0, or 1) and short radio dialogue.
                Return ONLY JSON: {"dx": 0, "dy": 1, "dialogue": "Closing in on coordinates."}
            """.trimIndent()

            val userPrompt = "Turn $turnNumber. Make your move."
            val responseBody = callLlmProvider(settings, systemPrompt, userPrompt)
            val cleanJson = extractJsonString(responseBody)
            val json = JSONObject(cleanJson)
            val dx = json.optInt("dx", 0).coerceIn(-1, 1)
            val dy = json.optInt("dy", 0).coerceIn(-1, 1)
            val dialogue = json.optString("dialogue", "Target locked.")
            val newX = (enemy.x + dx).coerceIn(0, 8)
            val newY = (enemy.y + dy).coerceIn(0, 8)
            Pair(Point3D(newX, newY, enemy.z), dialogue)
        } catch (e: Exception) {
            computeHeuristicEnemyMove(enemy, playerPos)
        }
    }

    private fun computeHeuristicEnemyMove(enemy: EnemyData, playerPos: Point3D): Pair<Point3D, String> {
        val dx = when {
            playerPos.x > enemy.x -> 1
            playerPos.x < enemy.x -> -1
            else -> 0
        }
        val dy = when {
            playerPos.y > enemy.y -> 1
            playerPos.y < enemy.y -> -1
            else -> 0
        }

        // Hunter drones track aggressively; Sentinels hold ground and scan
        return when (enemy.type) {
            EnemyType.HUNTER -> {
                // Advance in either X or Y
                val stepX = if (Random.nextBoolean()) dx else 0
                val stepY = if (stepX == 0) dy else 0
                val newX = (enemy.x + stepX).coerceIn(0, 8)
                val newY = (enemy.y + stepY).coerceIn(0, 8)
                Pair(Point3D(newX, newY, enemy.z), "Pursuit protocol active: [${enemy.x},${enemy.y}] -> [$newX,$newY]")
            }
            EnemyType.SENTINEL -> {
                Pair(Point3D(enemy.x, enemy.y, enemy.z), "High-ground laser turret tracking target...")
            }
            EnemyType.PHANTOM -> {
                // Glitches or jumps toward player
                val stepX = if (Random.nextFloat() < 0.6f) dx else Random.nextInt(-1, 2)
                val stepY = if (Random.nextFloat() < 0.6f) dy else Random.nextInt(-1, 2)
                val newX = (enemy.x + stepX).coerceIn(0, 8)
                val newY = (enemy.y + stepY).coerceIn(0, 8)
                Pair(Point3D(newX, newY, enemy.z), "Phase shift anomaly detected!")
            }
            EnemyType.STALKER -> {
                val stepX = if (Random.nextFloat() < 0.5f) dx else 0
                val stepY = if (stepX == 0) dy else 0
                val newX = (enemy.x + stepX).coerceIn(0, 8)
                val newY = (enemy.y + stepY).coerceIn(0, 8)
                Pair(Point3D(newX, newY, enemy.z), "Circumventing flank angles...")
            }
        }
    }

    private fun callLlmProvider(
        settings: GameSettingsEntity,
        systemPrompt: String,
        userPrompt: String
    ): String {
        return when (settings.apiProvider.uppercase()) {
            "GEMINI" -> callGeminiApi(settings, systemPrompt, userPrompt)
            "OPENROUTER" -> callOpenAiCompatible(
                url = "https://openrouter.ai/api/v1/chat/completions",
                apiKey = settings.apiKey,
                model = settings.modelId.ifBlank { "meta-llama/llama-3.3-70b-instruct" },
                systemPrompt = systemPrompt,
                userPrompt = userPrompt,
                extraHeaders = mapOf(
                    "HTTP-Referer" to "https://cybermaze3d.ai",
                    "X-Title" to "CyberMaze 3D"
                )
            )
            "GROQ" -> callOpenAiCompatible(
                url = "https://api.groq.com/openai/v1/chat/completions",
                apiKey = settings.apiKey,
                model = settings.modelId.ifBlank { "llama-3.3-70b-versatile" },
                systemPrompt = systemPrompt,
                userPrompt = userPrompt
            )
            "OPENAI" -> callOpenAiCompatible(
                url = "https://api.openai.com/v1/chat/completions",
                apiKey = settings.apiKey,
                model = settings.modelId.ifBlank { "gpt-4o-mini" },
                systemPrompt = systemPrompt,
                userPrompt = userPrompt
            )
            else -> {
                val endpoint = if (settings.customBaseUrl.isNotBlank()) {
                    if (settings.customBaseUrl.endsWith("/chat/completions")) settings.customBaseUrl
                    else "${settings.customBaseUrl.trimEnd('/')}/chat/completions"
                } else "https://api.groq.com/openai/v1/chat/completions"

                callOpenAiCompatible(
                    url = endpoint,
                    apiKey = settings.apiKey,
                    model = settings.modelId.ifBlank { "llama-3.3-70b-versatile" },
                    systemPrompt = systemPrompt,
                    userPrompt = userPrompt
                )
            }
        }
    }

    private fun callOpenAiCompatible(
        url: String,
        apiKey: String,
        model: String,
        systemPrompt: String,
        userPrompt: String,
        extraHeaders: Map<String, String> = emptyMap()
    ): String {
        val root = JSONObject()
        root.put("model", model)
        root.put("temperature", 0.7)

        val messages = JSONArray()
        val sysMsg = JSONObject().apply {
            put("role", "system")
            put("content", systemPrompt)
        }
        val userMsg = JSONObject().apply {
            put("role", "user")
            put("content", userPrompt)
        }
        messages.put(sysMsg)
        messages.put(userMsg)
        root.put("messages", messages)

        val requestBuilder = Request.Builder()
            .url(url)
            .post(root.toString().toRequestBody(jsonMediaType))
            .addHeader("Authorization", "Bearer $apiKey")

        extraHeaders.forEach { (k, v) -> requestBuilder.addHeader(k, v) }

        val response = httpClient.newCall(requestBuilder.build()).execute()
        val body = response.body?.string().orEmpty()
        if (!response.isSuccessful) {
            throw RuntimeException("API error ${response.code}: $body")
        }

        val json = JSONObject(body)
        val choices = json.getJSONArray("choices")
        if (choices.length() == 0) throw RuntimeException("Empty choices in response")
        return choices.getJSONObject(0).getJSONObject("message").getString("content")
    }

    private fun callGeminiApi(
        settings: GameSettingsEntity,
        systemPrompt: String,
        userPrompt: String
    ): String {
        val model = settings.modelId.ifBlank { "gemini-1.5-flash" }
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=${settings.apiKey}"

        val root = JSONObject()
        val contents = JSONArray()
        val contentObj = JSONObject()
        val parts = JSONArray()
        val partObj = JSONObject()
        partObj.put("text", "$systemPrompt\n\n$userPrompt")
        parts.put(partObj)
        contentObj.put("parts", parts)
        contents.put(contentObj)
        root.put("contents", contents)

        val request = Request.Builder()
            .url(url)
            .post(root.toString().toRequestBody(jsonMediaType))
            .build()

        val response = httpClient.newCall(request).execute()
        val body = response.body?.string().orEmpty()
        if (!response.isSuccessful) {
            throw RuntimeException("Gemini error ${response.code}: $body")
        }

        val json = JSONObject(body)
        val candidates = json.getJSONArray("candidates")
        val candidate = candidates.getJSONObject(0)
        val textPart = candidate.getJSONObject("content").getJSONArray("parts").getJSONObject(0)
        return textPart.getString("text")
    }

    private fun extractJsonString(raw: String): String {
        var trimmed = raw.trim()
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.removePrefix("```json").trim()
        }
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.removePrefix("```").trim()
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.removeSuffix("```").trim()
        }
        val firstBrace = trimmed.indexOf('{')
        val lastBrace = trimmed.lastIndexOf('}')
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            return trimmed.substring(firstBrace, lastBrace + 1)
        }
        return trimmed
    }

    fun parseJsonToLevel(jsonContent: String, telemetry: MotionTelemetry): LevelData {
        val clean = extractJsonString(jsonContent)
        val obj = JSONObject(clean)

        val levelName = obj.optString("levelName", "Synthesized Sector")
        val description = obj.optString("description", "Constructed from physical movements.")
        val gridSize = obj.optInt("gridSize", 8).coerceIn(6, 10)
        val aiBriefing = obj.optString("aiBriefing", "Grid online.")

        val elevatedMap = mutableMapOf<Pair<Int, Int>, Int>()
        val elevatedArr = obj.optJSONArray("elevatedCoords")
        if (elevatedArr != null) {
            for (i in 0 until elevatedArr.length()) {
                val pt = elevatedArr.getJSONObject(i)
                elevatedMap[Pair(pt.optInt("x"), pt.optInt("y"))] = pt.optInt("z", 1).coerceIn(1, 3)
            }
        }

        val depressionSet = mutableSetOf<Pair<Int, Int>>()
        val depressionArr = obj.optJSONArray("depressionCoords")
        if (depressionArr != null) {
            for (i in 0 until depressionArr.length()) {
                val pt = depressionArr.getJSONObject(i)
                depressionSet.add(Pair(pt.optInt("x"), pt.optInt("y")))
            }
        }

        val wallSet = mutableSetOf<Pair<Int, Int>>()
        val wallArr = obj.optJSONArray("wallCoords")
        if (wallArr != null) {
            for (i in 0 until wallArr.length()) {
                val pt = wallArr.getJSONObject(i)
                wallSet.add(Pair(pt.optInt("x"), pt.optInt("y")))
            }
        }

        val powerCoreList = mutableListOf<Point3D>()
        val coresArr = obj.optJSONArray("powerCores")
        if (coresArr != null) {
            for (i in 0 until coresArr.length()) {
                val pt = coresArr.getJSONObject(i)
                val x = pt.optInt("x").coerceIn(0, gridSize - 1)
                val y = pt.optInt("y").coerceIn(0, gridSize - 1)
                val z = elevatedMap[Pair(x, y)] ?: 0
                powerCoreList.add(Point3D(x, y, z))
            }
        }

        val terminalList = mutableListOf<TerminalData>()
        val terminalsArr = obj.optJSONArray("terminals")
        if (terminalsArr != null) {
            for (i in 0 until terminalsArr.length()) {
                val term = terminalsArr.getJSONObject(i)
                val x = term.optInt("x").coerceIn(0, gridSize - 1)
                val y = term.optInt("y").coerceIn(0, gridSize - 1)
                val z = elevatedMap[Pair(x, y)] ?: 0
                terminalList.add(
                    TerminalData(
                        id = term.optString("id", "t$i"),
                        x = x,
                        y = y,
                        z = z,
                        requiredCores = term.optInt("requiredCores", 1),
                        securityLevel = term.optInt("securityLevel", 1),
                        puzzlePrompt = term.optString("puzzlePrompt", "ACCESS CYBER NODE")
                    )
                )
            }
        }

        val enemyList = mutableListOf<EnemyData>()
        val enemiesArr = obj.optJSONArray("enemies")
        if (enemiesArr != null) {
            for (i in 0 until enemiesArr.length()) {
                val en = enemiesArr.getJSONObject(i)
                val typeStr = en.optString("type", "HUNTER").uppercase()
                val type = try {
                    EnemyType.valueOf(typeStr)
                } catch (e: Exception) {
                    EnemyType.HUNTER
                }
                val ex = en.optInt("x").coerceIn(0, gridSize - 1)
                val ey = en.optInt("y").coerceIn(0, gridSize - 1)
                val ez = elevatedMap[Pair(ex, ey)] ?: 0
                enemyList.add(
                    EnemyData(
                        id = en.optString("id", "e$i"),
                        name = en.optString("name", "Drone $i"),
                        type = type,
                        x = ex,
                        y = ey,
                        z = ez,
                        aggression = en.optDouble("aggression", 0.6).toFloat(),
                        behaviorDescription = en.optString("behaviorDescription", "Scanning grid"),
                        lastActionText = en.optString("dialogue", "Patrol routine initiated.")
                    )
                )
            }
        }

        val spawnObj = obj.optJSONObject("playerSpawn")
        val playerSpawn = if (spawnObj != null) {
            val sx = spawnObj.optInt("x", 0).coerceIn(0, gridSize - 1)
            val sy = spawnObj.optInt("y", gridSize - 1).coerceIn(0, gridSize - 1)
            Point3D(sx, sy, elevatedMap[Pair(sx, sy)] ?: 0)
        } else Point3D(0, gridSize - 1, 0)

        val exitObj = obj.optJSONObject("exitPortal")
        val exitPortal = if (exitObj != null) {
            val ex = exitObj.optInt("x", gridSize - 1).coerceIn(0, gridSize - 1)
            val ey = exitObj.optInt("y", 0).coerceIn(0, gridSize - 1)
            Point3D(ex, ey, elevatedMap[Pair(ex, ey)] ?: 0)
        } else Point3D(gridSize - 1, 0, 0)

        // Build all grid tiles
        val tiles = mutableListOf<LevelTile>()
        for (y in 0 until gridSize) {
            for (x in 0 until gridSize) {
                val pair = Pair(x, y)
                val elevZ = elevatedMap[pair] ?: 0
                val isDepression = depressionSet.contains(pair)
                val isWall = wallSet.contains(pair) && pair != Pair(playerSpawn.x, playerSpawn.y) && pair != Pair(exitPortal.x, exitPortal.y)
                val isCore = powerCoreList.any { it.x == x && it.y == y }
                val isTerm = terminalList.any { it.x == x && it.y == y }
                val isExit = exitPortal.x == x && exitPortal.y == y

                val tileType = when {
                    isExit -> TileType.EXIT_PORTAL
                    isTerm -> TileType.TERMINAL
                    isCore -> TileType.POWER_CORE
                    isWall -> TileType.WALL
                    isDepression -> TileType.DEPRESSION_PIT
                    elevZ > 0 -> TileType.ELEVATION_RAMP
                    else -> TileType.FLOOR
                }

                tiles.add(
                    LevelTile(
                        x = x,
                        y = y,
                        z = if (isDepression) -1 else elevZ,
                        type = tileType
                    )
                )
            }
        }

        return LevelData(
            name = levelName,
            description = description,
            gridWidth = gridSize,
            gridHeight = gridSize,
            tiles = tiles,
            enemies = enemyList,
            terminals = terminalList,
            playerSpawn = playerSpawn,
            exitPortal = exitPortal,
            totalCoresNeeded = max(1, powerCoreList.size),
            sourceElevationGain = telemetry.elevationGainMeters,
            sourceDepression = telemetry.depressionMeters,
            sourceSteps = telemetry.stepCount,
            sourceDistance = telemetry.totalDistanceMeters,
            aiBriefing = aiBriefing
        )
    }

    /**
     * High-fidelity procedural level generator that translates raw real-world sensor telemetry:
     * - Elevation Gain creates towering 3D elevation platforms and ascending ramps!
     * - Depression creates sunken trenches and hazard pits!
     * - Step count and compass heading dictate maze branching and labyrinth corridor layouts!
     */
    fun generateProceduralLevel(
        telemetry: MotionTelemetry,
        difficulty: String,
        briefingMsg: String = "Synthesized directly from sensor telemetry."
    ): LevelData {
        val gridSize = 8
        val tiles = mutableListOf<LevelTile>()
        val enemies = mutableListOf<EnemyData>()
        val terminals = mutableListOf<TerminalData>()

        // 1. Elevation tiers count proportional to elevation climbed (e.g. 5m gain = tier 1, 10m gain = tier 2)
        val elevationTiers = min(3, max(1, (telemetry.elevationGainMeters / 4f).toInt()))
        // Depression depth proportional to depression sensor drop
        val hasDepressionHazards = telemetry.depressionMeters > 0.5f

        val playerSpawn = Point3D(0, gridSize - 1, 0)
        val exitPortal = Point3D(gridSize - 1, 0, elevationTiers)

        // Seeded pseudorandom from telemetry steps and elevation
        val seed = (telemetry.stepCount * 31 + (telemetry.elevationGainMeters * 100).toInt() + (telemetry.depressionMeters * 50).toInt()).toLong()
        val rng = Random(if (seed != 0L) seed else 42L)

        // Generate heightmap based on distance from spawn to exit (ascending)
        for (y in 0 until gridSize) {
            for (x in 0 until gridSize) {
                val distFromSpawn = x + (gridSize - 1 - y)
                var z = (distFromSpawn * elevationTiers / (gridSize * 2 - 2)).coerceIn(0, elevationTiers)

                var type = TileType.FLOOR
                if (z > 0) {
                    type = TileType.ELEVATION_RAMP
                }

                // Place depression pits in random middle sectors if depression was detected
                if (hasDepressionHazards && (x in 2..5 && y in 2..5) && rng.nextFloat() < 0.22f) {
                    type = TileType.DEPRESSION_PIT
                    z = -1
                } else if ((x != playerSpawn.x || y != playerSpawn.y) && (x != exitPortal.x || y != exitPortal.y)) {
                    // Place walls to form labyrinth corridors
                    if (rng.nextFloat() < 0.16f) {
                        type = TileType.WALL
                    }
                }

                tiles.add(LevelTile(x = x, y = y, z = z, type = type))
            }
        }

        // Place Exit Portal
        val exitIndex = tiles.indexOfFirst { it.x == exitPortal.x && it.y == exitPortal.y }
        if (exitIndex != -1) {
            tiles[exitIndex] = tiles[exitIndex].copy(type = TileType.EXIT_PORTAL, z = elevationTiers)
        }

        // Place Player Spawn
        val spawnIndex = tiles.indexOfFirst { it.x == playerSpawn.x && it.y == playerSpawn.y }
        if (spawnIndex != -1) {
            tiles[spawnIndex] = tiles[spawnIndex].copy(type = TileType.FLOOR, z = 0)
        }

        // Place 3 Power Cores
        val coreCoords = listOf(
            Point3D(1, gridSize - 3, 0),
            Point3D(gridSize / 2, gridSize / 2, 1),
            Point3D(gridSize - 2, 2, elevationTiers)
        )
        coreCoords.forEach { pt ->
            val idx = tiles.indexOfFirst { it.x == pt.x && it.y == pt.y }
            if (idx != -1 && tiles[idx].type != TileType.EXIT_PORTAL && tiles[idx].type != TileType.FLOOR) {
                tiles[idx] = tiles[idx].copy(type = TileType.POWER_CORE, z = pt.z)
            } else if (idx != -1) {
                tiles[idx] = tiles[idx].copy(type = TileType.POWER_CORE, z = pt.z)
            }
        }

        // Place 1 Terminal near mid-high tier
        val termPt = Point3D(gridSize - 3, 3, (elevationTiers / 2).coerceAtLeast(0))
        val termIdx = tiles.indexOfFirst { it.x == termPt.x && it.y == termPt.y }
        if (termIdx != -1) {
            tiles[termIdx] = tiles[termIdx].copy(type = TileType.TERMINAL, z = termPt.z)
            terminals.add(
                TerminalData(
                    id = "term_alpha",
                    x = termPt.x,
                    y = termPt.y,
                    z = termPt.z,
                    requiredCores = 2,
                    securityLevel = if (difficulty == "Cyberpunk") 2 else 1,
                    puzzlePrompt = "DECRYPT TERMINAL SECURITY PASSCODE"
                )
            )
        }

        // Place Enemies based on movement intensity
        // 1. Hunter Drone: tracks player
        enemies.add(
            EnemyData(
                id = "drone_hunter",
                name = "Viper-9 Hunter",
                type = EnemyType.HUNTER,
                x = gridSize - 2,
                y = gridSize - 2,
                z = 0,
                aggression = if (difficulty == "Nightmare") 0.9f else 0.6f,
                behaviorDescription = "Dynamic predator stalking player movement patterns.",
                lastActionText = "Active pursuit mode engaged."
            )
        )

        // 2. High ground Sentinel: guards top elevation
        if (elevationTiers > 0) {
            enemies.add(
                EnemyData(
                    id = "drone_sentinel",
                    name = "Aegis-3 Sentinel",
                    type = EnemyType.SENTINEL,
                    x = exitPortal.x - 1,
                    y = exitPortal.y + 1,
                    z = elevationTiers,
                    aggression = 0.5f,
                    behaviorDescription = "Guards apex summit with scanning sensor.",
                    lastActionText = "Surveying lower terrain elevation..."
                )
            )
        }

        // 3. Phantom in depression hazard (if depression was present)
        if (hasDepressionHazards) {
            enemies.add(
                EnemyData(
                    id = "drone_phantom",
                    name = "Glitch Mirage",
                    type = EnemyType.PHANTOM,
                    x = 3,
                    y = 3,
                    z = -1,
                    aggression = 0.75f,
                    behaviorDescription = "Teleports through sensor depression anomalies.",
                    lastActionText = "Sub-level presence detected."
                )
            )
        }

        return LevelData(
            name = "Sector ${rng.nextInt(10, 99)}: Neon ${if (telemetry.elevationGainMeters > 3) "Ascent" else "Labyrinth"}",
            description = "Generated from ${String.format("%.1f", telemetry.elevationGainMeters)}m climb, ${String.format("%.1f", telemetry.depressionMeters)}m descent, and ${telemetry.stepCount} steps.",
            gridWidth = gridSize,
            gridHeight = gridSize,
            tiles = tiles,
            enemies = enemies,
            terminals = terminals,
            playerSpawn = playerSpawn,
            exitPortal = exitPortal,
            totalCoresNeeded = coreCoords.size,
            sourceElevationGain = telemetry.elevationGainMeters,
            sourceDepression = telemetry.depressionMeters,
            sourceSteps = telemetry.stepCount,
            sourceDistance = telemetry.totalDistanceMeters,
            aiBriefing = briefingMsg
        )
    }
}
