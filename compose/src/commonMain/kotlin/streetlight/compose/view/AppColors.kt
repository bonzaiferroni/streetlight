package streetlight.compose.view

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import streetlight.compose.utility.mixSrgb

/**
 * The theme's colors, each read from animated state.
 *
 * Reading a color subscribes the reader to that color alone, so a reader in a draw lambda redraws without recomposing.
 */
@Stable
class AppColors(
    val paper: Color,
    val ink: Color,
    private val accentState: State<Color>,
    private val primaryState: State<Color>,
) {
    val accent get() = accentState.value
    val primary get() = primaryState.value
    val accentFg get() = accent.mixSrgb(ink, 0.75f)
    val accentBg get() = accent.mixSrgb(paper, 0.75f)
    val primaryFg get() = primary.mixSrgb(ink, 0.75f)
    val primaryLightFg get() = primary.mixSrgb(ink, 0.25f)
}

val LocalAppColors = staticCompositionLocalOf<AppColors> { error("No AppColors provided") }
