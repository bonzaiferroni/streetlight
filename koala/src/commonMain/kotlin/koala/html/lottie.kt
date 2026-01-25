package koala.html

import kotlinx.html.*

inline fun FlowContent.lottie(
    id: String,
    vararg modifiers: CssClass?,
    crossinline block: DIV.() -> Unit = { }
) {
    div {
        modify(*modifiers)
        attributes["lottie"] = "/static/lottie/${id}.json"
        block()
    }
}