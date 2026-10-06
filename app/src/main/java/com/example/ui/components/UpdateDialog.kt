package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.window.Dialog
import com.example.update.UpdateInfo
import com.example.update.UpdateState
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyanAccent
import com.example.ui.theme.CyberLaserRed
import com.example.ui.theme.CyberMintLight
import com.example.ui.theme.CyberMintPrimary
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import java.io.File

@Composable
fun UpdateDialog(
    updateState: UpdateState,
    onStartDownload: (UpdateInfo) -> Unit,
    onInstallNow: (File) -> Unit,
    onSaveToDownloads: (File) -> Unit,
    onDismiss: () -> Unit
) {
    if (updateState is UpdateState.Idle) return

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("update_dialog_root"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2E23)),
            border = androidx.compose.foundation.BorderStroke(2.dp, CyberMintPrimary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with close icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CyberMintPrimary.copy(alpha = 0.2f))
                                .border(1.dp, CyberMintPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = CyberMintLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "IN-APP UPDATE",
                            color = CyberMintLight,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp).testTag("btn_close_update_dialog")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondaryDark)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (updateState) {
                    is UpdateState.Checking -> {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            androidx.compose.material3.CircularProgressIndicator(
                                color = CyberMintPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Checking GitHub Releases API...",
                                color = TextSecondaryDark,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    is UpdateState.UpToDate -> {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = CyberMintPrimary, modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "APPLICATION UP TO DATE", color = TextPrimaryDark, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text(text = "You are currently running version ${updateState.checkedVersion}", color = TextSecondaryDark, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(containerColor = CyberMintPrimary),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("DONE", color = Color(0xFF003822), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }

                    is UpdateState.Available -> {
                        val info = updateState.info
                        Text(
                            text = "New Release Available: ${info.versionName}",
                            color = TextPrimaryDark,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Size: ${String.format("%.1f", if (info.apkSize > 0) info.apkSize / (1024f * 1024f) else 15f)} MB • Direct GitHub CI/CD APK",
                            color = CyberCyanAccent,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Release notes box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 140.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF081F17))
                                .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = info.releaseNotes,
                                color = TextSecondaryDark,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("LATER", color = TextSecondaryDark, fontFamily = FontFamily.Monospace)
                            }

                            Button(
                                onClick = { onStartDownload(info) },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberMintPrimary),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1.5f).testTag("btn_download_update")
                            ) {
                                Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = Color(0xFF003822), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("UPDATE NOW", color = Color(0xFF003822), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }

                    is UpdateState.Downloading -> {
                        Text(
                            text = "Downloading Update: ${updateState.info.versionName}",
                            color = TextPrimaryDark,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { updateState.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = CyberMintPrimary,
                            trackColor = Color(0xFF133B2C)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${String.format("%.1f", updateState.downloadedMb)} / ${String.format("%.1f", updateState.totalMb)} MB (${(updateState.progress * 100).toInt()}%)",
                                color = CyberMintLight,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "${String.format("%.1f", updateState.speedMbs)} MB/s",
                                color = CyberCyanAccent,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("CANCEL", color = CyberLaserRed, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    is UpdateState.ReadyToInstall -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = CyberMintPrimary, modifier = Modifier.size(42.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "DOWNLOAD COMPLETE", color = TextPrimaryDark, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text(text = "Ready to launch package installer for ${updateState.info.versionName}", color = TextSecondaryDark, fontSize = 12.sp)

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { onInstallNow(updateState.apkFile) },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberMintPrimary),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().testTag("btn_install_package_now")
                            ) {
                                Text("INSTALL UPDATE NOW", color = Color(0xFF003822), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }

                    is UpdateState.Conflict -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = CyberLaserRed, modifier = Modifier.size(42.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "SIGNATURE CONFLICT", color = CyberLaserRed, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text(
                                text = updateState.reason + "\n\nYou can save the updated APK file to your Downloads folder to install cleanly.",
                                color = TextSecondaryDark,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { onSaveToDownloads(updateState.apkFile) },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberCyanAccent),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().testTag("btn_save_conflict_downloads")
                            ) {
                                Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, tint = Color(0xFF003038))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("SAVE TO DOWNLOADS FOLDER", color = Color(0xFF003038), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }

                    is UpdateState.Error -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = CyberLaserRed, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "UPDATE CHECK FAILED", color = CyberLaserRed, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text(text = updateState.message, color = TextSecondaryDark, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                                Text("DISMISS")
                            }
                        }
                    }

                    UpdateState.Idle -> Unit
                }
            }
        }
    }
}
