package koala.html

import koala.LottieFile
import kotlinx.html.*
import koala.css.*

fun FlowContent.lottie(
    file: LottieFile,
    modifiers: ModifierSet? = null,
    block: (DIV.() -> Unit)? = null
) {
    div {
        setModifiers(modify(ElementClass.lottie, modifiers))
        setAttribute(TagAttribute.lottie, file)
        block?.invoke(this)
    }
}