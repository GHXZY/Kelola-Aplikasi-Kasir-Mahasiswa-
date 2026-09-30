package com.example.ui.theme

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Window Width Size Classes (Standar Android & Material 3 Adaptive):
 * - COMPACT: < 600dp (Mayoritas handphone portrait) -> BASELINE DESIGN (100% UTUH)
 * - MEDIUM: 600dp .. 839dp (Tablet portrait, layar lipat/foldable unfolded)
 * - EXPANDED: >= 840dp (Tablet landscape, desktop, layar besar)
 */
enum class WindowSizeClassType {
    COMPACT,
    MEDIUM,
    EXPANDED;

    val isCompact: Boolean get() = this == COMPACT
    val isMedium: Boolean get() = this == MEDIUM
    val isExpanded: Boolean get() = this == EXPANDED
    val isMediumOrExpanded: Boolean get() = this != COMPACT
}

val LocalWindowSizeClass = compositionLocalOf { WindowSizeClassType.COMPACT }

@Composable
fun rememberWindowSizeClass(): WindowSizeClassType {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    return when {
        screenWidth < 600 -> WindowSizeClassType.COMPACT
        screenWidth < 840 -> WindowSizeClassType.MEDIUM
        else -> WindowSizeClassType.EXPANDED
    }
}

@Composable
fun isLandscapeOrientation(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}

/**
 * Adaptive Content Container:
 * Pada layar Compact: fillMaxSize() (100% baseline handphone).
 * Pada layar Medium/Expanded: membatasi lebar maksimum (agar kartu/form tidak melar tak terhingga)
 * dan memusatkan konten secara proporsional.
 */
@Composable
fun AdaptiveContainer(
    modifier: Modifier = Modifier,
    maxWidth: Dp = 1040.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val windowSize = LocalWindowSizeClass.current
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = if (windowSize.isCompact) {
                Modifier.fillMaxSize()
            } else {
                Modifier
                    .fillMaxSize()
                    .widthIn(max = maxWidth)
            },
            content = content
        )
    }
}
