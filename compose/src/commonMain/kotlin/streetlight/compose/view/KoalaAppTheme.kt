package streetlight.compose.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.State
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

/**
 * The app's theme: the spacing [unit], the [text] styles, the [colors], the [opacity] levels, the page's [lights], its [magic], the [curve] of its corners, and the [line]:
 * the two pixels the web uses where a unit is too much, such as the gap between feed rows and the width of an outline. The counterpart of the web's `KoalaTheme`
 * and the stylesheets built from it, grouped as they are.
 */
@Stable
class KoalaAppTheme(
    val unit: Dp,
    val text: KoalaTypography,
    val colors: KoalaColors,
    val opacity: KoalaOpacity,
    val lights: KoalaLights,
    val magic: KoalaMagic,
    private val curveState: State<Float>,
) {
    val line: Dp = 2.dp

    /** The page's curve, from square corners at 0 to full ones at 1, held at 0 through the transition's overshoot. */
    val curve: Float get() = curveState.value.coerceAtLeast(0f)

    /** The radius of a corner of [units] at the page's [curve]. */
    fun radius(units: Number): Dp = unit * units.toFloat() * curve

    /** The shape of a pill at the page's [curve], round at its ends at 1. */
    val pillShape: Shape get() = RoundedCornerShape(percent = (50 * curve).roundToInt())
}

val LocalKoalaAppTheme = staticCompositionLocalOf<KoalaAppTheme> { error("No KoalaAppTheme provided") }

/**
 * The scheme color of the content within, as the web's `--color-scheme`: the ink of a neutral view, as [AppTheme]
 * provides it, and an entity's color within a layout that shows the entity, such as a feed row or a page header.
 */
val LocalSchemeColor = staticCompositionLocalOf<Color> { error("No scheme color provided") }

/** The page whose drawing a floating card blurs behind itself, captured where [AppScaffold] draws the page. */
val LocalPageHaze = staticCompositionLocalOf<HazeState> { error("No page HazeState provided") }

/** The theme [AppTheme] provides. */
val currentTheme: KoalaAppTheme
    @Composable @ReadOnlyComposable get() = LocalKoalaAppTheme.current

/** The spacing unit of [rem]: the shortest distance that shows two elements are separate, the web's `0.5rem`. */
fun unitOf(rem: TextUnit) = (rem.value * 0.5f).dp
