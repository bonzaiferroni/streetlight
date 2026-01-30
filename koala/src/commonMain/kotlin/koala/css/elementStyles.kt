package koala.css

import kotlinx.css.*
import kotlinx.css.properties.Timing
import kotlinx.css.properties.Transition
import kotlinx.css.properties.s

object LottieClass: CssClass { override val value: String = "lottie" }
object IconClass: CssClass { override val value: String = "icon" }
object LogoClass: CssClass { override val value: String = "logo" }
object ActionClass: CssClass { override val value: String = "action" }

fun CssBuilder.elementStyles(theme: KoalaTheme) {
    rule(LottieClass) {
        child("svg") {
//            display = Display.block
//            maxHeight = 100.pct
        }
    }

    rule(IconClass) {
        display = Display.inlineBlock
        backgroundColor = Color.currentColor
        put("aspect-ratio", "1 / 1")
        put("mask-image", "var(--mask-src)")
        put("-webkit-mask-image", "var(--mask-src)")
        put("mask-size", "contain")
        put("-webkit-mask-size", "contain")
        put("mask-repeat", "no-repeat")
        put("-webkit-mask-repeat", "no-repeat")
        put("mask-position", "center")
        put("-webkit-mask-position", "center")
    }

    rule(ActionClass) {
        transition += Transition("opacity", 0.35.s, Timing.ease)

        hover {
            opacity = 1
        }
    }
}