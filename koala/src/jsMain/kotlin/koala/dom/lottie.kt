package koala.dom

import koala.LottieFile
import koala.core.initLottie
import koala.css.ElementClass
import koala.css.ModifierSet
import koala.css.applyModifiers
import koala.html.Attribute
import kotlinx.css.div
import kotlinx.html.DIV
import kotlinx.html.div
import kotlinx.html.js.div
import org.w3c.dom.HTMLDivElement

inline fun DOMContext.lottie(
    file: LottieFile,
    modifiers: ModifierSet? = null,
    crossinline block: DIV.() -> Unit = { }
): HTMLDivElement {
    val div = div {
        applyModifiers(ElementClass.lottie, modifiers)
        attributes[Attribute.lottie.key] = file.path
        block()
    }
    initLottie(div)
    return div
}