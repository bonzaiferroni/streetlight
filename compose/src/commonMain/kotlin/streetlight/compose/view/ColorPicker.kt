package streetlight.compose.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kampfire.model.MutableTap
import kampfire.model.mutableTapOf
import koala.SvgFile
import koala.modifier.Rgb
import streetlight.compose.modifier.borderRadius
import streetlight.compose.modifier.padding
import streetlight.compose.modifier.pointerClick
import streetlight.compose.utility.Hsl
import streetlight.compose.utility.collectAsState
import streetlight.compose.utility.toColor
import streetlight.compose.utility.toHsl
import streetlight.compose.utility.toRgb
import kotlin.math.roundToInt

/**
 * A pill in the color of [state] reading [label], opening sliders of its hue, saturation and lightness. The cousin of
 * koala's `colorPicker`.
 */
@Composable
fun ColorPicker(label: String, state: MutableTap<Rgb>, modifier: Modifier = Modifier) {
    val theme = currentTheme
    val color by state.collectAsState()
    var isOpen by remember { mutableStateOf(false) }
    val hueState = remember(state) { state.hslTapOf({ it.hue }) { copy(hue = it) } }
    val saturationState = remember(state) { state.hslTapOf({ it.saturation * 100 }) { copy(saturation = it / 100) } }
    val lightnessState = remember(state) { state.hslTapOf({ it.lightness * 100 }) { copy(lightness = it / 100) } }

    Box(modifier) {
        Box(
            Modifier.fillMaxWidth().height(theme.unit * PillUnits)
                .borderRadius(PillUnits / 2f, moonShadow = true)
                .background(color.toColor())
                .pointerClick { isOpen = true }
                .padding(horizontal = 2),
            contentAlignment = Alignment.Center,
        ) {
            BasicText(
                label.uppercase(),
                style = theme.text.small.copy(
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    shadow = Shadow(Color.Black.copy(alpha = 0.8f), Offset(0f, 1f), 2f),
                ),
                color = { Color.White },
            )
        }
        Popover(isOpen, onDismiss = { isOpen = false }) {
            Column(1, Modifier.width(theme.unit * PopoverUnits).padding(2)) {
                Box(
                    Modifier.width(theme.unit * PopoverUnits).height(theme.unit * SwatchUnits)
                        .clip(RoundedCornerShape(theme.unit))
                        .background(color.toColor())
                )
                Slider(hueState, 0..359, icon = SvgFile.Palette.small)
                Slider(saturationState, 0..100, icon = SvgFile.Droplet.small)
                Slider(lightnessState, 0..100, icon = SvgFile.Sun.small)
            }
        }
    }
}

/** A lens on one part of this color's hsl, [read] and [write] as a whole number. */
private fun MutableTap<Rgb>.hslTapOf(read: (Hsl) -> Float, write: Hsl.(Float) -> Hsl) =
    mutableTapOf({ read(it.toHsl()).roundToInt() }) { toHsl().write(it.toFloat()).toRgb() }

/** The height of the pill in units, as every button and field is. */
private const val PillUnits = 5

/** The width of the sliders' popover in units. */
private const val PopoverUnits = 40

/** The height of the color's swatch in units. */
private const val SwatchUnits = 6
