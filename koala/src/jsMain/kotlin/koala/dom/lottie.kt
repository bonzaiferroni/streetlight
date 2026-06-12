package koala.dom

import koala.Lottie
import koala.core.initLottie
import koala.css.ModifierSet
import koala.html.configureLottie
import kotlinx.html.DIV
import kotlinx.html.js.div
import org.w3c.dom.HTMLDivElement

fun AppendScope.lottie(
    file: Lottie,
    modifiers: ModifierSet? = null,
    block: (DIV.() -> Unit)? = null
): HTMLDivElement {
    val div = div {
        configureLottie(file, modifiers, block)
    }
    initLottie(div)
    return div
}