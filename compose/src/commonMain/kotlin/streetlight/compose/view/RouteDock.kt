package streetlight.compose.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import koala.SvgFile
import streetlight.compose.model.DockRoute
import streetlight.compose.model.FocusDock
import streetlight.compose.model.StableDock
import streetlight.compose.model.currentPortal
import streetlight.compose.model.toFocusDock
import streetlight.compose.modifier.padding
import streetlight.compose.modifier.pointerClick
import streetlight.compose.utility.mixSrgb

/**
 * The dock over the bottom of the page: a pill of the routes of the [StableDock], or of the [FocusDock] of the route on
 * top, centered, with a tray at its left that switches between them while the route has a focus. Each new focus shows
 * its own dock first. The cousin of the web's `viewRouteDock`.
 */
@Composable
fun RouteDock(modifier: Modifier = Modifier) {
    val theme = currentTheme
    val portal = currentPortal
    val focusDock = portal.backStack.last().toFocusDock()
    var isFocusShown by remember(focusDock) { mutableStateOf(true) }
    val dock = focusDock?.routes?.takeIf { isFocusShown } ?: StableDock

    // the sides share the width left over, so the routes stay centered and each tray holds to them
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(theme.unit),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            FocusTray(focusDock, isFocusShown) { isFocusShown = !isFocusShown }
        }
        DockRoutes(dock)
        Spacer(Modifier.weight(1f))
    }
}

/**
 * A tray, shown while there is a [focusDock], that calls [onToggle] to switch between the docks. It matches the dock
 * shown: the focus dock's icon on the primary background while [isFocusShown], otherwise the flame on neutral glass.
 */
@Composable
private fun FocusTray(focusDock: FocusDock?, isFocusShown: Boolean, onToggle: () -> Unit) {
    val theme = currentTheme
    val colors = theme.colors
    val magic = theme.magic
    // the last focus stays while the tray fades out
    var shownDock by remember { mutableStateOf(focusDock) }
    focusDock?.let { shownDock = it }
    val primary = animateFloatAsState(if (isFocusShown) 1f else 0f, magic.eased())

    AnimatedVisibility(focusDock != null, enter = fadeIn(magic.eased()), exit = fadeOut(magic.eased())) {
        DockGlass(Modifier.pointerClick(onToggle), glass = { drawGlass(colors, primary.value) }) {
            val icon = shownDock?.icon?.takeIf { isFocusShown } ?: SvgFile.Flame
            AnimatedContent(icon, transitionSpec = { fadeIn(magic.eased()) togetherWith fadeOut(magic.eased()) }) {
                Icon(it.small, color = { colors.ink.copy(alpha = InkAlpha) })
            }
        }
    }
}

/**
 * The labels of [dock], fading from one dock to the next, on glass that turns to the primary background for a focus
 * dock. A route of the [StableDock] shows its stack; a route of a focus dock takes the place of the route on top.
 */
@Composable
private fun DockRoutes(dock: List<DockRoute>) {
    val theme = currentTheme
    val colors = theme.colors
    val magic = theme.magic
    val portal = currentPortal
    val focus = animateFloatAsState(if (dock == StableDock) 0f else 1f, magic.eased())

    DockGlass(glass = { drawGlass(colors, focus.value) }) {
        AnimatedContent(dock, transitionSpec = { fadeIn(magic.eased()) togetherWith fadeOut(magic.eased()) }) { routes ->
            val isStable = routes == StableDock
            Row(verticalAlignment = Alignment.CenterVertically) {
                routes.forEach { dockRoute ->
                    val isShown = when {
                        isStable -> dockRoute.route == portal.dockRoute
                        else -> dockRoute.route == portal.backStack.last()
                    }
                    // the outline of the route shown fades in and out, read while drawing
                    val outline = animateFloatAsState(if (isShown) 1f else 0f, magic.eased())
                    BasicText(
                        dockRoute.label.uppercase(),
                        Modifier.pillOutline(theme.line) { colors.primary.copy(alpha = colors.primary.alpha * outline.value) }
                            .clip(PillShape)
                            .pointerClick {
                                when {
                                    isStable -> portal.dock(dockRoute.route)
                                    !isShown -> portal.swap(dockRoute.route)
                                }
                            }
                            .padding(horizontal = 2, vertical = 1),
                        style = theme.text.small.copy(fontWeight = FontWeight.SemiBold),
                        color = { if (isShown) colors.ink else colors.ink.copy(alpha = InkAlpha) },
                    )
                }
            }
        }
    }
}

/** A pill of [content] on the [glass] it draws, which blurs the page beneath it. */
@Composable
private fun DockGlass(modifier: Modifier = Modifier, glass: DrawScope.() -> Unit, content: @Composable () -> Unit) {
    val theme = currentTheme
    Box(
        Modifier.clip(PillShape)
            .hazeBlur(HazeInput.Sources(LocalPageHaze.current), DockBlur)
            .drawBehind(glass)
            .border(theme.line, theme.colors.outlineMid, PillShape)
            .then(modifier)
            .padding(1),
    ) {
        content()
    }
}

/** Draws the glass of the dock, turned [primary] of the way from paper to the primary background. */
private fun DrawScope.drawGlass(colors: KoalaColors, primary: Float) {
    drawRect(colors.paper, alpha = GlassAlpha * (1f - primary))
    drawRect(colors.primaryBg(size), alpha = GlassAlpha * primary)
}

private val PillShape = RoundedCornerShape(50)

/** The blur of the page beneath the dock, the web's `--strong-blur` of 10px. */
private val DockBlur = HazeBlurStyle { blurRadius(10.dp) }

/** The glass of the dock and its ink, the web's paper at `.6` and ink at `.8`. */
private const val GlassAlpha = 0.6f
private const val InkAlpha = 0.8f
