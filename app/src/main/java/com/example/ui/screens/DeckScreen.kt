package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BattleCard
import com.example.data.model.BattleCardType
import com.example.data.model.CyberChest
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberSectionHeader
import com.example.ui.theme.CyberAmberWarning
import com.example.ui.theme.CyberBackgroundDark
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyanAccent
import com.example.ui.theme.CyberLaserRed
import com.example.ui.theme.CyberMintDark
import com.example.ui.theme.CyberMintLight
import com.example.ui.theme.CyberMintPrimary
import com.example.ui.theme.CyberPurpleNeon
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun DeckScreen(
    activeDeck: List<BattleCard>,
    chests: List<CyberChest>,
    userBits: Int,
    userCrystals: Int,
    onUpgradeCard: (BattleCardType) -> Unit,
    onOpenChest: (Long) -> Unit,
    onStartRun: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackgroundDark)
            .padding(14.dp)
            .testTag("deck_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Outpost Vault Header (Bits & Crystals)
        item {
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xFF0C241B),
                borderColor = CyberMintPrimary.copy(alpha = 0.5f),
                contentPadding = 12.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CYBER OUTPOST VAULT",
                            color = CyberMintLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Upgrade battle cards & open outdoor walk chests",
                            color = TextSecondaryDark,
                            fontSize = 11.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Bits
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Stars, null, tint = CyberAmberWarning, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$userBits",
                                color = CyberAmberWarning,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Crystals
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Diamond, null, tint = CyberCyanAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$userCrystals",
                                color = CyberCyanAccent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // 2. Tactical Battle Cards Section Header
        item {
            CyberSectionHeader(
                title = "TACTICAL BATTLE DECK",
                subtitle = "Deploy during 3D runner sprints using Energy",
                badgeText = "${activeDeck.size} CARDS"
            )
        }

        // 3. Battle Cards List
        items(activeDeck, key = { it.id }) { card ->
            BattleCardRowItem(
                card = card,
                userBits = userBits,
                onUpgrade = { onUpgradeCard(card.type) }
            )
        }

        // 4. Outdoor Walk Chests Section
        item {
            Spacer(modifier = Modifier.height(4.dp))
            CyberSectionHeader(
                title = "EXPEDITION CHESTS",
                subtitle = "Walk outdoors or climb elevation to unlock loot!",
                badgeText = "STEP POWERED"
            )
        }

        // 5. Chests List
        items(chests, key = { it.id }) { chest ->
            ExpeditionChestCard(
                chest = chest,
                onOpen = { onOpenChest(chest.id) }
            )
        }

        // 6. Launch Runner CTA Button
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onStartRun,
                colors = ButtonDefaults.buttonColors(containerColor = CyberMintPrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_deck_start_run")
            ) {
                Icon(Icons.Default.RocketLaunch, null, tint = Color(0xFF003822), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "DEPLOY INTO RUNNER ARENA",
                    color = Color(0xFF003822),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun BattleCardRowItem(
    card: BattleCard,
    userBits: Int,
    onUpgrade: () -> Unit
) {
    val icon = when (card.type) {
        BattleCardType.EMP_BLAST -> Icons.Default.FlashOn
        BattleCardType.WARP_DRIVE -> Icons.Default.RocketLaunch
        BattleCardType.SHIELD_MATRIX -> Icons.Default.Shield
        BattleCardType.CHRONO_SLOW -> Icons.Default.Timer
        BattleCardType.DRONE_STRIKE -> Icons.Default.Navigation
    }
    val accentColor = when (card.type) {
        BattleCardType.EMP_BLAST -> CyberLaserRed
        BattleCardType.WARP_DRIVE -> CyberPurpleNeon
        BattleCardType.SHIELD_MATRIX -> CyberMintPrimary
        BattleCardType.CHRONO_SLOW -> CyberCyanAccent
        BattleCardType.DRONE_STRIKE -> CyberAmberWarning
    }

    val canAfford = userBits >= card.upgradeBitsCost

    CyberCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = accentColor.copy(alpha = 0.4f),
        contentPadding = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Card Icon Badge
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Card Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = card.name,
                        color = TextPrimaryDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0F3526))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LV ${card.level}",
                            color = CyberMintLight,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Text(
                    text = card.description,
                    color = TextSecondaryDark,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Bolt, null, tint = CyberCyanAccent, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Cost: ${card.energyCost} NRG • Cooldown: ${card.cooldownSeconds.toInt()}s",
                        color = CyberCyanAccent,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Upgrade Button
            Button(
                onClick = onUpgrade,
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canAfford) CyberMintPrimary else Color(0xFF1E2F28)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(38.dp).testTag("btn_upgrade_${card.type.name}")
            ) {
                Icon(Icons.Default.Upgrade, null, tint = if (canAfford) Color(0xFF003822) else TextMutedDark, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${card.upgradeBitsCost} Bits",
                    color = if (canAfford) Color(0xFF003822) else TextMutedDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun ExpeditionChestCard(
    chest: CyberChest,
    onOpen: () -> Unit
) {
    val isReady = chest.isUnlocked && !chest.isOpened
    val isClaimed = chest.isOpened

    CyberCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = if (isReady) Color(0xFF0E3827) else Color(0xFF081C15),
        borderColor = if (isReady) CyberMintPrimary else CyberCardBorder,
        contentPadding = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isReady) CyberMintPrimary.copy(alpha = 0.2f) else Color(0xFF132B22)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isReady || isClaimed) Icons.Default.LockOpen else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isReady) CyberMintLight else if (isClaimed) Color.Gray else CyberAmberWarning,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chest.name,
                    color = if (isClaimed) Color.Gray else TextPrimaryDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (isClaimed) "Claimed! +${chest.bitsReward} Bits awarded"
                    else "Requires ${chest.stepsRequired} steps outdoors • Reward: ${chest.bitsReward} Bits",
                    color = TextSecondaryDark,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isReady) {
                Button(
                    onClick = onOpen,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberMintPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(36.dp).testTag("btn_open_chest_${chest.id}")
                ) {
                    Text("OPEN", color = Color(0xFF003822), fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            } else if (isClaimed) {
                Text("OPENED", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            } else {
                Text("LOCKED", color = CyberAmberWarning, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}
