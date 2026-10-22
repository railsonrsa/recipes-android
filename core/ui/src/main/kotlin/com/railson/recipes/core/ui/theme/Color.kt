@file:Suppress("MagicNumber") // Color values are DESIGN.md tokens.

package com.railson.recipes.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val Orange = Color(0xFFFE6F0C)
private val Ink = Color(0xFF1B1710)
private val Black = Color(0xFF000000)
private val Outline = Color(0xFF8A8379)

internal val LightColors = lightColorScheme(
    background = Color(0xFFF7F4EE),
    surface = Color(0xFFF7F4EE),
    onBackground = Ink,
    onSurface = Ink,
    onSurfaceVariant = Color(0xFF625B53),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFF9F4E8),
    outline = Outline,
    outlineVariant = Color(0xFFDAD5CE),
    primary = Orange,
    onPrimary = Ink,
    primaryContainer = Color(0xFFFEE3CE),
    onPrimaryContainer = Orange,
    secondaryContainer = Color(0xFFB8D4FF),
    onSecondaryContainer = Ink,
    tertiary = Color(0xFF256DCB),
    scrim = Black,
)

internal val DarkColors = darkColorScheme(
    background = Color(0xFF17140F),
    surface = Color(0xFF17140F),
    onBackground = Color(0xFFF3EEE6),
    onSurface = Color(0xFFF3EEE6),
    onSurfaceVariant = Color(0xFFB9B0A4),
    surfaceContainerLowest = Color(0xFF221E18),
    surfaceContainer = Color(0xFF2E2922),
    outline = Outline,
    outlineVariant = Color(0xFF4A443C),
    primary = Orange,
    onPrimary = Ink,
    primaryContainer = Color(0xFF553621),
    onPrimaryContainer = Orange,
    secondaryContainer = Color(0xFF23395A),
    onSecondaryContainer = Color(0xFFF3EEE6),
    tertiary = Color(0xFF8FB8F2),
    scrim = Black,
)
