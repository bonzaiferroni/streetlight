package koala.css

import kotlinx.css.*

object LottieClass: CssClass { override val value: String = "lottie" }

fun CssBuilder.elementStyles(theme: KoalaTheme) {
    rule(LottieClass) {
        child("svg") {
//            display = Display.block
//            maxHeight = 100.pct
        }
    }
}