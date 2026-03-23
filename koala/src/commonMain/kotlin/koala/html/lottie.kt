package koala.html

import koala.LottieFile
import kotlinx.html.*
import koala.css.*

inline fun FlowContent.lottie(
    file: LottieFile,
    modifiers: ModifierSet? = null,
    crossinline block: DIV.() -> Unit = { }
) {
    div {
        applyModifiers(modify(ElementClass.lottie, modifiers))
        attributes[Attribute.lottie] = file.path
        block()
    }
}