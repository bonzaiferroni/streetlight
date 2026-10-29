package streetlight.compose.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import org.jetbrains.compose.resources.Font
import streetlight.compose.resources.Res
import streetlight.compose.resources.fira_sans_bold
import streetlight.compose.resources.fira_sans_bold_italic
import streetlight.compose.resources.fira_sans_extra_light
import streetlight.compose.resources.fira_sans_extra_light_italic
import streetlight.compose.resources.fira_sans_italic
import streetlight.compose.resources.fira_sans_light
import streetlight.compose.resources.fira_sans_light_italic
import streetlight.compose.resources.fira_sans_medium
import streetlight.compose.resources.fira_sans_medium_italic
import streetlight.compose.resources.fira_sans_regular
import streetlight.compose.resources.fira_sans_semi_bold
import streetlight.compose.resources.fira_sans_semi_bold_italic

/**
 * The app's text styles in [family], each a multiple of [rem] as in the web's `TypographyCss`.
 *
 * [regular] is the size of text with no style of its own, the rem.
 */
@Immutable
class KoalaTypography(private val family: FontFamily, rem: TextUnit) {
    val heading1 = styleOf(rem * 4, FontWeight.ExtraLight)
    val heading2 = styleOf(rem * 2.5f, FontWeight.ExtraLight)
    val heading3 = styleOf(rem * 2, FontWeight.Light)
    val heading4 = styleOf(rem * 1.6f, FontWeight.Light)
    val heading5 = styleOf(rem * 1.3f, FontWeight.Normal)
    val heading6 = styleOf(rem * 0.9f, FontWeight.Normal)
    val paragraph = styleOf(rem * 1.1f)
    val regular = styleOf(rem)
    val small = styleOf(rem * 0.9f)
    val label = styleOf(rem * 0.65f).copy(lineHeight = 1.em)
    val large = styleOf(rem * 1.4f)

    /** A style of [size] and [weight] at the web body's line height, its leading split above and below as CSS does. */
    private fun styleOf(size: TextUnit, weight: FontWeight = FontWeight.Normal) = TextStyle(
        fontFamily = family,
        fontSize = size,
        fontWeight = weight,
        lineHeight = 1.5.em,
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None),
    )
}

/** Fira Sans, the app's font, in the weights the app and the web use, each upright and italic. */
@Composable
fun firaSans() = FontFamily(
    Font(Res.font.fira_sans_extra_light, FontWeight.ExtraLight),
    Font(Res.font.fira_sans_extra_light_italic, FontWeight.ExtraLight, FontStyle.Italic),
    Font(Res.font.fira_sans_light, FontWeight.Light),
    Font(Res.font.fira_sans_light_italic, FontWeight.Light, FontStyle.Italic),
    Font(Res.font.fira_sans_regular, FontWeight.Normal),
    Font(Res.font.fira_sans_italic, FontWeight.Normal, FontStyle.Italic),
    Font(Res.font.fira_sans_medium, FontWeight.Medium),
    Font(Res.font.fira_sans_medium_italic, FontWeight.Medium, FontStyle.Italic),
    Font(Res.font.fira_sans_semi_bold, FontWeight.SemiBold),
    Font(Res.font.fira_sans_semi_bold_italic, FontWeight.SemiBold, FontStyle.Italic),
    Font(Res.font.fira_sans_bold, FontWeight.Bold),
    Font(Res.font.fira_sans_bold_italic, FontWeight.Bold, FontStyle.Italic),
)
