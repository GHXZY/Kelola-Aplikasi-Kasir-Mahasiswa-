package com.example.ui.theme

import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val LocalIsDarkTheme = compositionLocalOf { false }

@Composable
@ReadOnlyComposable
fun isAppInDarkTheme(): Boolean {
    return LocalIsDarkTheme.current
}

// =========================================================================
// RADIUS TOKENS (Level 2: Rounded)
// - sm: 4dp (0.25rem)
// - DEFAULT: 8dp (0.5rem) -> Inputs, buttons, chips, form elements
// - md: 12dp (0.75rem)
// - lg: 16dp (1rem) -> Cards, modals, flyout panels
// - xl: 24dp (1.5rem)
// - full: 9999px (CircleShape)
// =========================================================================
object KelolaRadius {
    val Sm: Dp = 4.dp             // 0.25rem
    val Small: Dp = 8.dp          // 0.5rem - chip, badges
    val Input: Dp = 12.dp         // 0.75rem - inputs, buttons, form elements
    val Md: Dp = 12.dp            // 0.75rem
    val Card: Dp = 12.dp          // 0.75rem - cards, containers
    val Hero: Dp = 12.dp          // 0.75rem
    val Lg: Dp = 16.dp            // 1rem
    val Sheet: Dp = 16.dp         // 1rem (rounded-lg)
    val Xl: Dp = 24.dp            // 1.5rem
    val Full: Shape = CircleShape // 9999px

    val ShapeSm = RoundedCornerShape(4.dp)
    val ShapeSmall = RoundedCornerShape(8.dp)
    val ShapeChip = RoundedCornerShape(8.dp)
    val ShapeInput = RoundedCornerShape(12.dp)
    val ShapeMedium = RoundedCornerShape(12.dp)
    val ShapeCard = RoundedCornerShape(12.dp)
    val ShapeHero = RoundedCornerShape(12.dp)
    val ShapeLarge = RoundedCornerShape(16.dp)
    val ShapeSheet = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    val ShapeXl = RoundedCornerShape(24.dp)
}

// =========================================================================
// 8 MATERIAL 3 COLOR SCHEMES (4 Themes x 2 Modes)
// Designed strictly according to design specs:
// - Oceanic Modernity (DESIGN (1).md)
// - Blush Blossom (Pink Light.md & Pink Dark.md)
// - Terra & Flora (Coklat Light.md & Coklat drak.md)
// - Solar Flare (Orenge light.md & Orange dark.md)
// =========================================================================

// 1. DEFAULT (Oceanic Modernity)
private val OceanicLightColorScheme = lightColorScheme(
    primary = Color(0xFF006199),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF006199),
    onPrimaryContainer = Color(0xFFB7D9FF),
    secondary = Color(0xFF0B658A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF8FD4FE),
    onSecondaryContainer = Color(0xFF005D7F),
    tertiary = Color(0xFF264865),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF3F607E),
    onTertiaryContainer = Color(0xFFB8DAFE),
    background = Color(0xFFF7F9FF),
    onBackground = Color(0xFF161C23),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF161C23),
    surfaceVariant = Color(0xFFEEF4FE),
    onSurfaceVariant = Color(0xFF414750),
    outline = Color(0xFF717881),
    outlineVariant = Color(0xFFE2E8F0),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A)
)

