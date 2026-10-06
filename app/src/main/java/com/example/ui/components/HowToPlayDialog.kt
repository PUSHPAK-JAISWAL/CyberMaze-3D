package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.CyberAmberWarning
import com.example.ui.theme.CyberBackgroundDark
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyanAccent
import com.example.ui.theme.CyberMintLight
import com.example.ui.theme.CyberMintPrimary
import com.example.ui.theme.CyberPurpleNeon
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun HowToPlayDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("1. RAIDS", "2. BASE BUILDER", "3. GO OUTSIDE", "4. LLM & AI")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.5.dp, CyberMintPrimary, RoundedCornerShape(16.dp))
                .testTag("how_to_play_dialog"),
            color = CyberBackgroundDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F382A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = CyberMintPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "OPERATOR FIELD MANUAL",
                                color = TextPrimaryDark,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Rules, Controls, Sensor Walking & AI Systems",
                                color = TextSecondaryDark,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = CyberMintLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) Color(0xFF0E382A) else Color(0xFF071912))
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) CyberMintPrimary else CyberCardBorder,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { selectedTab = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) CyberMintLight else TextSecondaryDark,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Content per Tab
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (selectedTab) {
                        0 -> RaidsGuideSection()
                        1 -> BaseBuilderGuideSection()
                        2 -> OutdoorRewardsSection()
                        3 -> LlmAiFeaturesSection()
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dismiss Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberMintPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Text(
                        text = "GOT IT, ENTER ARENA",
                        color = Color(0xFF003822),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun RaidsGuideSection() {
    CyberCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xFF091E16),
        borderColor = CyberMintPrimary.copy(alpha = 0.5f)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "⚔️ HOW TO PLAY: SYNDICATE RAIDS",
                color = CyberMintLight,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Your mission is to breach rival syndicate mazes and steal their Quantum Core before time runs out (90s).",
                color = TextPrimaryDark,
                fontSize = 11.sp
            )

            RuleItem(
                title = "⚡ 1. Elixir Energy Management",
                desc = "Your Elixir meter charges automatically up to 10⚡. Every troop and tactical spell costs Elixir."
            )

            RuleItem(
                title = "🎯 2. How to Deploy Troops",
                desc = "Tap any Troop Card in the bottom deck, then tap on the maze map perimeter to drop them into battle."
            )

            RuleItem(
                title = "🛡️ 3. Troop Roles & Tactics",
                desc = "• BRAWLER (3⚡): Heavy armored tank. Destroys walls & defenses first.\n• SPRINTER (2⚡): Fast ninja. Ignores traps & rushes straight to the Core Server.\n• EMP HACKER (4⚡): Ranged hacker. Stuns and disables enemy turrets.\n• PHANTOM (5⚡): Aerial hover unit. Glides completely over ground walls!"
            )

            RuleItem(
                title = "💥 4. Tactical Spells",
                desc = "• EMP Surge (3⚡): Stuns all defenses in a radius for 5 seconds.\n• Overclock (2⚡): +80% speed and damage boost to all active troops.\n• Orbital Beam (5⚡): Massive kinetic strike directly on target defenses."
            )

            RuleItem(
                title = "⭐ 5. Victory Conditions",
                desc = "• 1 Star: Destroy 50% of enemy buildings.\n• 2 Stars: Destroy the Quantum Core Server.\n• 3 Stars: 100% Total Wipeout!\nEarn Neon Bits and Trophies to climb the syndicate leagues."
            )
        }
    }
}

@Composable
private fun BaseBuilderGuideSection() {
    CyberCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xFF091E16),
        borderColor = CyberMintPrimary.copy(alpha = 0.5f)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "🏰 HOW TO BUILD: CYBER FORTRESS",
                color = CyberMintLight,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "You are the Cyber Architect. Design your own 8×8 neon maze to defend your Quantum Core Server from rival syndicate invasions.",
                color = TextPrimaryDark,
                fontSize = 11.sp
            )

            RuleItem(
                title = "🧱 1. Create Chokepoints With Neon Walls",
                desc = "Place walls to force enemy troops to walk down narrow, winding corridors instead of rushing straight to your Core."
            )

            RuleItem(
                title = "🔫 2. Defensive Turret Arsenal",
                desc = "• Pulse Laser Turret: Rapid single-target laser with continuous fire.\n• Tesla Shock Pylon: Arc-lightning coil that zaps multiple clustered enemies.\n• Plasma Mortar: Long-range heavy artillery with explosive splash damage.\n• Stealth Glitch Mine: Invisible proximity trap that explodes when stepped on."
            )

            RuleItem(
                title = "🎮 3. Test Defense Simulation",
                desc = "Tap 'TEST DEFENSE' at any time to spawn an AI invasion wave and watch how your maze defenses hold up in real time."
            )
        }
    }
}

