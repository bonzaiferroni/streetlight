package koala.dom

import koala.css.CssClass
import koala.css.LottieClass
import koala.css.applyModifiers
import kotlinx.html.DIV
import kotlinx.html.js.div
import org.w3c.dom.HTMLDivElement

inline fun DOMContext.lottie(
    filename: String,
    vararg modifiers: CssClass?,
    crossinline block: DIV.() -> Unit = { }
): HTMLDivElement {
    val div = div {
        applyModifiers(LottieClass, *modifiers)
        block()
    }
    initLottie(div, filename)
    return div
}