private val OceanicDarkColorScheme = darkColorScheme(
    primary = Color(0xFF97CBFF),
    onPrimary = Color(0xFF001D33),
    primaryContainer = Color(0xFF004974),
    onPrimaryContainer = Color(0xFFCEE5FF),
    secondary = Color(0xFF8ACFF8),
    onSecondary = Color(0xFF001E2D),
    secondaryContainer = Color(0xFF004C69),
    onSecondaryContainer = Color(0xFFC4E7FF),
    tertiary = Color(0xFFA8CAED),
    onTertiary = Color(0xFF001D32),
    tertiaryContainer = Color(0xFF264865),
    onTertiaryContainer = Color(0xFFCEE5FF),
    background = Color(0xFF0C141B),
    onBackground = Color(0xFFECF1FB),
    surface = Color(0xFF121B24),
    onSurface = Color(0xFFECF1FB),
    surfaceVariant = Color(0xFF192531),
    onSurfaceVariant = Color(0xFFC0C7D1),
    outline = Color(0xFF717881),
    outlineVariant = Color(0xFF263542),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

// 2. PINK (Blush Blossom)
private val BlushLightColorScheme = lightColorScheme(
    primary = Color(0xFFA7354B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFF788D),
    onPrimaryContainer = Color(0xFF740B29),
    secondary = Color(0xFF735858),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDADA),
    onSecondaryContainer = Color(0xFF795D5E),
    tertiary = Color(0xFF874D5A),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD5909D),
    onTertiaryContainer = Color(0xFF5C2935),
    background = Color(0xFFFFF8F8),
    onBackground = Color(0xFF22191C),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF22191C),
    surfaceVariant = Color(0xFFFFF0F3),
    onSurfaceVariant = Color(0xFF574143),
    outline = Color(0xFF8A7173),
    outlineVariant = Color(0xFFDDBFC1),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A)
)

private val BlushDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB2BA),
    onPrimary = Color(0xFF670020),
    primaryContainer = Color(0xFFFF788D),
    onPrimaryContainer = Color(0xFF740B29),
    secondary = Color(0xFFE1BEBE),
    onSecondary = Color(0xFF412A2B),
    secondaryContainer = Color(0xFF594041),
    onSecondaryContainer = Color(0xFFCFADAD),
    tertiary = Color(0xFFFCB3C0),
    onTertiary = Color(0xFF51212D),
    tertiaryContainer = Color(0xFFD5909D),
    onTertiaryContainer = Color(0xFF5C2935),
    background = Color(0xFF1A1114),
    onBackground = Color(0xFFF0DEE2),
    surface = Color(0xFF22191C),
    onSurface = Color(0xFFF0DEE2),
    surfaceVariant = Color(0xFF271D20),
    onSurfaceVariant = Color(0xFFDDBFC1),
    outline = Color(0xFFA58A8C),
    outlineVariant = Color(0xFF574143),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

// 3. COKLAT (Terra & Flora)
private val TerraLightColorScheme = lightColorScheme(
    primary = Color(0xFF6F472B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF8A5F41),
    onPrimaryContainer = Color(0xFFFFE4D4),
    secondary = Color(0xFF5A6334),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDEE8AD),
    onSecondaryContainer = Color(0xFF60693A),
    tertiary = Color(0xFF7C4113),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF9A5829),
    onTertiaryContainer = Color(0xFFFFE4D5),
    background = Color(0xFFFDF9F2),
    onBackground = Color(0xFF1C1C18),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1C18),
    surfaceVariant = Color(0xFFF7F3ED),
    onSurfaceVariant = Color(0xFF51443D),
    outline = Color(0xFF83746C),
    outlineVariant = Color(0xFFD5C3B9),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A)
)

private val TerraDarkColorScheme = darkColorScheme(
    primary = Color(0xFFF2BB97),
    onPrimary = Color(0xFF49280F),
    primaryContainer = Color(0xFF8A5F41),
    onPrimaryContainer = Color(0xFFFFE4D4),
    secondary = Color(0xFFC2CC93),
    onSecondary = Color(0xFF2C340A),
    secondaryContainer = Color(0xFF424B1F),
    onSecondaryContainer = Color(0xFFB0BA83),
    tertiary = Color(0xFFFFB688),
    onTertiary = Color(0xFF512400),
    tertiaryContainer = Color(0xFF9A5829),
    onTertiaryContainer = Color(0xFFFFE4D5),
    background = Color(0xFF141310),
    onBackground = Color(0xFFE6E2DC),
    surface = Color(0xFF1C1C18),
    onSurface = Color(0xFFE6E2DC),
    surfaceVariant = Color(0xFF20201C),
    onSurfaceVariant = Color(0xFFD5C3B9),
    outline = Color(0xFF9D8E85),
    outlineVariant = Color(0xFF51443D),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

// 4. ORANGE (Solar Flare)
private val SolarLightColorScheme = lightColorScheme(
    primary = Color(0xFF845400),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFCAD38),
    onPrimaryContainer = Color(0xFF6B4400),
    secondary = Color(0xFFAE3020),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFD6952),
    onSecondaryContainer = Color(0xFF680400),
    tertiary = Color(0xFF176B4B),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF7FCDA6),
    onTertiaryContainer = Color(0xFF00583A),
    background = Color(0xFFFFF8F6),
    onBackground = Color(0xFF1F1B1A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1F1B1A),
    surfaceVariant = Color(0xFFFBF2EF),
    onSurfaceVariant = Color(0xFF524435),
    outline = Color(0xFF847463),
    outlineVariant = Color(0xFFD7C3AF),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A)
)

