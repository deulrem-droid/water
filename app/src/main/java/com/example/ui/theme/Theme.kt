package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val IvorySeniorColorScheme =
  lightColorScheme(
    primary = MedDoneGreen,
    onPrimary = PureWhite,
    primaryContainer = MedDoneBadgeBg,
    onPrimaryContainer = MedDoneBadgeText,
    secondary = WaterPrimaryBlue,
    onSecondary = PureWhite,
    secondaryContainer = WaterBadgeBg,
    onSecondaryContainer = WaterPrimaryBlue,
    background = IvoryBackground,
    onBackground = WarmInk,
    surface = IvoryCardSurface,
    onSurface = WarmInk,
    surfaceVariant = IvoryHeaderSurface,
    onSurfaceVariant = WarmInkSecondary,
    outline = IvoryCardBorder,
  )

val SeniorShapes =
  Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
  )

@Composable
fun SeniorHealthTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = IvorySeniorColorScheme,
    typography = AppTypography,
    shapes = SeniorShapes,
    content = content,
  )
}
