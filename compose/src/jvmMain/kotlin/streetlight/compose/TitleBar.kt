package streetlight.compose

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import koala.SvgFile
import streetlight.compose.model.currentPortal
import streetlight.compose.modifier.pointerClick
import streetlight.compose.view.Icon
import streetlight.compose.view.currentTheme
import java.awt.Cursor
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import koala.Svg
import streetlight.compose.modifier.opacityHigh
import streetlight.compose.modifier.padding
import androidx.compose.ui.text.style.TextOverflow

/**
 * The window's own title bar, a plain band of dark gray above the page: the back button at the start, dimmed while there
 * is nowhere to go back to; the app's name centered on the window over the area that drags it, which maximizes it when
 * double-clicked; and the window's controls at the end, minimizing, maximizing and closing it with [onClose]. A button
 * lights when hovered, the close button in red.
 */
@Composable
fun FrameWindowScope.TitleBar(state: WindowState, onClose: () -> Unit) {
    val theme = currentTheme
    val portal = currentPortal
    val isMaximized = state.placement == WindowPlacement.Maximized
    val toggleMaximized = {
        state.placement = if (isMaximized) WindowPlacement.Floating else WindowPlacement.Maximized
    }

    Box(Modifier.fillMaxWidth().height(theme.unit * BarUnits).background(BarColor)) {
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            BarButton(SvgFile.ArrowLeft.small, onClick = portal::back, isEnabled = portal.backStack.size > 1)
            WindowDraggableArea(
                Modifier.weight(1f).fillMaxHeight().pointerInput(isMaximized) { detectTapGestures(onDoubleTap = { toggleMaximized() }) }
            ) {}
            BarButton(SvgFile.Minus.small, onClick = { state.isMinimized = true })
            BarButton(if (isMaximized) SvgFile.Minimize.small else SvgFile.Maximize.small, onClick = toggleMaximized)
            BarButton(SvgFile.X.small, onClick = onClose, hoverColor = theme.colors.red)
        }
        // centered on the window, held clear of the widest group of buttons on either side, so it never sits under one
        BasicText(
            "Streetlight",
            Modifier.align(Alignment.Center).padding(horizontal = ButtonUnits * SideButtons).opacityHigh(),
            style = theme.text.small,
            color = { theme.colors.ink },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** A button of the bar drawn as [svg], lit with [hoverColor] while hovered, and dimmed and still unless [isEnabled]. */
@Composable
private fun BarButton(svg: Svg, onClick: () -> Unit, isEnabled: Boolean = true, hoverColor: Color = HoverColor) {
    val theme = currentTheme
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    Box(
        Modifier.fillMaxHeight()
            .width(theme.unit * ButtonUnits)
            .background(if (isHovered && isEnabled) hoverColor else Color.Transparent)
            .hoverable(interactionSource)
            .then(if (isEnabled) Modifier.pointerClick(onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Icon(svg, color = { theme.colors.ink }, modifier = Modifier.alpha(if (isEnabled) 1f else IdleAlpha))
    }
}

/**
 * Handles along the window's right and bottom edges and its bottom right corner that resize it while it floats, as an
 * undecorated window has no edges of its own.
 */
@Composable
fun BoxScope.ResizeEdges(state: WindowState) {
    if (state.placement != WindowPlacement.Floating) return
    val density = LocalDensity.current
    fun Modifier.resizing(cursor: Int, width: Boolean, height: Boolean) =
        pointerHoverIcon(PointerIcon(Cursor(cursor))).pointerInput(Unit) {
            detectDragGestures { change, drag ->
                change.consume()
                with(density) {
                    state.size = DpSize(
                        state.size.width + if (width) drag.x.toDp() else 0.dp,
                        state.size.height + if (height) drag.y.toDp() else 0.dp,
                    )
                }
            }
        }
    Box(Modifier.align(Alignment.CenterEnd).width(EdgeWidth).fillMaxHeight().resizing(Cursor.E_RESIZE_CURSOR, width = true, height = false))
    Box(Modifier.align(Alignment.BottomCenter).height(EdgeWidth).fillMaxWidth().resizing(Cursor.S_RESIZE_CURSOR, width = false, height = true))
    Box(Modifier.align(Alignment.BottomEnd).size(EdgeWidth * 3).resizing(Cursor.SE_RESIZE_CURSOR, width = true, height = true))
}

/** The height of the bar in units. */
private const val BarUnits = 4

/** The width of a button of the bar in units. */
private const val ButtonUnits = 6

/** The most buttons on either side of the bar. */
private const val SideButtons = 3

/** The narrowest the window may be, its title clear of the buttons on either side, in dp. */
const val WindowMinWidth = 480

/** The shortest the window may be, in dp. */
const val WindowMinHeight = 360

/** The color of the bar, a dark gray apart from the page's theme. */
private val BarColor = Color(0xFF1E1F21)

/** The light behind a hovered button of the bar. */
private val HoverColor = Color.White.copy(alpha = 0.1f)

/** The opacity of the back arrow while there is nowhere to go back to. */
private const val IdleAlpha = 0.2f

/** The width of an edge that resizes the window. */
private val EdgeWidth = 6.dp
