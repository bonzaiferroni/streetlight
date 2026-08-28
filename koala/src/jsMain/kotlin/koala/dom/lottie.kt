package koala.dom

import koala.Lottie
import koala.core.initLottie
import koala.css.ModifierSet
import koala.html.configureLottie
import kotlinx.html.DIV
import kotlinx.html.js.div
import web.html.HTMLDivElement

fun AppendScope.lottie(
    file: Lottie,
    mod: ModifierSet? = null,
    block: (DIV.() -> Unit)? = null
): HTMLDivElement {
    val div = div {
        configureLottie(file, mod, block)
    }.asWeb()
    initLottie(div)
    return div
}