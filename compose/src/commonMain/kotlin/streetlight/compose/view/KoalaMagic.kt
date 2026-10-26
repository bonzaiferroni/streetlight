package streetlight.compose.view

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable
import koala.modifier.MagicStyle

/**
 * The theme's motion, the web's `MagicCss`: the [interval] of a transition in milliseconds, its [easing], the [bounce]
 * that overshoots as it settles, and the [initialScale] something grows from as it appears.
 */
@Immutable
class KoalaMagic(
    val interval: Int = MagicStyle.Interval,
    val easing: Easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f),
    val bounce: Easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f),
    val initialScale: Float = MagicStyle.InitialScale.toFloat(),
) {
    /** A transition over the [interval] with the [easing], the web's `--magic-easing`. */
    fun <T> eased(): FiniteAnimationSpec<T> = tween(interval, easing = easing)

    /** A transition over the [interval] that overshoots and settles, the web's `--magic-cubic-bounce`. */
    fun <T> bounced(): FiniteAnimationSpec<T> = tween(interval, easing = bounce)
}
