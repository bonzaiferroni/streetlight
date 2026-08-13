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

    val rho: Glow = Glow(Rgb(255, 99, 132), GlowPosition(18, 22, 39), 50, 0),
    val beta: Glow = Glow(Rgb(88, 164, 255), GlowPosition(82, 20, 39), 50, 0),
    val gamma: Glow = Glow(Rgb(88, 255, 188), GlowPosition(50, 65, 36), 40, 0),
) {
    companion object {
        const val MagicInterval = 222
        const val ThemeId = "koala"
        const val MaxEnergy = 50
        const val MaxFocus = 99
        val ThemeTransitionSeconds = 1.2
    }
}

val Koala = KoalaTheme()

object KoalaStyle {
    val Accent = Property<Rgb>("accent")
    val Primary = Property<Rgb>("primary")
    val ColorFlux = Property<String>("color-flux")
    val RhoColor = Property<String>("rho-color")
    val BetaColor = Property<String>("beta-color")
    val GammaColor = Property<String>("gamma-color")
    val RhoX = Property<LinearDimension>("rho-x")
    val BetaX = Property<LinearDimension>("beta-x")
    val GammaX = Property<LinearDimension>("gamma-x")
    val RhoY = Property<LinearDimension>("rho-y")
    val BetaY = Property<LinearDimension>("beta-y")
    val GammaY = Property<LinearDimension>("gamma-y")
    val RhoRadius = Property<Int>("rho-radius")
    val BetaRadius = Property<Int>("beta-radius")
    val GammaRadius = Property<Int>("gamma-radius")
    val RhoFocus = Property<LinearDimension>("rho-focus")
    val BetaFocus = Property<LinearDimension>("beta-focus")
    val GammaFocus = Property<LinearDimension>("gamma-focus")
}

@Serializable
data class GlowPosition(
    val x: Int, // 0-100
    val y: Int,
    val radius: Int = 90 // % of viewport width
) {
    override fun toString() =
        "circle ${radius.coerceIn(0, 100)}vw at ${x.coerceIn(0, 100)}% ${y.coerceIn(0, 100)}%"
}

@Serializable
data class Glow(
    val color: Rgb,
    val position: GlowPosition,
    val energy: Int,
    val focus: Int,
)

fun Glow.rgba() = "rgba($color, ${energy / 100.0})"
