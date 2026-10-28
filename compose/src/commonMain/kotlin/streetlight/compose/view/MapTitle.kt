package streetlight.compose.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import streetlight.compose.modifier.filigree

/** The [title] of a map in a filigree on a band of paper across its top. The cousin of the web's map title. */
@Composable
fun MapTitle(title: String, modifier: Modifier = Modifier) {
    val theme = currentTheme
    Box(
        modifier.fillMaxWidth().height(theme.unit * TitleHeightUnits).drawBehind {
            drawRect(
                Brush.horizontalGradient(
                    0f to Color.Transparent,
                    0.5f to theme.colors.paper.copy(alpha = TitleBandAlpha),
                    1f to Color.Transparent,
                )
            )
        },
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            title,
            Modifier.fillMaxWidth().filigree(),
            style = theme.text.heading3.copy(lineHeight = 1.em, textAlign = TextAlign.Center),
            color = { theme.colors.ink },
        )
    }
}

/** The height of the title's band in units, the web's `Height(8)`. */
private const val TitleHeightUnits = 8

/** The paper's opacity at the middle of the title's band, as the web's `EarthStyle.MapTitle`. */
private const val TitleBandAlpha = 0.6f
