package streetlight.compose.utility

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import koala.modifier.Glow
import koala.modifier.Rgb
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

fun Rgb.toColor() = Color(red, green, blue)

/** The glow's color, with its energy as the alpha. */
fun Glow.toColor() = Color(color.red, color.green, color.blue, (energy * 255 / 100).coerceIn(0, 255))

/** The color [amount] of this color mixed with [other] in sRGB, as CSS `color-mix(in srgb, …)` does. */
fun Color.mixSrgb(other: Color, amount: Float) = Color(
    red = red * amount + other.red * (1 - amount),
    green = green * amount + other.green * (1 - amount),
    blue = blue * amount + other.blue * (1 - amount),
    alpha = alpha * amount + other.alpha * (1 - amount),
)

/**
 * This color with its lightness scaled by [lightness] and its chroma by [chroma], as CSS
 * `oklch(from color calc(l * lightness) calc(c * chroma) h)` does.
 */
fun Color.scaleOklch(lightness: Float, chroma: Float): Color {
    val oklab = convert(ColorSpaces.Oklab)
    return Color(oklab.red * lightness, oklab.green * chroma, oklab.blue * chroma, oklab.alpha, ColorSpaces.Oklab)
        .convert(ColorSpaces.Srgb)
}

/** The color turned [degrees] around the hue wheel, as CSS `hue-rotate()` does. */
fun Color.rotateHue(degrees: Float): Color {
    val radians = degrees * PI.toFloat() / 180
    val c = cos(radians)
    val s = sin(radians)
    return Color(
        red = (red * (0.213f + 0.787f * c - 0.213f * s) + green * (0.715f - 0.715f * c - 0.715f * s) + blue * (0.072f - 0.072f * c + 0.928f * s)).coerceIn(0f, 1f),
        green = (red * (0.213f - 0.213f * c + 0.143f * s) + green * (0.715f + 0.285f * c + 0.140f * s) + blue * (0.072f - 0.072f * c - 0.283f * s)).coerceIn(0f, 1f),
        blue = (red * (0.213f - 0.213f * c - 0.787f * s) + green * (0.715f - 0.715f * c + 0.715f * s) + blue * (0.072f + 0.928f * c + 0.072f * s)).coerceIn(0f, 1f),
        alpha = alpha,
    )
}

/** A color as CSS `hsl()` reads it: its [hue] in degrees, its [saturation] and [lightness] from 0 to 1. */
data class Hsl(val hue: Float, val saturation: Float, val lightness: Float)

fun Rgb.toHsl(): Hsl {
    val r = red / 255f
    val g = green / 255f
    val b = blue / 255f
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val lightness = (max + min) / 2
    val delta = max - min
    if (delta == 0f) return Hsl(0f, 0f, lightness)
    val saturation = delta / (1 - abs(2 * lightness - 1))
    val hue = when (max) {
        r -> 60 * (((g - b) / delta).mod(6f))
        g -> 60 * ((b - r) / delta + 2)
        else -> 60 * ((r - g) / delta + 4)
    }
    return Hsl(hue, saturation, lightness)
}

fun Hsl.toRgb(): Rgb {
    val color = Color.hsl(hue.mod(360f), saturation.coerceIn(0f, 1f), lightness.coerceIn(0f, 1f))
    return Rgb((color.red * 255).roundToInt(), (color.green * 255).roundToInt(), (color.blue * 255).roundToInt())
}
