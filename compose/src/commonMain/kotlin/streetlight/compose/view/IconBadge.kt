package streetlight.compose.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import koala.SvgPack

/**
 * The animated icon of [svg] in [color] on a round badge: a soft glow of the color at its center and a faint ring. The
 * web's `ConnectStyle.IconBadge`.
 */
@Composable
fun IconBadge(svg: SvgPack, color: Color, modifier: Modifier = Modifier) {
    val theme = currentTheme
    Box(
        modifier.size(theme.unit * IconBadgeUnits).drawBehind {
            drawCircle(Brush.radialGradient(listOf(color.copy(alpha = 0.22f), Color.Transparent)))
            drawCircle(color.copy(alpha = 0.35f), radius = size.minDimension / 2 - 0.5f, style = Stroke(1f))
        },
        contentAlignment = Alignment.Center,
    ) {
        AnimatedIcon(svg.animated, color = { color }, size = theme.unit * IconUnits)
    }
}

/** The width of a badge in units, the web's `Width(16)`. */
const val IconBadgeUnits = 16

/** The width of a badge's icon in units, the web's `Width(8)`. */
private const val IconUnits = 8
