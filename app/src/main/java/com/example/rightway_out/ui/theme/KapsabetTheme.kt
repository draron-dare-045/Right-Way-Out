package com.example.rightway_out.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Kapsabet High School brand colours ───────────────────────────────────────
val MaroonPrimary      = Color(0xFF7B1E28)
val MaroonDark         = Color(0xFF4A0E14)
val MaroonLight        = Color(0xFFA83240)
val MaroonContainer    = Color(0xFFFFDAD9)
val CreamBackground    = Color(0xFFFAF6F0)
val CreamSurface       = Color(0xFFF5EFE6)
val ForestGreen        = Color(0xFF2D5A27)
val ForestGreenLight   = Color(0xFF4A7C43)
val GoldAccent         = Color(0xFFC9A84C)
val TextDark           = Color(0xFF1C1B1F)
val TextMedium         = Color(0xFF49454F)
val White              = Color(0xFFFFFFFF)

// Status colours (kept consistent)
val StatusPendingColor = Color(0xFFFFA726)
val StatusClearedColor = Color(0xFF2D5A27)   // forest green for cleared
val StatusFlaggedColor = Color(0xFF7B1E28)   // maroon for flagged

private val KapsabetColorScheme = lightColorScheme(
    primary            = MaroonPrimary,
    onPrimary          = White,
    primaryContainer   = MaroonContainer,
    onPrimaryContainer = MaroonDark,

    secondary          = ForestGreen,
    onSecondary        = White,
    secondaryContainer = Color(0xFFC8E6C4),
    onSecondaryContainer = Color(0xFF1B3D18),

    tertiary           = GoldAccent,
    onTertiary         = White,
    tertiaryContainer  = Color(0xFFFFF0C2),
    onTertiaryContainer = Color(0xFF4A3800),

    background         = CreamBackground,
    onBackground       = TextDark,

    surface            = White,
    onSurface          = TextDark,
    surfaceVariant     = CreamSurface,
    onSurfaceVariant   = TextMedium,

    error              = Color(0xFFBA1A1A),
    onError            = White,
    errorContainer     = Color(0xFFFFDAD6),
    onErrorContainer   = Color(0xFF410002),

    outline            = Color(0xFFCAC4D0),
    outlineVariant     = Color(0xFFE7E0EC),
)

@Composable
fun KapsabetTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KapsabetColorScheme,
        typography  = Typography(),
        content     = content
    )
}
