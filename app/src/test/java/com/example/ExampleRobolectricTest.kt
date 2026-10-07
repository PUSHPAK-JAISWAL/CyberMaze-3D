package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DefenseType
import com.example.data.model.TroopType
import com.example.game.engine.SiegeEngine
import com.example.update.VersionUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun `siege engine initializes player base with core server and walls`() {
        val engine = SiegeEngine()
        val base = engine.playerBase.value

        assertTrue(base.isNotEmpty())
        assertTrue("Base should have a Quantum Core Server", base.any { it.type == DefenseType.CORE_SERVER })
        assertTrue("Base should have perimeter neon walls", base.any { it.type == DefenseType.NEON_WALL })
    }

    @Test
    fun `siege engine allows placing and removing defense buildings`() {
        val engine = SiegeEngine()

        // Place a Laser Turret at (2, 3)
        val placed = engine.placeBuildingOnBase(DefenseType.LASER_TURRET, 2, 3)
        assertTrue(placed)
        assertTrue(engine.playerBase.value.any { it.gridX == 2 && it.gridY == 3 && it.type == DefenseType.LASER_TURRET })

        // Remove it
        val removed = engine.removeBuildingFromBase(2, 3)
        assertTrue(removed)
        assertFalse(engine.playerBase.value.any { it.gridX == 2 && it.gridY == 3 })
    }

    @Test
    fun `siege engine handles raid battles and troop deployment`() {
        val engine = SiegeEngine()
        engine.startRaidSector(1)

        val initial = engine.raidState.value
        assertEquals("Sector 1: Neon Alley", initial.sectorName)
        assertTrue(initial.buildings.isNotEmpty())

        // Deploy a Byte Brawler at edge
        var deployed = false
        engine.deployTroop(TroopType.BYTE_BRAWLER, 1f, 1f, onDeployed = { deployed = true }, onFail = {})
        assertTrue(deployed)
        assertEquals(1, engine.raidState.value.troops.size)

        // Test Level 8 generation
        engine.startRaidSector(8)
        assertEquals("Sector 8: Zero-Day Dark Citadel", engine.raidState.value.sectorName)
        assertTrue(engine.raidState.value.buildings.size >= 8)

        // Test Level 12 generation
        engine.startRaidSector(12)
        assertEquals("Sector 12: Cyber Overlord Nexus", engine.raidState.value.sectorName)
        assertTrue(engine.raidState.value.buildings.size >= 12)
    }

    @Test
    fun `siege engine handles pokemon go style nearby player base raid`() {
        val engine = SiegeEngine()
        val nearbyBase = com.example.data.model.NearbyPlayerBase(
            id = "test_p1",
            architectName = "Shadow_Walker",
            rankTitle = "Apex",
            distanceMeters = 30,
            angleDegrees = 45f,
            trophyCount = 500,
            lootableBits = 300,
            buildings = listOf(
                com.example.data.model.MazeBuilding(1, DefenseType.CORE_SERVER, 4, 4, 1200f, 1200f),
                com.example.data.model.MazeBuilding(2, DefenseType.LASER_TURRET, 3, 3, 300f, 300f)
            )
        )

        engine.startNearbyPlayerRaid(nearbyBase)
        val state = engine.raidState.value
        assertTrue(state.sectorName.contains("Shadow_Walker"))
        assertEquals(2, state.buildings.size)
        assertFalse(state.battleEnded)
    }

    @Test
    fun `main view model instantiates and connects siege engine`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = com.example.ui.MainViewModel(app)
        assertNotNull(vm)
        assertNotNull(vm.raidBattleState.value)
        assertTrue(vm.playerBase.value.isNotEmpty())
        assertTrue(vm.cards.value.isNotEmpty())
        assertTrue(vm.radarNodes.value.isNotEmpty())
        assertTrue(vm.userBits.value > 0)
    }

    @Test
    fun `version util correctly identifies newer releases`() {
        // Same version
        assertFalse(VersionUtil.isUpdateAvailable("1.1.2", "v1.1.2"))
        assertFalse(VersionUtil.isUpdateAvailable("1.1.2", "1.1.2"))

        // Remote newer
        assertTrue(VersionUtil.isUpdateAvailable("1.1.2", "v1.1.3"))
        assertTrue(VersionUtil.isUpdateAvailable("1.1.2", "v1.2.0"))
        assertTrue(VersionUtil.isUpdateAvailable("1.1.2", "v2.0.0"))

        // Local newer
        assertFalse(VersionUtil.isUpdateAvailable("1.2.0", "v1.1.9"))
        assertFalse(VersionUtil.isUpdateAvailable("2.0.0", "v1.9.9"))
    }
}
