package koala.dom

import koala.LottieFile
import koala.core.initLottie
import koala.css.ElementClass
import koala.css.ModifierSet
import koala.css.setModifiers
import koala.html.TagAttribute
import kotlinx.html.DIV
import kotlinx.html.js.div
import org.w3c.dom.HTMLDivElement

inline fun DOMContext.lottie(
    file: LottieFile,
    modifiers: ModifierSet? = null,
    crossinline block: DIV.() -> Unit = { }
): HTMLDivElement {
    val div = div {
        setModifiers(ElementClass.lottie, modifiers)
        attributes[TagAttribute.lottie.key] = file.path
        block()
    }
    initLottie(div)
    return div
}