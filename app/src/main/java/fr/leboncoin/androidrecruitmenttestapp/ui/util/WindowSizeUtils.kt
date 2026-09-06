package fr.leboncoin.androidrecruitmenttestapp.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp

// Material's own breakpoint for "medium width and up" (the same threshold
// `WindowWidthSizeClass.Medium` uses).
private val TWO_PANE_MIN_WIDTH = Dp(600f)

/**
 * Whether there's enough width to show a list-detail split view instead of navigating between
 * separate screens. Deliberately width-based rather than orientation-based: a phone in landscape
 * almost always crosses this threshold (so this *is* the practical landscape behavior), while a
 * tablet in portrait can still qualify and a small landscape window correctly won't - a plain
 * `Configuration.orientation` check can't tell those apart.
 *
 * Reads the window's actual pixel size via `LocalWindowInfo` rather than
 * `Configuration.screenWidthDp` - the latter is flagged by lint (`ConfigurationScreenWidthHeight`)
 * as unreliable across some multi-window/resize scenarios.
 */
@Composable
fun rememberIsTwoPane(): Boolean {
    val containerWidthPx = LocalWindowInfo.current.containerSize.width
    val density = LocalDensity.current
    return remember(containerWidthPx, density) {
        with(density) { containerWidthPx.toDp() >= TWO_PANE_MIN_WIDTH }
    }
}
