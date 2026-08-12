package koala.css

import kotlinx.css.*
import kotlinx.serialization.Serializable

data class KoalaTheme(
    val paper: Rgb = Rgb(18, 26, 26),
    val ink: Rgb = Rgb(238, 230, 230),
    val spacingUnit: LinearDimension = 0.5.rem,
    val bg: Rgb = Rgb(9, 13, 13),
    val fg: Rgb = Rgb(245, 246, 246),
    val void: Rgb = Rgb(24, 31, 31),
    val accent: Rgb = Rgb(200, 87, 178),
    val primary: Rgb = Rgb(58, 158, 200),

    val rho: BackgroundLight = BackgroundLight(Rgb(255, 99, 132), CirclePosition(18, 22), 50),
    val beta: BackgroundLight = BackgroundLight(Rgb(88, 164, 255), CirclePosition(82, 20), 50),
    val gamma: BackgroundLight = BackgroundLight(Rgb(88, 255, 188), CirclePosition(50, 65), 40),
) {
    companion object {
        const val MAGIC_INTERVAL = 222
        const val ThemeId = "koala"
    }
}

val Koala = KoalaTheme()

object KoalaStyle {
    val Accent = Property<Rgb>("accent")
    val Primary = Property<Rgb>("primary")
    val RhoColor = Property<Rgb>("rho-color")
    val BetaColor = Property<Rgb>("beta-color")
    val GammaColor = Property<Rgb>("gamma-color")
    val RhoPosition = Property<CirclePosition>("rho-position")
    val BetaPosition = Property<CirclePosition>("beta-position")
    val GammaPosition = Property<CirclePosition>("gamma-position")
}

@Serializable
data class CirclePosition(
    val x: Int, // 0-100
    val y: Int,
    val radius: Int = 90 // % of viewport width
) {
    override fun toString() =
        "circle ${radius.coerceIn(0, 100)}vw at ${x.coerceIn(0, 100)}% ${y.coerceIn(0, 100)}%"
}

@Serializable
data class BackgroundLight(
    val color: Rgb,
    val position: CirclePosition,
    val brightness: Int,
)
