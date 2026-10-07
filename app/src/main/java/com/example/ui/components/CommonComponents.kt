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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberMintContainer
import com.example.ui.theme.CyberMintLight
import com.example.ui.theme.CyberMintOnContainer
import com.example.ui.theme.CyberMintPrimary
import com.example.ui.theme.CyberSurfaceCard
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = CyberSurfaceCard,
    borderColor: Color = CyberCardBorder,
    cornerRadius: Dp = 16.dp,
    contentPadding: Dp = 14.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(cornerRadius))
            .padding(contentPadding)
    ) {
        content()
    }
}

@Composable
fun CyberPillButton(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String = "cyber_pill_button",
    isPrimary: Boolean = true,
    horizontalPadding: Dp = 10.dp,
    verticalPadding: Dp = 8.dp,
    fontSize: TextUnit = 11.sp
) {
    val bgBrush = if (isPrimary) {
        Brush.horizontalGradient(listOf(CyberMintPrimary, CyberMintLight))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFF133B2C), Color(0xFF133B2C)))
    }
    val contentColor = if (isPrimary) Color(0xFF003822) else CyberMintLight
    val borderColor = if (isPrimary) CyberMintLight else CyberCardBorder

    Box(
        modifier = modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(20.dp))
            .background(if (enabled) bgBrush else Brush.linearGradient(listOf(Color(0xFF1E2F28), Color(0xFF1E2F28))))
            .border(1.dp, if (enabled) borderColor else Color.Transparent, RoundedCornerShape(20.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) contentColor else TextMutedDark,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                color = if (enabled) contentColor else TextMutedDark,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CyberMetricBadge(
    title: String,
    value: String,
    unit: String = "",
    icon: ImageVector,
    accentColor: Color = CyberMintPrimary,
    modifier: Modifier = Modifier
) {
    CyberCard(
        modifier = modifier,
        backgroundColor = Color(0xFF0C241B),
        borderColor = accentColor.copy(alpha = 0.35f),
        cornerRadius = 12.dp,
        contentPadding = 8.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title.uppercase(),
                    color = TextSecondaryDark,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = value,
                    color = TextPrimaryDark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                if (unit.isNotBlank()) {
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = unit,
                        color = accentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun CyberSectionHeader(
    title: String,
    subtitle: String? = null,
    badgeText: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(
                text = title,
                color = TextPrimaryDark,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = TextSecondaryDark,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
        }
        if (badgeText != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(CyberMintContainer)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = badgeText,
                    color = CyberMintOnContainer,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
