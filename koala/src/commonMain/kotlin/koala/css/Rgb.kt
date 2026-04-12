package koala.css

data class Rgb(val red: Int, val green: Int, val blue: Int) {
    override fun toString() = "$red, $green, $blue"
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