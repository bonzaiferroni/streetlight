package koala.html

import kotlinx.html.*
import koala.css.*

inline fun FlowContent.label(
    content: String,
    vararg modifiers: CssClass?,
    crossinline block: (DIV.() -> Unit) = { }
) {
    div {
        applyModifiers(*modifiers)
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
        applyModifiers(*modifiers)
        block()
        +content
    }
}

fun FlowContent.heading1(
    content: String,
    vararg modifiers: CssClass,
) {
    h1 {
        applyModifiers(*modifiers)
        +content
    }
}

fun FlowContent.heading2(
    content: String,
    vararg modifiers: CssClass,
) {
    h2 {
        applyModifiers(*modifiers)
        +content
    }
}

fun FlowContent.heading3(
    content: String,
    vararg modifiers: CssClass,
) {
    h3 {
        applyModifiers(*modifiers)
        +content
    }
}

fun FlowContent.heading4(
    content: String,
    vararg modifiers: CssClass,
) {
    h4 {
        applyModifiers(*modifiers)
        +content
    }
}

fun FlowContent.heading5(
    content: String,
    vararg modifiers: CssClass,
) {
    h5 {
        applyModifiers(*modifiers)
        +content
    }
}