package streetlight.compose.view

import androidx.compose.foundation.layout.Box
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import streetlight.compose.modifier.Outline
import streetlight.compose.modifier.borderRadius
import streetlight.compose.modifier.cardBg
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import androidx.compose.ui.unit.dp

/**
 * A menu of [content] on a card that blurs the page behind it, beside the composable that holds it while [isOpen]:
 * below it when it fits there, above it when only that fits, and otherwise on the side with more room, kept inside the
 * window. It grows out of the edge nearest its opener and shrinks back into it as it closes. A click outside calls
 * [onDismiss]. The cousin of the web's `popoverMenu`.
 */
@Composable
fun Popover(isOpen: Boolean, onDismiss: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val theme = currentTheme
    val magic = theme.magic
    val gap = with(LocalDensity.current) { theme.unit.roundToPx() }
    val positionProvider = remember(gap) { FlipPositionProvider(gap) }
    val visibility = remember { MutableTransitionState(false) }
    visibility.targetState = isOpen
    // the popup stays while it closes, so its exit plays out
    if (!visibility.currentState && !visibility.targetState) return

    Popup(positionProvider, onDismissRequest = onDismiss, properties = PopupProperties(focusable = true)) {
        AnimatedVisibility(visibility, enter = fadeIn(magic.eased()), exit = fadeOut(magic.eased())) {
            val scale by transition.animateFloat(
                transitionSpec = { if (targetState == EnterExitState.Visible) magic.bounced() else magic.eased() },
            ) { if (it == EnterExitState.Visible) 1f else magic.initialScale }
            val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
            Box(
                modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        // the corner at the opener: its start edge, on the side the popover opened toward
                        transformOrigin = TransformOrigin(if (isLtr) 0f else 1f, if (positionProvider.isAbove) 1f else 0f)
                    }
                    .borderRadius(3, moonShadow = true, outline = Outline.Mid)
                    .hazeBlur(HazeInput.Sources(LocalPageHaze.current), PopoverBlur)
                    .cardBg()
            ) {
                content()
            }
        }
    }
}

/** The blur of the page behind a popover, the web's `--stronger-blur` of 18px. */
private val PopoverBlur = HazeBlurStyle { blurRadius(18.dp) }

/**
 * Places a popup [gap] from its anchor, below it unless only above fits or above has more room, and keeps whether it
 * last placed it [isAbove].
 */
private class FlipPositionProvider(private val gap: Int) : PopupPositionProvider {
    var isAbove by mutableStateOf(false)
        private set

    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val height = popupContentSize.height
        val below = anchorBounds.bottom + gap
        val above = anchorBounds.top - gap - height
        val roomBelow = windowSize.height - below
        val roomAbove = anchorBounds.top - gap
        val y = when {
            height <= roomBelow -> below
            height <= roomAbove -> above
            roomBelow >= roomAbove -> below
            else -> above
        }.coerceIn(0, maxOf(0, windowSize.height - height))
        isAbove = y < anchorBounds.top

        val width = popupContentSize.width
        val start = if (layoutDirection == LayoutDirection.Ltr) anchorBounds.left else anchorBounds.right - width
        val x = start.coerceIn(0, maxOf(0, windowSize.width - width))
        return IntOffset(x, y)
    }
}
