package koala.html

import kotlinx.html.*
import koala.css.*

inline fun FlowContent.lottie(
    filename: String,
    vararg modifiers: CssClass?,
    crossinline block: DIV.() -> Unit = { }
) {
    div {
        modify(*modifiers)
        attributes["lottie"] = filename
        block()
    }
}