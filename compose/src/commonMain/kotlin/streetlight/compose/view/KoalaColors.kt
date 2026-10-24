package streetlight.compose.view

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.Color
import koala.modifier.Koala
import streetlight.compose.model.ThemeColor
import streetlight.compose.utility.mixSrgb
import streetlight.compose.utility.toColor

/**
 * The theme's colors, the counterpart of the color custom properties of the web's `ThemeCss`.
 *
 * An animated color is read from state, so reading it subscribes the reader to that color alone, and a reader in
 * a draw lambda redraws without recomposing.
 */
@Stable
class KoalaColors(
    val paper: Color,
    val ink: Color,
    opacity: KoalaOpacity,
    private val accentState: State<Color>,
    private val primaryState: State<Color>,
) {
    val accent get() = accentState.value
    val primary get() = primaryState.value
    val accentFg get() = accent.mixSrgb(ink, 0.5f)
    val accentBg get() = accent.mixSrgb(paper, 0.75f)
    val primaryFg get() = primary.mixSrgb(ink, 0.5f)
    val primaryLightFg get() = primary.mixSrgb(ink, 0.25f)
    val muted = ink.copy(alpha = opacity.high)
    val outlineLow = ink.copy(alpha = 0.2f)
    val zenBg = paper.copy(alpha = 0.25f)
    val cardBg = paper.copy(alpha = 0.4f)
    val seaGreenFg = Koala.seaGreen.toColor().mixSrgb(ink, 0.5f)
    val greenFg = Koala.green.toColor().mixSrgb(ink, 0.25f)
    val yellowFg = Koala.yellow.toColor().mixSrgb(ink, 0.25f)

    /** The color [color] names, as the web's `ThemeColor` maps to its custom properties. */
    fun colorOf(color: ThemeColor): Color = when (color) {
        ThemeColor.Ink -> ink
        ThemeColor.Muted -> muted
        ThemeColor.Accent, ThemeColor.Event -> accentFg
        ThemeColor.Primary, ThemeColor.Media -> primaryFg
        ThemeColor.Galaxy -> seaGreenFg
        ThemeColor.Location -> greenFg
        ThemeColor.City -> yellowFg
    }
}
