package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.GameSettingsEntity
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberPillButton
import com.example.ui.components.CyberSectionHeader
import com.example.ui.theme.CyberAmberWarning
import com.example.ui.theme.CyberBackgroundDark
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyanAccent
import com.example.ui.theme.CyberLaserRed
import com.example.ui.theme.CyberMintDark
import com.example.ui.theme.CyberMintLight
import com.example.ui.theme.CyberMintPrimary
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun SettingsScreen(
    settings: GameSettingsEntity,
    apiTestResult: String?,
    cachedLevelsCount: Int,
    onSaveSettings: (GameSettingsEntity) -> Unit,
    onTestApiConnection: (provider: String, key: String, model: String, baseUrl: String) -> Unit,
    onClearTestResult: () -> Unit,
    onClearMovementLogs: () -> Unit,
    onCheckForUpdates: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    var selectedProvider by remember(settings) { mutableStateOf(settings.apiProvider) }
    var apiKeyText by remember(settings) { mutableStateOf(settings.apiKey) }
    var isKeyVisible by remember { mutableStateOf(false) }
    var modelIdText by remember(settings) { mutableStateOf(settings.modelId) }
    var customBaseUrlText by remember(settings) { mutableStateOf(settings.customBaseUrl) }
    var hapticsEnabled by remember(settings) { mutableStateOf(settings.hapticsEnabled) }
    var dynamicEnemyAi by remember(settings) { mutableStateOf(settings.dynamicAiEnemyEnabled) }
    var sensitivity by remember(settings) { mutableFloatStateOf(settings.sensorSensitivity) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackgroundDark)
            .verticalScroll(scrollState)
            .padding(14.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CyberSectionHeader(
            title = "CYBER CORE SETTINGS",
            subtitle = "BYOK AI model configuration & game preferences",
            badgeText = "SQLITE CACHE"
        )

        // API Provider Card
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = CyberMintPrimary.copy(alpha = 0.4f),
            contentPadding = 14.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = CyberMintLight, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AI PROVIDER (BRING YOUR OWN KEY)",
                        color = CyberMintLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Row 1 Providers: GROQ, OPENROUTER, OPENAI
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("GROQ", "OPENROUTER", "OPENAI").forEach { prov ->
                        val isSelected = selectedProvider.equals(prov, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) CyberMintPrimary else Color(0xFF0C241B))
                                .border(1.dp, if (isSelected) CyberMintLight else CyberCardBorder, RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedProvider = prov
                                    when (prov) {
                                        "GROQ" -> modelIdText = "llama-3.3-70b-versatile"
                                        "OPENROUTER" -> modelIdText = "deepseek/deepseek-chat"
                                        "OPENAI" -> modelIdText = "gpt-4o-mini"
                                    }
                                }
                                .padding(vertical = 9.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = prov,
                                color = if (isSelected) Color(0xFF003822) else TextSecondaryDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                softWrap = false,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Row 2 Providers: GEMINI, CUSTOM
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("GEMINI", "CUSTOM").forEach { prov ->
                        val isSelected = selectedProvider.equals(prov, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) CyberMintPrimary else Color(0xFF0C241B))
                                .border(1.dp, if (isSelected) CyberMintLight else CyberCardBorder, RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedProvider = prov
                                    if (prov == "GEMINI") modelIdText = "gemini-1.5-flash"
                                }
                                .padding(vertical = 9.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = prov,
                                color = if (isSelected) Color(0xFF003822) else TextSecondaryDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                softWrap = false,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // API Key Textfield
                OutlinedTextField(
                    value = apiKeyText,
                    onValueChange = { apiKeyText = it },
                    label = { Text("API Key ($selectedProvider)", fontSize = 12.sp) },
                    placeholder = { Text("Enter key or leave empty for local synthesizer", fontSize = 11.sp) },
                    visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                            Icon(
                                imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle Visibility",
                                tint = CyberMintLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberMintPrimary,
                        unfocusedBorderColor = CyberCardBorder,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_api_key")
                )

                // Model Identifier Textfield
                OutlinedTextField(
                    value = modelIdText,
                    onValueChange = { modelIdText = it },
                    label = { Text("Model Identifier (Any LLM)", fontSize = 12.sp) },
                    placeholder = { Text("e.g. llama-3.3-70b-versatile", fontSize = 11.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberMintPrimary,
                        unfocusedBorderColor = CyberCardBorder,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_model_id")
                )

                // Quick Presets with Comfortable Horizontal Scrolling (no cramped truncation)
                Text(
                    text = "PRESET SUGGESTIONS:",
                    color = TextSecondaryDark,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )

                val presets = when (selectedProvider.uppercase()) {
                    "GROQ" -> listOf("llama-3.3-70b-versatile", "mixtral-8x7b-32768", "llama-3.1-8b-instant")
                    "OPENROUTER" -> listOf("deepseek/deepseek-chat", "meta-llama/llama-3.3-70b", "anthropic/claude-3.5-sonnet")
                    "OPENAI" -> listOf("gpt-4o-mini", "gpt-4o", "gpt-3.5-turbo")
                    "GEMINI" -> listOf("gemini-1.5-flash", "gemini-1.5-pro", "gemini-2.0-flash")
                    else -> listOf("custom-model-v1", "local-ollama")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { preset ->
                        val isSelected = modelIdText == preset
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF133B2C) else Color(0xFF071912))
                                .border(1.dp, if (isSelected) CyberMintPrimary else Color(0xFF1E4233), RoundedCornerShape(8.dp))
                                .clickable { modelIdText = preset }
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = preset,
                                color = if (isSelected) CyberMintLight else CyberCyanAccent,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                if (selectedProvider.uppercase() == "CUSTOM") {
                    OutlinedTextField(
                        value = customBaseUrlText,
                        onValueChange = { customBaseUrlText = it },
                        label = { Text("Base URL Endpoint", fontSize = 12.sp) },
                        placeholder = { Text("https://your-proxy.com/v1", fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberMintPrimary,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_custom_base_url")
                    )
                }

                // Action Buttons: TEST LINK & SAVE CONFIG (Equally balanced, no cramped text)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CyberPillButton(
                        text = "TEST API",
                        icon = Icons.Default.NetworkCheck,
                        onClick = {
                            onTestApiConnection(selectedProvider, apiKeyText, modelIdText, customBaseUrlText)
                        },
                        isPrimary = false,
                        testTag = "btn_test_connection",
                        modifier = Modifier.weight(1f),
                        fontSize = 11.sp,
                        horizontalPadding = 8.dp,
                        verticalPadding = 10.dp
                    )

                    CyberPillButton(
                        text = "SAVE CONFIG",
                        icon = Icons.Default.CheckCircle,
                        onClick = {
                            onSaveSettings(
                                settings.copy(
                                    apiProvider = selectedProvider,
                                    apiKey = apiKeyText,
                                    modelId = modelIdText,
                                    customBaseUrl = customBaseUrlText,
                                    hapticsEnabled = hapticsEnabled,
                                    dynamicAiEnemyEnabled = dynamicEnemyAi,
                                    sensorSensitivity = sensitivity
                                )
                            )
                        },
                        isPrimary = true,
                        testTag = "btn_save_settings",
                        modifier = Modifier.weight(1f),
                        fontSize = 11.sp,
                        horizontalPadding = 8.dp,
                        verticalPadding = 10.dp
                    )
                }

                // API Test Result Banner
                if (apiTestResult != null) {
                    val isSuccess = apiTestResult.startsWith("Success")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSuccess) Color(0xFF0F3829) else Color(0xFF280B12))
                            .border(1.dp, if (isSuccess) CyberMintPrimary else CyberLaserRed, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (isSuccess) CyberMintLight else CyberLaserRed,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = apiTestResult,
                                color = if (isSuccess) CyberMintLight else Color.White,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(1f),
                                maxLines = 2
                            )
                            Text(
                                text = "✕",
                                color = TextMutedDark,
                                fontSize = 12.sp,
                                modifier = Modifier.clickable { onClearTestResult() }.padding(4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Gameplay Preferences Card
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFF0C241B),
            contentPadding = 14.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = CyberMintLight, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GAMEPLAY & TACTICAL SETTINGS",
                        color = CyberMintLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Dynamic AI Enemy Responses Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Dynamic Drone Tactics", color = TextPrimaryDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Hostile drones adapt tactics in real-time", color = TextSecondaryDark, fontSize = 10.sp)
                    }
                    Switch(
                        checked = dynamicEnemyAi,
                        onCheckedChange = { dynamicEnemyAi = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberMintLight, checkedTrackColor = CyberMintDark),
                        modifier = Modifier.testTag("switch_dynamic_enemy_ai")
                    )
                }

                // Haptic Feedback Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Tactile Haptics", color = TextPrimaryDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Vibrations for core pickup, EMP, and alerts", color = TextSecondaryDark, fontSize = 10.sp)
                    }
                    Switch(
                        checked = hapticsEnabled,
                        onCheckedChange = { hapticsEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberMintLight, checkedTrackColor = CyberMintDark),
                        modifier = Modifier.testTag("switch_haptics")
                    )
                }

                // Sensor Sensitivity Slider
                Text(
                    text = "Elevation Multiplier: ${String.format("%.1f", sensitivity)}x",
                    color = TextPrimaryDark,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Slider(
                    value = sensitivity,
                    onValueChange = { sensitivity = it },
                    valueRange = 0.5f..2.5f,
                    colors = SliderDefaults.colors(thumbColor = CyberMintLight, activeTrackColor = CyberMintPrimary),
                    modifier = Modifier.testTag("slider_sensitivity")
                )
            }
        }

        // Room SQLite Offline Cache Card
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFF091E16),
            contentPadding = 14.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Storage, contentDescription = null, tint = CyberCyanAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ROOM SQLITE OFFLINE DATABASE",
                        color = CyberCyanAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = "Cached 3D Sectors: $cachedLevelsCount • Room SQLite active",
                    color = TextSecondaryDark,
                    fontSize = 11.sp
                )

                CyberPillButton(
                    text = "CLEAR MOVEMENT LOGS",
                    icon = Icons.Default.DeleteSweep,
                    onClick = onClearMovementLogs,
                    isPrimary = false,
                    testTag = "btn_clear_movement_logs",
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 11.sp,
                    horizontalPadding = 12.dp,
                    verticalPadding = 10.dp
                )
            }
        }

        // GitHub Releases In-App Updates Card
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFF0C2B20),
            borderColor = CyberMintPrimary.copy(alpha = 0.5f),
            contentPadding = 14.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.SystemUpdate, contentDescription = null, tint = CyberMintLight, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GITHUB RELEASES AUTO-UPDATE",
                        color = CyberMintLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = "Source: github.com/PUSHPAK-JAISWAL/cybermaze-3d\nVersion: ${com.example.BuildConfig.VERSION_NAME} • Deterministic signing",
                    color = TextSecondaryDark,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )

                CyberPillButton(
                    text = "CHECK FOR UPDATES",
                    icon = Icons.Default.SystemUpdate,
                    onClick = onCheckForUpdates,
                    isPrimary = true,
                    modifier = Modifier.fillMaxWidth().testTag("btn_check_github_updates"),
                    fontSize = 11.sp,
                    horizontalPadding = 12.dp,
                    verticalPadding = 10.dp
                )
            }
        }
    }
}
