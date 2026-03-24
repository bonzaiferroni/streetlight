package koala.dom

import koala.LottieFile
import koala.core.initLottie
import koala.css.ElementClass
import koala.css.ModifierSet
import koala.css.setModifiers
import koala.html.TagAttribute
import koala.html.configureLottie
import kotlinx.html.DIV
import kotlinx.html.js.div
import org.w3c.dom.HTMLDivElement

fun DOMContext.lottie(
    file: LottieFile,
    modifiers: ModifierSet? = null,
    block: (DIV.() -> Unit)? = null
): HTMLDivElement {
    val div = div {
        configureLottie(file, modifiers, block)
    }
    initLottie(div)
    return div
}