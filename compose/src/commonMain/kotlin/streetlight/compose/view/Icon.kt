package streetlight.compose.view

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import koala.Svg
import koala.iconPath
import org.jetbrains.compose.resources.decodeToSvgPainter
import streetlight.compose.resources.Res

/**
 * Draws [svg] in [color], filling its shape as the web's mask does. The cousin of koala's `icon`.
 *
 * [color] is read while drawing, so an animating color redraws the icon without recomposing it.
 */
@Composable
fun Icon(
    svg: Svg,
    color: () -> Color,
    modifier: Modifier = Modifier,
    size: Dp = currentTheme.unit * 3,
) {
    val painter = rememberSvgPainter(svg)
    Spacer(
        modifier.size(size).drawBehind {
            with(painter) { draw(this@drawBehind.size, colorFilter = ColorFilter.tint(color())) }
        }
    )
}

/** The painter of [svg] from the app's bundled icons, empty until its file is read. */
@Composable
fun rememberSvgPainter(svg: Svg): Painter {
    val density = LocalDensity.current
    val painter by produceState<Painter>(EmptyPainter, svg, density) {
        value = readIconBytes(svg).decodeToSvgPainter(density)
    }
    return painter
}

/** The bytes of [svg], read from its bundled file once and kept. */
private suspend fun readIconBytes(svg: Svg): ByteArray {
    val path = svg.toResourcePath()
    return iconBytes[path] ?: Res.readBytes(path).also { iconBytes[path] = it }
}

/** The resource of an icon served from `/www/icon/`, which the build bundles as `files/icon/`. */
private fun Svg.toResourcePath(): String {
    val name = url.value.removePrefix(iconPath).takeIf { it != url.value } ?: error("not a bundled icon: $url")
    return "files/icon/$name"
}

private val iconBytes = mutableMapOf<String, ByteArray>()

/** A painter that draws nothing, shown while an icon loads. */
private object EmptyPainter : Painter() {
    override val intrinsicSize = Size.Unspecified
    override fun DrawScope.onDraw() = Unit
}
