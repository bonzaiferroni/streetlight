package streetlight.compose.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import streetlight.compose.model.DockRoute
import streetlight.compose.model.StableDock
import streetlight.compose.model.currentPortal
import streetlight.compose.modifier.padding
import streetlight.compose.modifier.pointerClick

/**
 * A pill of the labels of [dock] on glass that blurs the page beneath it, each opening its route's stack, the one shown
 * outlined in the primary color. The cousin of the main routes of the web's `viewRouteDock`.
 */
@Composable
fun RouteDock(modifier: Modifier = Modifier, dock: List<DockRoute> = StableDock) {
    val theme = currentTheme
    val colors = theme.colors
    val portal = currentPortal

    Row(
        modifier.clip(PillShape)
            .hazeBlur(HazeInput.Sources(LocalPageHaze.current), DockBlur)
            .background(colors.paper.copy(alpha = GlassAlpha))
            .border(theme.line, colors.outlineMid, PillShape)
            .padding(1),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        dock.forEach { dockRoute ->
            val isShown = dockRoute.route == portal.dockRoute
            val routeMod = when {
                isShown -> Modifier.border(theme.line, colors.primary, PillShape)
                else -> Modifier
            }
            BasicText(
                dockRoute.label.uppercase(),
                routeMod.clip(PillShape)
                    .pointerClick { portal.dock(dockRoute.route) }
                    .padding(horizontal = 2, vertical = 1),
                style = theme.text.small.copy(fontWeight = FontWeight.SemiBold),
                color = { if (isShown) colors.ink else colors.ink.copy(alpha = InkAlpha) },
            )
        }
    }
}

private val PillShape = RoundedCornerShape(50)

/** The blur of the page beneath the dock, the web's `--strong-blur` of 10px. */
private val DockBlur = HazeBlurStyle { blurRadius(10.dp) }

/** The glass of the dock and its ink, the web's paper at `.6` and ink at `.8`. */
private const val GlassAlpha = 0.6f
private const val InkAlpha = 0.8f
