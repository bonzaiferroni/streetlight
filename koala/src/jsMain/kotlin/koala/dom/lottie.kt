package koala.dom

import koala.Lottie
import koala.core.initLottie
import koala.css.ModifierSet
import koala.html.configureLottie
import kotlinx.html.DIV
import kotlinx.html.js.div
import org.w3c.dom.HTMLDivElement

fun TagScope.lottie(
    file: Lottie,
    mod: ModifierSet? = null,
    block: (DIV.() -> Unit)? = null
): HTMLDivElement {
    val div = div {
        configureLottie(file, mod, block)
    }
    initLottie(div)
    return div
}