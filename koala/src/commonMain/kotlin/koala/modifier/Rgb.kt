package koala.modifier

import kotlinx.serialization.Serializable

/** A color by its red, green and blue channels, 0 to 255, written as `r, g, b` for use in `rgb()` and `rgba()`. */
@Serializable
data class Rgb(val red: Int, val green: Int, val blue: Int) {
    override fun toString() = "${red.coerceIn(0, 255)}, ${green.coerceIn(0, 255)}, ${blue.coerceIn(0, 255)}"
}

/** The color with each channel scaled by [factor]. */
operator fun Rgb.times(factor: Float): Rgb = Rgb(
    red = (red * factor).toInt().coerceIn(0, 255),
    green = (green * factor).toInt().coerceIn(0, 255),
    blue = (blue * factor).toInt().coerceIn(0, 255)
)

/** The color with each channel scaled by [factor]. */
operator fun Rgb.times(factor: Int): Rgb = Rgb(
    red = (red * factor).coerceIn(0, 255),
    green = (green * factor).coerceIn(0, 255),
    blue = (blue * factor).coerceIn(0, 255)
)

/** The color of the hex code [hex]. Throws when it is not valid. */
fun rgbOf(hex: String) = rgbOfOrNull(hex) ?: error("invalid rgb: $hex")

/** The color of the hex code [hex], or `null` when it is not valid. */
fun rgbOfOrNull(hex: String): Rgb? {
    val digits = hex.removePrefix("#")
    if (digits.length != 6) return null
    val value = digits.toIntOrNull(16) ?: return null
    return Rgb(value shr 16 and 0xFF, value shr 8 and 0xFF, value and 0xFF)
}

/** The color as a hex code. */
fun Rgb.toHex(): String {
    fun Int.hex() = coerceIn(0, 255).toString(16).padStart(2, '0')
    return "#${red.hex()}${green.hex()}${blue.hex()}"
}