@Composable
private fun OutdoorRewardsSection() {
    CyberCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xFF091E16),
        borderColor = CyberMintPrimary.copy(alpha = 0.5f)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "🛰️ WHY GO OUTSIDE? (REAL-WORLD MOVEMENT)",
                color = CyberMintLight,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "CyberMaze connects to your device's actual motion sensors (accelerometer, hardware pedometer, and barometer). Walking outside powers up your army!",
                color = TextPrimaryDark,
                fontSize = 11.sp
            )

            RuleItem(
                title = "⚡ 1. Double Currency While Walking",
                desc = "Every physical step walked outside awards 2× Neon Bits. A 10-minute walk generates thousands of Bits to instantly upgrade your cards without waiting."
            )

            RuleItem(
                title = "💎 2. Exclusive Quantum Nanites",
                desc = "Rare Quantum Nanites cannot be earned on the couch. They are located at physical Darknet Radar Nodes in the real world (80m, 180m, 350m, 600m). Walk to them to decrypt!"
            )

            RuleItem(
                title = "⛰️ 3. Altitude Climbing = Free Orbital Strikes",
                desc = "The device's barometer tracks elevation gained. Climbing +5m of outdoor stairs, ramps, or hills charges the Orbital Ion Satellite, granting free orbital airstrikes in raids!"
            )

            RuleItem(
                title = "🕹️ 4. Testing Indoors or On Emulator?",
                desc = "If you're on a computer emulator or staying inside, tap 'RECON DRONE (+150m)' in the Radar tab to simulate outdoor steps so you're never stuck."
            )
        }
    }
}

@Composable
private fun LlmAiFeaturesSection() {
    CyberCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xFF091E16),
        borderColor = CyberMintPrimary.copy(alpha = 0.5f)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "🧠 WHERE IS THE LLM USED IN CYBERMAZE?",
                color = CyberMintLight,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "CyberMaze features multi-provider Bring-Your-Own-Key (BYOK) AI integration supporting Google Gemini, Groq (Llama-3), OpenRouter, and OpenAI.",
                color = TextPrimaryDark,
                fontSize = 11.sp
            )

            RuleItem(
                title = "📡 1. Live AI Tactical Recon Briefing",
                desc = "Tap 'AI TACTICAL INTEL' in the Raid Arena. The configured LLM analyzes the rival syndicate layout, identifies defensive vulnerabilities, and produces real-time tactical advice and boss syndicate dialogue."
            )

            RuleItem(
                title = "🔍 2. AI Base Defense Security Audit",
                desc = "In the 'Base' builder, tap 'AI SECURITY AUDIT'. The LLM reviews your 8×8 wall layout, checks chokepoints, rates your fortress, and provides tactical recommendations to stop infiltrators."
            )

            RuleItem(
                title = "🌐 3. Neural Procedural Sector Generation",
                desc = "The LLM synthesizes custom procedural syndicate sectors using your real elevation, steps, and selected difficulty theme (Cyberpunk, Iron Bastion, Quantum Spire)."
            )

            RuleItem(
                title = "🔑 4. How to Configure Your Key",
                desc = "Go to the 'Settings' tab. Select your provider (e.g. GEMINI or GROQ), paste your API key, and tap 'TEST CONNECTION'. If no key is set, high-performance local neural procedural engines run automatically!"
            )
        }
    }
}

@Composable
private fun RuleItem(title: String, desc: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(
            text = title,
            color = CyberCyanAccent,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = desc,
            color = TextSecondaryDark,
            fontSize = 11.sp,
            lineHeight = 15.sp
        )
    }
}
