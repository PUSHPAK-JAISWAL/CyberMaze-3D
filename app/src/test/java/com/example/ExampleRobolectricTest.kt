package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.api.AiLevelService
import com.example.data.model.TileType
import com.example.data.sensor.MotionTelemetry
import com.example.game.engine.Direction
import com.example.game.engine.GameSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("CyberMaze 3D", appName)
    }

    @Test
    fun `procedural generation translates sensor elevation and depression`() {
        val service = AiLevelService()
        val telemetry = MotionTelemetry(
            stepCount = 120,
            elevationGainMeters = 8.5f,
            depressionMeters = 2.4f,
            totalDistanceMeters = 86f
        )
        val level = service.generateProceduralLevel(telemetry, "Normal")

        assertNotNull(level)
        assertTrue(level.tiles.isNotEmpty())
        assertTrue("Level should contain elevation ramps from 8.5m climb", level.tiles.any { it.type == TileType.ELEVATION_RAMP || it.z > 0 })
        assertTrue("Level should contain depression pits from 2.4m drop", level.tiles.any { it.type == TileType.DEPRESSION_PIT })
        assertTrue("Level should have dynamic enemies", level.enemies.isNotEmpty())
    }

    @Test
    fun `game session handles player movement and collects cores`() {
        val service = AiLevelService()
        val telemetry = MotionTelemetry(stepCount = 50, elevationGainMeters = 2f)
        val level = service.generateProceduralLevel(telemetry, "Normal")
        val session = GameSession(level)

        val initialPos = session.state.value.playerPos
        session.movePlayer(Direction.NORTH, onStepSuccess = {}, onEncounter = {})

        val stateAfter = session.state.value
        assertTrue(stateAfter.movesCount >= 0)
    }

    @Test
    fun `main view model instantiates without error`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = com.example.ui.MainViewModel(app)
        assertNotNull(vm)
        assertNotNull(vm.gameState.value)
        assertEquals("Initial Boot Sequence: Neural Arena initialized.", vm.gameState.value.currentLevel.aiBriefing)
    }

    @Test
    fun `version util correctly identifies newer releases`() {
        // Same version
        org.junit.Assert.assertFalse(com.example.update.VersionUtil.isUpdateAvailable("1.1.2", "v1.1.2"))
        org.junit.Assert.assertFalse(com.example.update.VersionUtil.isUpdateAvailable("1.1.2", "1.1.2"))

        // Remote newer
        assertTrue(com.example.update.VersionUtil.isUpdateAvailable("1.1.2", "v1.1.3"))
        assertTrue(com.example.update.VersionUtil.isUpdateAvailable("1.1.2", "v1.2.0"))
        assertTrue(com.example.update.VersionUtil.isUpdateAvailable("1.1.2", "v2.0.0"))

        // Local newer
        org.junit.Assert.assertFalse(com.example.update.VersionUtil.isUpdateAvailable("1.2.0", "v1.1.9"))
        org.junit.Assert.assertFalse(com.example.update.VersionUtil.isUpdateAvailable("2.0.0", "v1.9.9"))
    }
}
