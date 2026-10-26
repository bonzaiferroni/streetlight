package koala.modifier

import kotlinx.css.*
import kotlinx.serialization.Serializable
import kotlin.math.abs

/** The colors, spacing unit and background glows a Koala stylesheet is built from. */
data class KoalaTheme(
    val paper: Rgb = Rgb(18, 26, 26),
    val ink: Rgb = Rgb(238, 230, 230),
    val spacingUnit: LinearDimension = 0.5.rem,
    val bg: Rgb = Rgb(9, 13, 13),
    val fg: Rgb = Rgb(245, 246, 246),
    val void: Rgb = Rgb(24, 31, 31),
    val gray: Rgb = Rgb(60, 62, 64),
    val red: Rgb = Rgb(255, 99, 132),
    val seaGreen: Rgb = Palette.SeaGreen.rgb,
    val green: Rgb = Rgb(99, 255, 132),
    val yellow: Rgb = Rgb(255, 240, 32),
    val gold: Rgb = Palette.Gold.rgb,
    val purple: Rgb = Rgb(158, 87, 200),
    val cyan: Rgb = Palette.Cyan.rgb,
    val magenta: Rgb = Palette.Magenta.rgb,
    val accent: Rgb = magenta,
    val primary: Rgb = cyan,

    val rho: Glow = Glow(Rgb(255, 99, 132), GlowPosition(18, 22, 39), 25, 0),
    val beta: Glow = Glow(Rgb(88, 164, 255), GlowPosition(82, 20, 39), 25, 0),
    val gamma: Glow = Glow(Rgb(88, 255, 188), GlowPosition(50, 65, 36), 20, 0),
) {
    companion object {
        const val ThemeId = "koala"
        const val MaxRadius = 200
        const val MaxEnergy = 50
        const val MaxFocus = 99
        val ThemeTransitionSeconds = 1.2

        /** The multiple of a glow's radius a shine surface reaches, half of it falling beyond the edge. */
        const val ShineSpread = 3.0

        /** The fraction of a glow's energy a shine surface casts. */
        const val ShineIntensity = 1.0

        /** The opacity of the ink over paper in a shine surface's opaque background, below its glows. */
        const val ShineInk = 0.1
    }
}

/** Assorted colors for any display that wants one, and the defaults of their [KoalaTheme] colors. [mod] colors text with it. */
enum class Palette(val rgb: Rgb, val mod: Class) {
    Magenta(Rgb(236, 46, 106), MagentaFg),
    Cyan(Rgb(26, 188, 200), CyanFg),
    Gold(Rgb(200, 178, 87), GoldFg),
    SeaGreen(Rgb(58, 200, 158), SeaGreenFg);

    companion object {
        /** The color at [index], wrapping around the palette, so a list can color its items in turn. */
        fun cycle(index: Int) = entries[index.mod(entries.size)]
    }
}

/** The default [KoalaTheme]. */
val Koala = KoalaTheme()

/** The custom properties the theme sets, for the accent, primary, and background glow colors. */
object KoalaStyle {
    val Accent = Property<Rgb>("accent", true)
    val Primary = Property<Rgb>("primary", true)
    val ColorFlux = Property<String>("color-flux", true)
    val RhoColor = Property<String>("rho-color", true)
    val BetaColor = Property<String>("beta-color", true)
    val GammaColor = Property<String>("gamma-color", true)
    val RhoX = Property<LinearDimension>("rho-x", true)
    val BetaX = Property<LinearDimension>("beta-x", true)
    val GammaX = Property<LinearDimension>("gamma-x", true)
    val RhoY = Property<LinearDimension>("rho-y", true)
    val BetaY = Property<LinearDimension>("beta-y", true)
    val GammaY = Property<LinearDimension>("gamma-y", true)
    val RhoRadius = Property<Int>("rho-radius", true)
    val BetaRadius = Property<Int>("beta-radius", true)
    val GammaRadius = Property<Int>("gamma-radius", true)
    val RhoFocus = Property<LinearDimension>("rho-focus", true)
    val BetaFocus = Property<LinearDimension>("beta-focus", true)
    val GammaFocus = Property<LinearDimension>("gamma-focus", true)
    val RhoShineX = Property<LinearDimension>("rho-shine-x", true)
    val BetaShineX = Property<LinearDimension>("beta-shine-x", true)
    val GammaShineX = Property<LinearDimension>("gamma-shine-x", true)
    val RhoShineY = Property<LinearDimension>("rho-shine-y", true)
    val BetaShineY = Property<LinearDimension>("beta-shine-y", true)
    val GammaShineY = Property<LinearDimension>("gamma-shine-y", true)

    val all = listOf(
        Accent, Primary, ColorFlux,
        RhoColor, BetaColor, GammaColor,
        RhoX, BetaX, GammaX,
        RhoY, BetaY, GammaY,
        RhoRadius, BetaRadius, GammaRadius,
        RhoFocus, BetaFocus, GammaFocus,
        RhoShineX, BetaShineX, GammaShineX,
        RhoShineY, BetaShineY, GammaShineY,
    )
}

/** Where a background glow sits, as percentages of the viewport, and its radius. */
@Serializable
data class GlowPosition(
    val x: Int, // 0-100% of viewport
    val y: Int,
    val radius: Int = 90 //
) {
    override fun toString() =
        "circle ${radius.coerceIn(0, 200)}vw at ${x.coerceIn(0, 200)}% ${y.coerceIn(0, 200)}%"
}

/**
 * Where a shine surface centers this glow, as percentages of the surface's sides: the point on its edge that the glow's
 * direction from the page's center points to, that direction scaled by its max norm and stretched to the surface.
 */
fun GlowPosition.shineAnchor(): Pair<Double, Double> {
    val dx = x - 50.0
    val dy = y - 50.0
    val norm = maxOf(abs(dx), abs(dy))
    return if (norm == 0.0) 50.0 to 50.0 else 50 + dx / norm * 50 to 50 + dy / norm * 50
}

/** A background glow: its color, position, strength and focus. */
@Serializable
data class Glow(
    val color: Rgb,
    val position: GlowPosition,
    val energy: Int,
    val focus: Int,
)

/** The glow's color as `rgba()`, with its energy as the alpha. */
fun Glow.rgba() = "rgba($color, ${energy / 100.0})"
