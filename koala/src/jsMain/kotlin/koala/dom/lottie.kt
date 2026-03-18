package koala.dom

import koala.core.initLottie
import koala.css.ElementClass
import koala.css.ModifierSet
import koala.css.applyModifiers
import koala.html.Attributes
import kotlinx.css.div
import kotlinx.html.DIV
import kotlinx.html.div
import kotlinx.html.js.div
import org.w3c.dom.HTMLDivElement

inline fun DOMContext.lottie(
    filename: String,
    modifiers: ModifierSet? = null,
    crossinline block: DIV.() -> Unit = { }
): HTMLDivElement {
    val div = div {
        applyModifiers(ElementClass.lottie, modifiers)
        attributes[Attributes.lottie.key] = filename
        block()
    }
    initLottie(div)
    return div
}