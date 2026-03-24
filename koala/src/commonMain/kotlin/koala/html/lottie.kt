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
        applyModifiers(modify(ElementClass.lottie, modifiers))
        attributes[Attribute.lottie] = file.path
        block?.invoke(this)
    }
}