private val SolarDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFD199),
    onPrimary = Color(0xFF462A00),
    primaryContainer = Color(0xFFFCAD38),
    onPrimaryContainer = Color(0xFF6B4400),
    secondary = Color(0xFFFFB4A7),
    onSecondary = Color(0xFF670400),
    secondaryContainer = Color(0xFF901A0C),
    onSecondaryContainer = Color(0xFFFF9F8F),
    tertiary = Color(0xFF9AE9C0),
    onTertiary = Color(0xFF003824),
    tertiaryContainer = Color(0xFF7FCDA6),
    onTertiaryContainer = Color(0xFF00583A),
    background = Color(0xFF161312),
    onBackground = Color(0xFFEAE1DE),
    surface = Color(0xFF1F1B1A),
    onSurface = Color(0xFFEAE1DE),
    surfaceVariant = Color(0xFF231F1E),
    onSurfaceVariant = Color(0xFFD7C3AF),
    outline = Color(0xFF9F8E7B),
    outlineVariant = Color(0xFF524435),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

fun colorSchemeForTheme(theme: ColorTheme, isDark: Boolean): ColorScheme {
    return when (theme) {
        ColorTheme.DEFAULT -> if (isDark) OceanicDarkColorScheme else OceanicLightColorScheme
        ColorTheme.PINK -> if (isDark) BlushDarkColorScheme else BlushLightColorScheme
        ColorTheme.COKLAT -> if (isDark) TerraDarkColorScheme else TerraLightColorScheme
        ColorTheme.ORANGE -> if (isDark) SolarDarkColorScheme else SolarLightColorScheme
    }
}

// =========================================================================
// EXTENSIONS & UTILITIES
// =========================================================================
object KelolaTheme {
    val brandGradient: Brush
        @Composable
        get() = GradientBrand

    val shadowSoft: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isAppInDarkTheme()) DarkShadowSoft else LightShadowSoft

    val positive: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isAppInDarkTheme()) DarkPositive else LightPositive

    val negative: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isAppInDarkTheme()) DarkNegative else LightNegative

    val warning: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isAppInDarkTheme()) DarkWarning else LightWarning

    val textTertiary: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isAppInDarkTheme()) DarkTextTertiary else LightTextTertiary
}

@Composable
fun Modifier.kelolaSoftShadow(
    shape: Shape = KelolaRadius.ShapeCard,
    elevation: Dp = 2.dp,
    showBorder: Boolean = true
): Modifier {
    val isDark = isAppInDarkTheme()
    val shadowColor = if (isDark) DarkShadowSoft else LightShadowSoft
    val borderColor = if (isDark) DarkBorder else LightBorder
    val shadowed = this.shadow(
        elevation = elevation,
        shape = shape,
        clip = false,
        ambientColor = shadowColor,
        spotColor = shadowColor
    )
    return if (showBorder) {
        shadowed.border(width = 1.dp, color = borderColor, shape = shape)
    } else {
        shadowed
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorTheme: ColorTheme = ColorTheme.DEFAULT,
    content: @Composable () -> Unit,
) {
    val colorScheme = colorSchemeForTheme(colorTheme, darkTheme)

    CompositionLocalProvider(
        LocalIsDarkTheme provides darkTheme,
        LocalColorTheme provides colorTheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
