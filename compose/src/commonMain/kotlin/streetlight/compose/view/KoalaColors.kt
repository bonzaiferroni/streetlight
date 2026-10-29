package streetlight.compose.view

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import kotlin.math.ln
import kotlin.math.pow
import koala.modifier.Koala
import streetlight.model.data.EntityType
import streetlight.compose.utility.mixSrgb
import streetlight.compose.utility.scaleOklch
import streetlight.compose.utility.toColor

/**
 * The theme's colors, the counterpart of the color custom properties of the web's `ThemeCss`.
 *
 * An animated color is read from state, so reading it subscribes the reader to that color alone, and a reader in
 * a draw lambda redraws without recomposing.
 */
@Stable
class KoalaColors(
    val paper: Color,
    val ink: Color,
    opacity: KoalaOpacity,
    private val accentState: State<Color>,
    private val primaryState: State<Color>,
) {
    val accent get() = accentState.value
    val primary get() = primaryState.value
    val accentFg get() = accent.mixSrgb(ink, 0.5f)
    val primaryFg get() = primary.mixSrgb(ink, 0.5f)
    val primaryLightFg get() = primary.mixSrgb(ink, 0.25f)
    val primaryCardBg get() = primary.copy(alpha = 0.2f).mixSrgb(cardBg, 0.5f)
    val primaryButton get() = primary.scaleOklch(0.93f, 0.8f)
    val accentButton get() = accent.scaleOklch(0.93f, 0.8f)
    val grayBg = Koala.gray.toColor().mixSrgb(paper, 0.5f)
    val systemFg = Koala.purple.toColor().mixSrgb(ink, 0.5f)
    val red = Koala.red.toColor()
    val dangerBg = red.mixSrgb(paper, 0.75f)
    val errorBg = red.mixSrgb(paper, 0.25f)
    val successBg = Koala.green.toColor().mixSrgb(paper, 0.25f)
    val inkDisabled = ink.mixSrgb(paper, 0.5f)
    val voidBg = paper.mixSrgb(ink, 0.89f)
    val voidBorder = voidBg.mixSrgb(ink, 0.9f)
    val requiredBg = Koala.gold.toColor().mixSrgb(paper, 0.5f)
    val validBg = Koala.green.toColor().mixSrgb(paper, 0.5f)
    val requiredFg = Koala.gold.toColor().mixSrgb(paper, 0.65f)
    val validFg = Koala.green.toColor().mixSrgb(paper, 0.65f)
    val muted = ink.copy(alpha = opacity.high)
    val outlineLow = ink.copy(alpha = 0.1f)
    val outlineMid = ink.copy(alpha = 0.2f)
    val zenBg = paper.copy(alpha = 0.25f)
    val inkBg = ink.copy(alpha = 0.05f)
    val cardBg = paper.copy(alpha = 0.4f)
    val heavyCardBg = paper.copy(alpha = 0.8f)
    val seaGreenFg = Koala.seaGreen.toColor().mixSrgb(ink, 0.5f)
    val greenFg = Koala.green.toColor().mixSrgb(ink, 0.25f)
    val yellowFg = Koala.yellow.toColor().mixSrgb(ink, 0.25f)

    /** The web's `--primary-bg` over a surface of [size]: the [primaryFg] in a [radialBgOf]. */
    fun primaryBg(size: Size) = radialBgOf(primaryFg, size)

    /** The web's `--accent-bg` over a surface of [size]: the [accentFg] in a [radialBgOf]. */
    fun accentBg(size: Size) = radialBgOf(accentFg, size)

    /**
     * A background of [color] over paper on a surface of [size], brighter in a circle at the center that fades out
     * toward the farther sides, as the web's `--accent-bg`, `--primary-bg` and `SchemeBg` are.
     */
    fun radialBgOf(color: Color, size: Size): Brush {
        val inner = color.mixSrgb(paper, RadialCenterMix)
        val outer = color.mixSrgb(paper, RadialEdgeMix)
        // the web's color hint, its curve traced in stops
        val stops = Array(RadialHintStops + 1) { index ->
            val position = index.toFloat() / RadialHintStops
            position to inner.mixSrgb(outer, 1 - position.pow(RadialHintExponent))
        }
        // the web's circle farthest-side, holding the edge's color past it
        return Brush.radialGradient(*stops, center = size.center, radius = maxOf(size.width, size.height) / 2)
    }

    /** The color of [type], as the web maps it to its custom properties, or the primary color without a type. */
    fun colorOf(type: EntityType?): Color = when (type) {
        null, EntityType.Media, EntityType.Star, EntityType.Offering, EntityType.Skill -> primaryFg
        EntityType.Event -> accentFg
        EntityType.Galaxy -> seaGreenFg
        EntityType.Location -> greenFg
        EntityType.City -> yellowFg
    }
}

/** The color's share of a [KoalaColors.radialBgOf] at its center and at its edge, over paper. */
private const val RadialCenterMix = 0.25f
private const val RadialEdgeMix = 0.15f

/** The point where a radial background is halfway from its center to its edge, the web's color hint, and its stops. */
private const val RadialHint = 0.7f
private const val RadialHintStops = 8

/** The curve of the web's color hint: a position raised to it gives the progress toward the edge's color. */
private val RadialHintExponent = ln(0.5f) / ln(RadialHint)
