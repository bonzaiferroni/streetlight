package streetlight.compose.utility

import androidx.compose.ui.graphics.Color
import koala.modifier.Glow
import koala.modifier.Rgb
import kotlin.math.PI
import kotlin.math.cos
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
