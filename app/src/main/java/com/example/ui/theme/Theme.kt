package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val CyberDarkColorScheme = darkColorScheme(
    primary = CyberMintPrimary,
    onPrimary = Color(0xFF003822),
    primaryContainer = CyberSurfaceCard,
    onPrimaryContainer = CyberMintLight,
    secondary = CyberCyanAccent,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFF0D3D37),
    onSecondaryContainer = Color(0xFFA1F5FF),
    tertiary = CyberAmberWarning,
    onTertiary = Color(0xFF452B00),
    background = CyberBackgroundDark,
    onBackground = TextPrimaryDark,
    surface = CyberSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = CyberSurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = CyberCardBorder,
    error = CyberLaserRed,
    onError = Color.White
)

private val CyberLightColorScheme = lightColorScheme(
    primary = CyberMintDark,
    onPrimary = Color.White,
    primaryContainer = CyberMintContainer,
    onPrimaryContainer = CyberMintOnContainer,
    secondary = Color(0xFF007A87),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC7F7FF),
    onSecondaryContainer = Color(0xFF001F24),
    tertiary = Color(0xFF8A5800),
    onTertiary = Color.White,
    background = Color(0xFFF2FBF7),
    onBackground = Color(0xFF0A1C16),
    surface = Color.White,
    onSurface = Color(0xFF0A1C16),
    surfaceVariant = Color(0xFFE0F5EC),
    onSurfaceVariant = Color(0xFF264C3E),
    outline = Color(0xFF91D4BA),
    error = CyberLaserRed,
    onError = Color.White
)

@Composable
fun CyberMazeTheme(
    darkTheme: Boolean = true, // Default to cyber dark theme for immersive cyberpunk gameplay
    dynamicColor: Boolean = false, // Keep distinctive cyber emerald branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> CyberDarkColorScheme
        else -> CyberLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
