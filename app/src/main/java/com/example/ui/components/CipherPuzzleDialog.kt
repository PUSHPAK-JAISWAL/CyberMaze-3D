package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.TerminalData
import com.example.ui.theme.CyberAmberWarning
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyanAccent
import com.example.ui.theme.CyberMintLight
import com.example.ui.theme.CyberMintPrimary
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun CipherPuzzleDialog(
    terminal: TerminalData,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    val targets = terminal.cipherTargets
    val currentDials = remember {
        mutableStateListOf(0, 0, 0)
    }

    val isSolved = currentDials[0] == targets[0] &&
            currentDials[1] == targets[1] &&
            currentDials[2] == targets[2]

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("cipher_dialog_root"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C241B)),
            border = androidx.compose.foundation.BorderStroke(2.dp, if (isSolved) CyberMintPrimary else CyberAmberWarning)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header
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
                                .background(CyberAmberWarning.copy(alpha = 0.2f))
                                .border(1.dp, CyberAmberWarning, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Terminal, contentDescription = null, tint = CyberAmberWarning, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "HACK CIPHER LOCK",
                            color = CyberMintLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondaryDark)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Align all 3 frequency harmonic dials to match target sequence [${targets.joinToString(" - ")}]",
                    color = TextSecondaryDark,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3 Cyber Harmonic Dials
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (index in 0..2) {
                        CipherDialColumn(
                            dialValue = currentDials[index],
                            targetValue = targets[index],
                            onIncrement = {
                                currentDials[index] = (currentDials[index] + 1) % 6
                            },
                            onDecrement = {
                                currentDials[index] = if (currentDials[index] == 0) 5 else currentDials[index] - 1
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onSuccess,
                    enabled = isSolved,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberMintPrimary,
                        disabledContainerColor = Color(0xFF143327)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("btn_solve_cipher")
                ) {
                    Icon(imageVector = if (isSolved) Icons.Default.LockOpen else Icons.Default.Terminal, contentDescription = null, tint = if (isSolved) Color(0xFF003822) else TextSecondaryDark, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSolved) "OVERRIDE MAINFRAME" else "ALIGN CIPHERS TO UNLOCK",
                        color = if (isSolved) Color(0xFF003822) else TextSecondaryDark,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CipherDialColumn(
    dialValue: Int,
    targetValue: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val isMatched = dialValue == targetValue

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(
            onClick = onIncrement,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up", tint = CyberMintLight)
        }

        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isMatched) Color(0xFF0F3829) else Color(0xFF071912))
                .border(
                    2.dp,
                    if (isMatched) CyberMintPrimary else CyberCardBorder,
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$dialValue",
                color = if (isMatched) CyberMintLight else Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        IconButton(
            onClick = onDecrement,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down", tint = CyberMintLight)
        }
    }
}
