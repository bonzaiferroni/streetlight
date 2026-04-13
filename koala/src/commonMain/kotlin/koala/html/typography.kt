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
    content: String? = null,
    modifiers: ModifierSet? = null,
    block: H1.() -> Unit = {},
) {
    h1 {
        addModifiers(modifiers)
        block()
        content?.let {
            +it
        }
    }
}

fun FlowContent.heading2(
    content: String? = null,
    modifiers: ModifierSet? = null,
    block: H2.() -> Unit = {},
) {
    h2 {
        addModifiers(modifiers)
        block()
        content?.let {
            +it
        }
    }
}

fun FlowContent.heading3(
    content: String? = null,
    modifiers: ModifierSet? = null,
    block: H3.() -> Unit = {},
) {
    h3 {
        addModifiers(modifiers)
        block()
        content?.let {
            +it
        }
    }
}

fun FlowContent.heading4(
    content: String? = null,
    modifiers: ModifierSet? = null,
    block: H4.() -> Unit = {},
) {
    h4 {
        addModifiers(modifiers)
        block()
        content?.let {
            +it
        }
    }
}

fun FlowContent.heading5(
    content: String? = null,
    modifiers: ModifierSet? = null,
    block: H5.() -> Unit = {},
) {
    h5 {
        addModifiers(modifiers)
        block()
        content?.let {
            +it
        }
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