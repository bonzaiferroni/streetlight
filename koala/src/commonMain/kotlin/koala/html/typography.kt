package koala.html

import kotlinx.html.*
import koala.css.*

inline fun FlowContent.label(
    content: String,
    modifiers: ModifierSet? = null,
    crossinline block: (P.() -> Unit) = { }
) {
    p {
        setModifiers(ElementClass.textLabel, modifiers)
        block()
        +content
    }
}

inline fun FlowContent.textBlock(
    content: String = "",
    modifiers: ModifierSet? = null,
    crossinline block: P.() -> Unit = { }
) {
    p {
        setModifiers(modifiers)
        block()
        +content
    }
}

fun FlowContent.heading1(
    content: String,
    modifiers: ModifierSet? = null,
) {
    h1 {
        setModifiers(modifiers)
        +content
    }
}

fun FlowContent.heading2(
    content: String,
    modifiers: ModifierSet? = null,
) {
    h2 {
        setModifiers(modifiers)
        +content
    }
}

fun FlowContent.heading3(
    content: String,
    modifiers: ModifierSet? = null,
) {
    h3 {
        setModifiers(modifiers)
        +content
    }
}

fun FlowContent.heading4(
    content: String,
    modifiers: ModifierSet? = null,
) {
    h4 {
        setModifiers(modifiers)
        +content
    }
}

fun FlowContent.heading5(
    content: String,
    modifiers: ModifierSet? = null,
) {
    h5 {
        setModifiers(modifiers)
        +content
    }
}

fun FlowContent.p(
    modifiers: ModifierSet,
    block: P.() -> Unit,
) {
    p {
        setModifiers(modifiers)
        block()
    }
}

fun FlowContent.span(
    modifiers: ModifierSet,
    block: SPAN.() -> Unit,
) {
    span {
        setModifiers(modifiers)
        block()
    }
}