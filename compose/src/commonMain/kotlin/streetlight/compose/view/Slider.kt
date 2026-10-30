package streetlight.compose.view

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import kampfire.model.MutableTap
import koala.Svg
import streetlight.compose.utility.collectAsState
import kotlin.math.roundToInt

/**
 * A slider setting [state] within [range], after the [icon] that names it, when given: a track filled up to its thumb,
 * moved by a tap or a drag. The cousin of the web's `slider`.
 */
@Composable
fun Slider(state: MutableTap<Int>, range: IntRange, modifier: Modifier = Modifier, icon: Svg? = null) {
    val theme = currentTheme
    val colors = theme.colors
    val value by state.collectAsState()
    val currentValue by rememberUpdatedState(value)
    val span = (range.last - range.first).coerceAtLeast(1)

    // the thumb's center runs from one inset to the other, so the ends are reachable
    fun setAt(x: Float, width: Int, inset: Float) {
        val fraction = ((x - inset) / (width - inset * 2).coerceAtLeast(1f)).coerceIn(0f, 1f)
        val next = range.first + (fraction * span).roundToInt()
        if (next != currentValue) state.set(next)
    }

    Row(1, modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        icon?.let { Icon(it, color = { colors.ink.copy(alpha = theme.opacity.high) }) }
        Box(
            Modifier.weight(1f)
                .height(theme.unit * TrackUnits)
                .pointerInput(range) {
                    val inset = theme.unit.toPx() * ThumbUnits / 2
                    detectTapGestures { setAt(it.x, size.width, inset) }
                }
                .pointerInput(range) {
                    val inset = theme.unit.toPx() * ThumbUnits / 2
                    detectDragGestures { change, _ -> setAt(change.position.x, size.width, inset) }
                }
                .drawBehind {
                    val thumb = theme.unit.toPx() * ThumbUnits / 2
                    val line = theme.line.toPx() * 2
                    val start = thumb
                    val end = size.width - thumb
                    val x = start + (end - start) * (value - range.first).toFloat() / span
                    val y = size.height / 2
                    drawLine(colors.outlineMid, Offset(start, y), Offset(end, y), line, StrokeCap.Round)
                    drawLine(colors.primaryButton, Offset(start, y), Offset(x, y), line, StrokeCap.Round)
                    drawCircle(colors.primaryButton, thumb, Offset(x, y))
                },
        )
    }
}

/** The height of the slider's touch target in units. */
private const val TrackUnits = 4

/** The width of the thumb in units. */
private const val ThumbUnits = 2
