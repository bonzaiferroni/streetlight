package streetlight.compose.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import streetlight.model.data.EntityType

/**
 * The app's theme: the spacing [unit], the [text] styles, the [colors], the [opacity] levels, the page's [lights], and the [line]:
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
) {
    val line: Dp = 2.dp
}

val LocalKoalaAppTheme = staticCompositionLocalOf<KoalaAppTheme> { error("No KoalaAppTheme provided") }

/** The entity type whose color is the scheme color of the content within, as the web's `--color-scheme`, or `null`. */
val LocalSchemeColor = staticCompositionLocalOf<EntityType?> { null }

/** The theme [AppTheme] provides. */
val currentTheme: KoalaAppTheme
    @Composable @ReadOnlyComposable get() = LocalKoalaAppTheme.current

/** The spacing unit of [rem]: the shortest distance that shows two elements are separate, the web's `0.5rem`. */
fun unitOf(rem: TextUnit) = (rem.value * 0.5f).dp
