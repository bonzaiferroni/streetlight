package koala.dom

import koala.css.ElementClass
import koala.css.ModifierSet
import koala.css.applyModifiers
import kotlinx.html.DIV
import kotlinx.html.js.div
import org.w3c.dom.HTMLDivElement

inline fun DOMContext.lottie(
    filename: String,
    modifiers: ModifierSet? = null,
    crossinline block: DIV.() -> Unit = { }
): HTMLDivElement {
    val div = div {
        applyModifiers(ElementClass.lottie, modifiers)
        block()
    }
    initLottie(div, filename)
    return div
}