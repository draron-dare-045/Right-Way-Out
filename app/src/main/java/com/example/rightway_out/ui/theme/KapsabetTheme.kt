package com.example.rightway_out.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Brand colours ─────────────────────────────────────────
val Maroon900   = Color(0xFF1A0508)
val Maroon800   = Color(0xFF4A0E14)
val Maroon700   = Color(0xFF7B1E28)
val Maroon600   = Color(0xFF9B2535)
val MaroonLight = Color(0xFFFFDAD9)
val Gold        = Color(0xFFC9A84C)
val GoldDark    = Color(0xFFD4B46A)
val GoldLight   = Color(0xFFFFF0C2)
val Cream       = Color(0xFFFAF6F0)
val CreamDark   = Color(0xFFF0E8DC)
val Forest      = Color(0xFF2D5A27)
val ForestLight = Color(0xFFC8E6C4)
val White       = Color(0xFFFFFFFF)
val TextDark    = Color(0xFF1A1A1A)
val TextMid     = Color(0xFF555555)
val TextLight   = Color(0xFF888888)

// Dark theme surfaces
val DarkSurface   = Color(0xFF1E1E1E)
val DarkSurface2  = Color(0xFF2A2A2A)
val DarkSurface3  = Color(0xFF333333)
val DarkBg        = Color(0xFF121212)

private val LightColorScheme = lightColorScheme(
    primary            = Maroon700,
    onPrimary          = White,
    primaryContainer   = MaroonLight,
    onPrimaryContainer = Maroon900,
    secondary          = Forest,
    onSecondary        = White,
    secondaryContainer = ForestLight,
    onSecondaryContainer = Color(0xFF1B3D18),
    tertiary           = Gold,
    onTertiary         = White,
    tertiaryContainer  = GoldLight,
    onTertiaryContainer = Color(0xFF3D2E00),
    background         = Cream,
    onBackground       = TextDark,
    surface            = White,
    onSurface          = TextDark,
    surfaceVariant     = CreamDark,
    onSurfaceVariant   = TextMid,
    error              = Color(0xFFBA1A1A),
    onError            = White,
    errorContainer     = Color(0xFFFFDAD6),
    onErrorContainer   = Color(0xFF410002),
    outline            = Color(0xFFD4C5C5),
)

private val DarkColorScheme = darkColorScheme(
    primary            = Color(0xFFFFB3B0),
    onPrimary          = Color(0xFF68000E),
    primaryContainer   = Maroon700,
    onPrimaryContainer = MaroonLight,
    secondary          = Color(0xFF9DCB96),
    onSecondary        = Color(0xFF003A02),
    secondaryContainer = Color(0xFF1B5318),
    onSecondaryContainer = Color(0xFFB9F0B0),
    tertiary           = GoldDark,
    onTertiary         = Color(0xFF3D2E00),
    tertiaryContainer  = Color(0xFF574400),
    onTertiaryContainer = GoldLight,
    background         = DarkBg,
    onBackground       = Color(0xFFECE0E0),
    surface            = DarkSurface,
    onSurface          = Color(0xFFECE0E0),
    surfaceVariant     = DarkSurface2,
    onSurfaceVariant   = Color(0xFFD7C2C2),
    error              = Color(0xFFFFB4AB),
    onError            = Color(0xFF690005),
    outline            = Color(0xFF9F8D8D),
)

@Composable
fun KapsabetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, content = content)
}
