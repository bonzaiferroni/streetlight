package koala.html

import kotlinx.html.*

inline fun FlowContent.label(
    content: String,
    vararg modifiers: CssClass?,
    crossinline block: (DIV.() -> Unit) = { }
) {
    div {
        modify(*modifiers)
        block()
        +content
    }
}

inline fun FlowContent.paragraph(
    content: String = "",
    vararg modifiers: CssClass,
    crossinline block: P.() -> Unit = { }
) {
    p {
        modify(*modifiers)
        block()
        +content
    }
}

fun FlowContent.heading1(
    content: String,
    vararg modifiers: CssClass,
) {
    h1 {
        modify(*modifiers)
        +content
    }
}

fun FlowContent.heading2(
    content: String,
    vararg modifiers: CssClass,
) {
    h2 {
        modify(*modifiers)
        +content
    }
}

fun FlowContent.heading3(
    content: String,
    vararg modifiers: CssClass,
) {
    h3 {
        modify(*modifiers)
        +content
    }
}

fun FlowContent.heading4(
    content: String,
    vararg modifiers: CssClass,
) {
    h4 {
        modify(*modifiers)
        +content
    }
}

fun FlowContent.heading5(
    content: String,
    vararg modifiers: CssClass,
) {
    h5 {
        modify(*modifiers)
        +content
    }
}