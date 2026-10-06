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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
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
import com.example.data.model.CardItem
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberPillButton
import com.example.ui.components.CyberSectionHeader
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
fun TroopsDeckScreen(
    cards: List<CardItem>,
    userBits: Int,
    userNanites: Int,
    trophies: Int,
    onUpgradeCard: (String) -> Unit,
    onStartRaid: () -> Unit
) {
    val leagueName = when {
        trophies >= 800 -> "Apex Master League"
        trophies >= 400 -> "Gold Syndicate League"
        trophies >= 150 -> "Silver Cyber League"
        else -> "Bronze Hacker League"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackgroundDark)
            .padding(12.dp)
            .testTag("troops_deck_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Vault Header & Trophies
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = CyberAmberWarning,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = leagueName,
                                color = CyberAmberWarning,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "$trophies TROPHIES EARNED",
                            color = TextSecondaryDark,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Currency Badges
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = CyberMintPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "$userBits",
                                color = CyberMintLight,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Diamond,
                                contentDescription = null,
                                tint = CyberCyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "$userNanites",
                                color = CyberCyanAccent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // 2. Battle Launch Shortcut
        item {
            Button(
                onClick = onStartRaid,
                colors = ButtonDefaults.buttonColors(containerColor = CyberMintPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(46.dp)
            ) {
                Text(
                    text = "⚔️ LAUNCH SYNDICATE RAID",
                    color = Color(0xFF003822),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // 3. Section Title
        item {
            CyberSectionHeader(
                title = "TACTICAL BATTLE DECK",
                subtitle = "Upgrade troops and defenses using Neon Bits earned from raids & outdoor walking",
                badgeText = "${cards.size} CARDS ACTIVE"
            )
        }

        // 4. Cards List
        items(cards) { card ->
            val canUpgrade = userBits >= card.upgradeCostBits
            val typeTitle = card.troopType?.title ?: card.defenseType?.title ?: card.title
            val desc = card.troopType?.description ?: card.defenseType?.description ?: ""
            val elixirCost = card.troopType?.elixirCost ?: card.defenseType?.costBits ?: 0

            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xFF071913),
                borderColor = if (canUpgrade) CyberMintPrimary.copy(alpha = 0.6f) else CyberCardBorder,
                contentPadding = 12.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Level Badge
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (card.isTroop) Color(0xFF0C3829) else Color(0xFF1B2E3D))
                                .border(1.5.dp, if (card.isTroop) CyberMintPrimary else CyberCyanAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "L${card.level}",
                                color = TextPrimaryDark,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = typeTitle,
                                    color = TextPrimaryDark,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (card.isTroop) "${elixirCost}⚡" else "DEFENSE",
                                    color = CyberCyanAccent,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = desc,
                                color = TextSecondaryDark,
                                fontSize = 11.sp,
                                maxLines = 2
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Upgrade Button
                    Button(
                        onClick = { onUpgradeCard(card.id) },
                        enabled = canUpgrade,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberMintPrimary,
                            disabledContainerColor = Color(0xFF15261F)
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Upgrade,
                                contentDescription = null,
                                tint = if (canUpgrade) Color(0xFF003822) else TextMutedDark,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${card.upgradeCostBits}⚡",
                                color = if (canUpgrade) Color(0xFF003822) else TextMutedDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
