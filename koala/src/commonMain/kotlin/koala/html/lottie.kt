package koala.html

import kotlinx.html.*
import koala.css.*

inline fun FlowContent.lottie(
    filename: String,
    modifiers: ModifierSet? = null,
    crossinline block: DIV.() -> Unit = { }
) {
    div {
        applyModifiers(modify(ElementClass.lottie, modifiers))
        attributes[Attribute.lottie] = filename
        block()
    }
}