package koala.css

import kotlinx.serialization.Serializable

@Serializable
data class Rgb(val red: Int, val green: Int, val blue: Int) {
    override fun toString() = "${red.coerceIn(0, 255)}, ${green.coerceIn(0, 255)}, ${blue.coerceIn(0, 255)}"
}

operator fun Rgb.times(factor: Float): Rgb = Rgb(
    red = (red * factor).toInt().coerceIn(0, 255),
    green = (green * factor).toInt().coerceIn(0, 255),
    blue = (blue * factor).toInt().coerceIn(0, 255)
)

operator fun Rgb.times(factor: Int): Rgb = Rgb(
    red = (red * factor).coerceIn(0, 255),
    green = (green * factor).coerceIn(0, 255),
    blue = (blue * factor).coerceIn(0, 255)
)

fun rgbOf(hex: String) = rgbOfOrNull(hex) ?: error("invalid rgb: $hex")

fun rgbOfOrNull(hex: String): Rgb? {
    val digits = hex.removePrefix("#")
    if (digits.length != 6) return null
    val value = digits.toIntOrNull(16) ?: return null
    return Rgb(value shr 16 and 0xFF, value shr 8 and 0xFF, value and 0xFF)
}

fun Rgb.toHex(): String {
    fun Int.hex() = coerceIn(0, 255).toString(16).padStart(2, '0')
    return "#${red.hex()}${green.hex()}${blue.hex()}"
}