package streetlight.compose.view

import androidx.compose.foundation.layout.Box
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
import streetlight.compose.modifier.heavyCardBg

/**
 * A menu of [content] on a card beside the composable that holds it: below it when it fits there, above it when only
 * that fits, and otherwise on the side with more room, kept inside the window. A click outside calls [onDismiss]. The
 * cousin of the web's `popoverMenu`.
 */
@Composable
fun Popover(onDismiss: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val theme = currentTheme
    val gap = with(LocalDensity.current) { theme.unit.roundToPx() }
    val positionProvider = remember(gap) { FlipPositionProvider(gap) }
    Popup(positionProvider, onDismissRequest = onDismiss, properties = PopupProperties(focusable = true)) {
        Box(modifier.borderRadius(3, moonShadow = true, outline = Outline.Mid).heavyCardBg()) {
            content()
        }
    }
}

/** Places a popup [gap] from its anchor, below it unless only above fits or above has more room. */
private class FlipPositionProvider(private val gap: Int) : PopupPositionProvider {
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

        val width = popupContentSize.width
        val start = if (layoutDirection == LayoutDirection.Ltr) anchorBounds.left else anchorBounds.right - width
        val x = start.coerceIn(0, maxOf(0, windowSize.width - width))
        return IntOffset(x, y)
    }
}
