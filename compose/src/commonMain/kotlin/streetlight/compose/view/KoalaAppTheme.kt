package streetlight.compose.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import streetlight.compose.utility.mixSrgb

/**
 * The app's theme: the spacing [unit] and the theme's colors, the counterpart of the web's `KoalaTheme`.
 *
 * An animated color is read from state, so reading it subscribes the reader to that color alone, and a reader in
 * a draw lambda redraws without recomposing.
 */
@Stable
class KoalaAppTheme(
    val unit: Dp,
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

val LocalKoalaAppTheme = staticCompositionLocalOf<KoalaAppTheme> { error("No KoalaAppTheme provided") }

/** The theme [AppTheme] provides. */
val currentTheme: KoalaAppTheme
    @Composable @ReadOnlyComposable get() = LocalKoalaAppTheme.current

/** The spacing unit: the shortest distance that shows two elements are separate, the web's `0.5rem`. */
val DefaultUnit = 8.dp
