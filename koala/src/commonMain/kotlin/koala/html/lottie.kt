package koala.html

import koala.LottieFile
import kotlinx.html.*
import koala.css.*
import koala.css.setModifiers
import koala.html.setAttribute

fun FlowContent.lottie(
    file: LottieFile,
    modifiers: ModifierSet? = null,
    block: (DIV.() -> Unit)? = null
) {
    div {
        configureLottie(file, modifiers, block)
    }
}

internal fun DIV.configureLottie(
    file: LottieFile,
    modifiers: ModifierSet? = null,
    block: (DIV.() -> Unit)? = null
) {
    setModifiers(modify(ElementClass.lottie, modifiers))
    setAttribute(TagAttribute.lottie, file)
    block?.invoke(this)
}