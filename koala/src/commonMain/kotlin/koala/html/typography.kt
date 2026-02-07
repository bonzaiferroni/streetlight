package koala.html

import kotlinx.html.*
import koala.css.*

inline fun FlowContent.label(
    content: String,
    modifiers: ModifierSet? = null,
    crossinline block: (P.() -> Unit) = { }
) {
    p {
        applyModifiers(ElementClass.textLabel, modifiers)
        block()
        +content
    }
}

inline fun FlowContent.paragraph(
    content: String = "",
    modifiers: ModifierSet? = null,
    crossinline block: P.() -> Unit = { }
) {
    p {
        applyModifiers(modifiers)
        block()
        +content
    }
}

fun FlowContent.heading1(
    content: String,
    modifiers: ModifierSet? = null,
) {
    h1 {
        applyModifiers(modifiers)
        +content
    }
}

fun FlowContent.heading2(
    content: String,
    modifiers: ModifierSet? = null,
) {
    h2 {
        applyModifiers(modifiers)
        +content
    }
}

fun FlowContent.heading3(
    content: String,
    modifiers: ModifierSet? = null,
) {
    h3 {
        applyModifiers(modifiers)
        +content
    }
}

fun FlowContent.heading4(
    content: String,
    modifiers: ModifierSet? = null,
) {
    h4 {
        applyModifiers(modifiers)
        +content
    }
}

fun FlowContent.heading5(
    content: String,
    modifiers: ModifierSet? = null,
) {
    h5 {
        applyModifiers(modifiers)
        +content
    }
}