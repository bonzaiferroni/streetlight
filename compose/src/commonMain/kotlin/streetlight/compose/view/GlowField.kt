package streetlight.compose.view

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import kampfire.model.MutableTap
import koala.modifier.Glow
import streetlight.compose.modifier.Outline
import streetlight.compose.modifier.borderRadius
import streetlight.compose.utility.collectAsState
import streetlight.compose.utility.toColor
import kotlin.math.roundToInt

/**
 * A field of the page in small, holding a handle for each of [glows] at its position, sized by its radius and lit by
 * its color and energy. Dragging a handle moves its glow. The cousin of koala's `glowField`, its three fields in one.
 */
@Composable
fun GlowField(glows: List<MutableTap<Glow>>, modifier: Modifier = Modifier) {
    val theme = currentTheme
    val values = glows.map { it.collectAsState().value }
    val current by rememberUpdatedState(values)
    var dragged by remember { mutableStateOf<Int?>(null) }

    val minHandle = with(LocalDensity.current) { (theme.unit * MinHandleUnits).toPx() }

    fun handleSize(glow: Glow, field: Size) = maxOf(field.width * glow.position.radius / 200f, minHandle)

    Box(
        modifier.fillMaxWidth()
            .aspectRatio(FieldAspect)
            .borderRadius(2, moonShadow = true, outline = Outline.Mid)
            .drawBehind {
                drawRect(theme.colors.voidBg)
                values.forEach { glow ->
                    val center = Offset(size.width * glow.position.x / 100f, size.height * glow.position.y / 100f)
                    drawCircle(glow.toColor(), handleSize(glow, size) / 2, center)
                }
            }
            .pointerInput(glows) {
                val field = Size(size.width.toFloat(), size.height.toFloat())
                detectDragGestures(
                    // the topmost handle under the pointer, the last drawn
                    onDragStart = { start ->
                        dragged = current.indices.lastOrNull { index ->
                            val glow = current[index]
                            val center = Offset(field.width * glow.position.x, field.height * glow.position.y) / 100f
                            (start - center).getDistance() <= handleSize(glow, field) / 2
                        }
                    },
                    onDragEnd = { dragged = null },
                    onDragCancel = { dragged = null },
                ) { change, _ ->
                    val index = dragged ?: return@detectDragGestures
                    val x = (change.position.x / field.width * 100).roundToInt().coerceIn(0, 100)
                    val y = (change.position.y / field.height * 100).roundToInt().coerceIn(0, 100)
                    glows[index].set { copy(position = position.copy(x = x, y = y)) }
                }
            },
    )
}

/** The field's shape, the web's `16 / 9`. */
private const val FieldAspect = 16f / 9f

/** The smallest a handle grows in units, the web's `1.5rem`. */
private const val MinHandleUnits = 3
