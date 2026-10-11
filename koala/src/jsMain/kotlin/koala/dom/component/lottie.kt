package koala.dom.component

import koala.Lottie
import koala.core.initLottie
import koala.modifier.*
import kotlinx.html.DIV
import kotlinx.html.js.div
import web.html.HTMLDivElement
import koala.dom.AppendScope
import koala.dom.asWeb
import koala.component.configureLottie

/** A square Lottie animation of [file], started at once. */
fun AppendScope.lottie(
    file: Lottie,
    mod: Modifier? = null,
    block: (DIV.() -> Unit)? = null
): HTMLDivElement {
    val div = div {
        configureLottie(file, mod, block)
    }.asWeb()
    initLottie(div)
    return div
}