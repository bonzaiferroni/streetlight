package koala.html

import kotlinx.html.*
import kotlinx.html.span as spanTag
import koala.css.*

inline fun FlowContent.textBlock(
    content: String = "",
    modifiers: ModifierSet? = null,
    crossinline block: P.() -> Unit = { }
) {
    p {
        addModifiers(modifiers)
        block()
        +content
    }
}

fun FlowContent.heading1(
    content: String,
    modifiers: ModifierSet? = null,
) {
    h1 {
        addModifiers(modifiers)
        +content
    }
}

fun FlowContent.heading2(
    content: String,
    modifiers: ModifierSet? = null,
) {
    h2 {
        addModifiers(modifiers)
        +content
    }
}

fun FlowContent.heading3(
    content: String,
    modifiers: ModifierSet? = null,
) {
    h3 {
        addModifiers(modifiers)
        +content
    }
}

fun FlowContent.heading4(
    content: String,
    modifiers: ModifierSet? = null,
) {
    h4 {
        addModifiers(modifiers)
        +content
    }
}

fun FlowContent.heading5(
    content: String,
    modifiers: ModifierSet? = null,
) {
    h5 {
        addModifiers(modifiers)
        +content
    }
}

fun FlowContent.p(
    modifiers: ModifierSet,
    block: P.() -> Unit,
) {
    p {
        addModifiers(modifiers)
        block()
    }
}

fun FlowContent.span(
    modifiers: ModifierSet? = null,
    block: SPAN.() -> Unit,
) {
    spanTag {
        addModifiers(modifiers)
        block()
    }
}

fun FlowContent.span(
    text: String,
    modifiers: ModifierSet? = null,
    block: SPAN.() -> Unit = {},
) {
    span(modifiers) {
        block()
        +text
    }
}