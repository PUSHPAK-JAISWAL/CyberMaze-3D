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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
    onClearMovementLogs: () -> Unit
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
            .padding(16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CyberSectionHeader(
            title = "CYBER CORE SETTINGS",
            subtitle = "Bring Your Own Key (BYOK) architecture & sensor tuning",
            badgeText = "SQLITE CACHE"
        )

        // API Provider Selector Card
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = CyberMintPrimary.copy(alpha = 0.4f)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = CyberMintLight, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI PROVIDER (BRING YOUR OWN KEY)",
                        color = CyberMintLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Provider selection chips
                val providers = listOf("GROQ", "OPENROUTER", "OPENAI", "GEMINI", "CUSTOM")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    providers.forEach { prov ->
                        val isSelected = selectedProvider.equals(prov, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) CyberMintPrimary else Color(0xFF0C241B))
                                .border(1.dp, if (isSelected) CyberMintLight else CyberCardBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedProvider = prov
                                    // Update default model placeholder
                                    when (prov) {
                                        "GROQ" -> modelIdText = "llama-3.3-70b-versatile"
                                        "OPENROUTER" -> modelIdText = "deepseek/deepseek-chat"
                                        "OPENAI" -> modelIdText = "gpt-4o-mini"
                                        "GEMINI" -> modelIdText = "gemini-1.5-flash"
                                        else -> Unit
                                    }
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = prov,
                                color = if (isSelected) Color(0xFF003822) else TextSecondaryDark,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // API Key input
                OutlinedTextField(
                    value = apiKeyText,
                    onValueChange = { apiKeyText = it },
                    label = { Text("API Key ($selectedProvider)") },
                    placeholder = { Text("gsk_... or sk-or-... or AIza...") },
                    visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                            Icon(
                                imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle Visibility",
                                tint = CyberMintLight
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

                Spacer(modifier = Modifier.height(10.dp))

                // Model ID input
                OutlinedTextField(
                    value = modelIdText,
                    onValueChange = { modelIdText = it },
                    label = { Text("Model Identifier (User Configurable)") },
                    placeholder = { Text("e.g. llama-3.3-70b-versatile, claude-3-5-sonnet") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberMintPrimary,
                        unfocusedBorderColor = CyberCardBorder,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_model_id")
                )

                // Quick model presets chips
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "POPULAR PRESETS FOR $selectedProvider:",
                    color = TextSecondaryDark,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))

                val presets = when (selectedProvider.uppercase()) {
                    "GROQ" -> listOf("llama-3.3-70b-versatile", "mixtral-8x7b-32768", "llama-3.1-8b-instant")
                    "OPENROUTER" -> listOf("deepseek/deepseek-chat", "meta-llama/llama-3.3-70b-instruct", "anthropic/claude-3.5-sonnet")
                    "OPENAI" -> listOf("gpt-4o-mini", "gpt-4o", "gpt-3.5-turbo")
                    "GEMINI" -> listOf("gemini-1.5-flash", "gemini-1.5-pro", "gemini-2.0-flash")
                    else -> listOf("custom-model-v1", "local-ollama")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { preset ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF071912))
                                .border(1.dp, Color(0xFF1E4233), RoundedCornerShape(8.dp))
                                .clickable { modelIdText = preset }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = preset.substringAfterLast('/'),
                                color = CyberCyanAccent,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                if (selectedProvider.uppercase() == "CUSTOM") {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customBaseUrlText,
                        onValueChange = { customBaseUrlText = it },
                        label = { Text("Base URL Endpoint") },
                        placeholder = { Text("https://your-proxy.com/v1") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberMintPrimary,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_custom_base_url")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Test Connection Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            onTestApiConnection(selectedProvider, apiKeyText, modelIdText, customBaseUrlText)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF133B2C)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("btn_test_connection")
                    ) {
                        Icon(imageVector = Icons.Default.NetworkCheck, contentDescription = null, tint = CyberMintLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "TEST CONNECTION", color = CyberMintLight, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }

                    CyberPillButton(
                        text = "SAVE CONFIG",
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
                        testTag = "btn_save_settings"
                    )
                }

                // API Test Result Display
                if (apiTestResult != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val isSuccess = apiTestResult.startsWith("Success")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSuccess) Color(0xFF0F3829) else Color(0xFF280B12))
                            .border(1.dp, if (isSuccess) CyberMintPrimary else CyberLaserRed, RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (isSuccess) CyberMintLight else CyberLaserRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = apiTestResult,
                                color = if (isSuccess) CyberMintLight else Color.White,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(1f)
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

        // Gameplay & Enemy AI Preferences Card
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFF0C241B)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = CyberMintLight, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GAMEPLAY & DYNAMIC ENEMY AI",
                        color = CyberMintLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Dynamic AI Enemy Behaviors Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Dynamic AI Enemy Responses", color = TextPrimaryDark, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Enemies adapt tactics in real-time using selected LLM model", color = TextSecondaryDark, fontSize = 11.sp)
                    }
                    Switch(
                        checked = dynamicEnemyAi,
                        onCheckedChange = { dynamicEnemyAi = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberMintLight, checkedTrackColor = CyberMintDark),
                        modifier = Modifier.testTag("switch_dynamic_enemy_ai")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Haptics Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Cyber Haptic Feedback", color = TextPrimaryDark, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Tactile feedback for core collection and drone ambushes", color = TextSecondaryDark, fontSize = 11.sp)
                    }
                    Switch(
                        checked = hapticsEnabled,
                        onCheckedChange = { hapticsEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberMintLight, checkedTrackColor = CyberMintDark),
                        modifier = Modifier.testTag("switch_haptics")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sensor Sensitivity Slider
                Text(
                    text = "Sensor Elevation Sensitivity: ${String.format("%.1f", sensitivity)}x",
                    color = TextPrimaryDark,
                    fontSize = 12.sp,
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

        // Room SQLite Cache Info Card
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFF091E16)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Storage, contentDescription = null, tint = CyberCyanAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ROOM SQLITE OFFLINE DATABASE",
                        color = CyberCyanAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Local Storage Status: $cachedLevelsCount levels cached • SQLite Room active",
                    color = TextSecondaryDark,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onClearMovementLogs,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F1215)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_clear_movement_logs")
                ) {
                    Text(text = "CLEAR MOVEMENT TELEMETRY LOGS", color = CyberLaserRed, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}
