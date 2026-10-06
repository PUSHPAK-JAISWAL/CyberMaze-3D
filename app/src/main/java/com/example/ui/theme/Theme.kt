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

private val CheerfulCyberColorScheme = lightColorScheme(
    primary = CyberMintPrimary,
    onPrimary = Color(0xFF003822),
    primaryContainer = CyberMintContainer,
    onPrimaryContainer = CyberMintOnContainer,
    secondary = CyberCyanAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD0F4FC),
    onSecondaryContainer = Color(0xFF002F38),
    tertiary = CyberAmberWarning,
    onTertiary = Color(0xFF452B00),
    background = CyberBackgroundBright,
    onBackground = TextPrimaryBright,
    surface = CyberSurfaceBright,
    onSurface = TextPrimaryBright,
    surfaceVariant = CyberSurfaceVariantBright,
    onSurfaceVariant = TextSecondaryBright,
    outline = CyberCardBorderBright,
    error = CyberLaserRed,
    onError = Color.White
)

private val LuminousDarkColorScheme = darkColorScheme(
    primary = CyberMintLight,
    onPrimary = Color(0xFF003822),
    primaryContainer = CyberSurfaceCard,
    onPrimaryContainer = CyberMintLight,
    secondary = CyberCyanAccent,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFF16473D),
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

@Composable
fun CyberMazeTheme(
    darkTheme: Boolean = false, // Default to bright and uplifting theme per user's preference!
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> LuminousDarkColorScheme
        else -> CheerfulCyberColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
